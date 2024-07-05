package com.github.ajalt.mordant.internal.syscalls.ffm

import java.lang.foreign.*
import java.lang.foreign.MemoryLayout.PathElement
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal object Layouts {
    val BOOL: ValueLayout.OfBoolean = ValueLayout.JAVA_BOOLEAN
    val CHAR: ValueLayout.OfByte = ValueLayout.JAVA_BYTE
    val WCHAR: ValueLayout.OfChar = ValueLayout.JAVA_CHAR
    val SHORT: ValueLayout.OfShort = ValueLayout.JAVA_SHORT
    val INT: ValueLayout.OfInt = ValueLayout.JAVA_INT
    val LONG: ValueLayout.OfLong = ValueLayout.JAVA_LONG
    val LONG_LONG: ValueLayout.OfLong = ValueLayout.JAVA_LONG
    val FLOAT: ValueLayout.OfFloat = ValueLayout.JAVA_FLOAT
    val DOUBLE: ValueLayout.OfDouble = ValueLayout.JAVA_DOUBLE
    val POINTER: AddressLayout = ValueLayout.ADDRESS
}

internal class FieldLayout<T>(
    val name: String,
    val layout: MemoryLayout,
    val access: (MemorySegment) -> T,
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
    return fieldDelegate(layout) { name, parent, segment ->
        convert(parent.layout.varHandle(name).get(segment))
    }
}

@Suppress("UnusedReceiverParameter")
internal inline fun <T : StructAccessor> StructLayout.structField(
    field: StructLayout,
    crossinline construct: (MemorySegment) -> T,
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return fieldDelegate(field.layout) { name, parent, segment ->
        construct(segment.offsetOf(name, parent.layout, field.layout))
    }
}

@Suppress("UnusedReceiverParameter")
internal inline fun <reified T> StructLayout.customField(
    layout: MemoryLayout,
    crossinline construct: (MemorySegment, parent: StructLayout) -> T,
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return fieldDelegate(layout) { _, parent, segment ->
        construct(segment, parent)
    }
}

private inline fun <T> fieldDelegate(
    layout: MemoryLayout,
    crossinline access: (name: String, parent: StructLayout, MemorySegment) -> T,
): PropertyDelegateProvider<StructLayout, ReadOnlyProperty<Any?, FieldLayout<T>>> {
    return PropertyDelegateProvider { parent, property ->
        val fl = FieldLayout(property.name, layout) { segment ->
            access(property.name, parent, segment)
        }
        parent.registerField(fl)
        ReadOnlyProperty { _, _ -> fl }
    }
}

internal fun StructLayout.shortField() = scalarField<Short>(Layouts.SHORT)
internal fun StructLayout.paddingField(size: Long) =
    scalarField<Unit>(MemoryLayout.paddingLayout(size)) { }

internal interface StructAccessor {
    val segment: MemorySegment

    operator fun <T> FieldLayout<T>.getValue(thisRef: StructAccessor, property: KProperty<*>): T {
        return access(thisRef.segment)
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
