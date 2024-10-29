package com.github.ajalt.mordant.terminal.terminalinterface.ffm

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal object Layouts {
//    val BOOL: ValueLayout.OfBoolean = ValueLayout.JAVA_BOOLEAN
    val BYTE: ValueLayout.OfByte = ValueLayout.JAVA_BYTE
    val SHORT: ValueLayout.OfShort = ValueLayout.JAVA_SHORT
    val INT: ValueLayout.OfInt = ValueLayout.JAVA_INT
    val LONG: ValueLayout.OfLong = ValueLayout.JAVA_LONG
//    val FLOAT: ValueLayout.OfFloat = ValueLayout.JAVA_FLOAT
//    val DOUBLE: ValueLayout.OfDouble = ValueLayout.JAVA_DOUBLE
    val POINTER: AddressLayout = ValueLayout.ADDRESS
}

internal class FieldLayout<T>(
    val name: String,
    val layout: MemoryLayout,
    val access: (MemorySegment) -> T,
    val set: (MemorySegment, T) -> Unit = { _, _ -> },
)

internal abstract class StructLayout {
    private val fields = mutableListOf<FieldLayout<*>>()

    fun registerField(layout: FieldLayout<*>) {
        fields.add(layout)
    }

    val layout: MemoryLayout
        get() = MemoryLayout.structLayout(
            *fields.map { it.layout.withName(it.name) }.toTypedArray()
        )
}


@Suppress("UnusedReceiverParameter")
internal inline fun <reified T> StructLayout.scalarField(
    layout: MemoryLayout,
    crossinline convert: (Any) -> T = { it as T },
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return fieldDelegate(layout,
        { name, parent, segment -> convert(parent.layout.varHandle(name).get(segment)) },
        { name, parent, segment, value -> parent.layout.varHandle(name).set(segment, value) }
    )
}

@Suppress("UnusedReceiverParameter")
internal inline fun <T : StructAccessor> StructLayout.structField(
    field: StructLayout,
    crossinline construct: (MemorySegment) -> T,
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return fieldDelegate(field.layout, { name, parent, segment ->
        construct(segment.offsetOf(name, parent.layout, field.layout))
    })
}

@Suppress("UnusedReceiverParameter")
internal inline fun <reified T> StructLayout.customField(
    layout: MemoryLayout,
    crossinline construct: (MemorySegment, parent: StructLayout) -> T,
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return fieldDelegate(layout,
        { _, parent, segment -> construct(segment, parent) }
    )
}

private inline fun <T> fieldDelegate(
    layout: MemoryLayout,
    crossinline access: (name: String, parent: StructLayout, MemorySegment) -> T,
    crossinline set: (name: String, parent: StructLayout, MemorySegment, T) -> Unit = { _, _, _, _ -> },
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return PropertyDelegateProvider { parent, property ->
        val fl = FieldLayout(
            property.name, layout,
            { segment -> access(property.name, parent, segment) },
            { segment, value -> set(property.name, parent, segment, value) },
        )
        parent.registerField(fl)
        ReadOnlyProperty { _, _ -> fl }
    }
}

internal fun StructLayout.byteField() = scalarField<Byte>(Layouts.BYTE)
internal fun StructLayout.shortField() = scalarField<Short>(Layouts.SHORT)
internal fun StructLayout.intField() = scalarField<Int>(Layouts.INT)
internal fun StructLayout.longField() = scalarField<Long>(Layouts.LONG)
//internal fun StructLayout.floatField() = scalarField<Float>(Layouts.FLOAT)
//internal fun StructLayout.doubleField() = scalarField<Double>(Layouts.DOUBLE)
//internal fun StructLayout.boolField() = scalarField<Boolean>(Layouts.BOOL)
@Suppress("UnusedReceiverParameter")
internal fun StructLayout.arrayField(
    size: Long,
    elementLayout: MemoryLayout = Layouts.BYTE,
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<MemorySegment>>> {
    return fieldDelegate(
        MemoryLayout.sequenceLayout(size, elementLayout), { name, parent, segment ->
            segment.asSlice(parent.layout.byteOffset(name), size * elementLayout.byteSize())
        }
    )
}

internal fun StructLayout.paddingField(size: Long) =
    scalarField<Unit>(MemoryLayout.paddingLayout(size)) { }

internal interface StructAccessor {
    val segment: MemorySegment

    operator fun <T> FieldLayout<T>.getValue(thisRef: StructAccessor, property: KProperty<*>): T {
        return access(thisRef.segment)
    }

    operator fun <T> FieldLayout<T>.setValue(
        thisRef: StructAccessor,
        property: KProperty<*>,
        value: T,
    ) {
        set(thisRef.segment, value)
    }
}


internal fun Arena.allocateInt(): MemorySegment = allocate(ValueLayout.JAVA_INT)
internal fun MemoryLayout.varHandle(vararg path: String): VarHandle {
    val handle = varHandle(*path.map { PathElement.groupElement(it) }.toTypedArray())
    return MethodHandles.insertCoordinates(handle, handle.coordinateTypes().lastIndex, 0)
}

internal fun MemoryLayout.byteOffset(name: String): Long {
    return byteOffset(PathElement.groupElement(name))
}

internal fun MemorySegment.offsetOf(
    name: String,
    parent: MemoryLayout,
    layout: MemoryLayout,
): MemorySegment {
    return asSlice(parent.byteOffset(name), layout.byteSize())
}

internal abstract class MethodHandlesHolder(
    private val linker: Linker = Linker.nativeLinker(),
    private val lookup: SymbolLookup = SymbolLookup.loaderLookup().or(linker.defaultLookup()),
) {
    protected fun handle(
        resLayout: MemoryLayout,
        vararg argLayouts: MemoryLayout,
        linkerOptions: Array<Linker.Option> = emptyArray(),
    ) =
        PropertyDelegateProvider<MethodHandlesHolder, ReadOnlyProperty<Any?, MethodHandle>> { _, property ->
            val name = property.name
            ReadOnlyProperty { _, _ ->
                lookup.find(name)
                    .map {
                        linker.downcallHandle(
                            it,
                            FunctionDescriptor.of(resLayout, *argLayouts),
                            *linkerOptions
                        )
                    }
                    .orElseThrow()
            }
        }
}
