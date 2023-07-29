package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.terminal.ConfirmationPrompt.Companion.create
import com.github.ajalt.mordant.terminal.ConfirmationPrompt.Companion.createString
import com.github.ajalt.mordant.widgets.Text

sealed class ConversionResult<out T> {
    data class Valid<T>(val value: T) : ConversionResult<T>()
    data class Invalid(val message: String) : ConversionResult<Nothing>()
}

/**
 * The base class for prompts.
 *
 * [ask] will print the [prompt][makePrompt] and ask for a line of user input and return the result
 * of passing that line to [convert]. If [convert] instead returns [ConversionResult.Invalid], its
 * message will be printed and the user will be asked for input again.
 *
 *
 * ### Theme styles used
 *  - `prompt.prompt`: applied to the [prompt] string
 *  - `prompt.choices`: applied to the rendered [choices] values when shown in the prompt
 *  - `prompt.default`: applied to the rendered [default] value when shown in the prompt
 *  - `prompt.choices.invalid`: applied to the [invalidChoiceMessage] when shown
 *  - `danger`: applied to the [error message][ConversionResult.Invalid.message] returned by [convert]
 */
abstract class Prompt<T>(
    /** The message asking for input to show the user */
    protected val prompt: String,
    /** The terminal to use */
    val terminal: Terminal,
    /** The value to return if the user enters an empty line, or `null` to require a value */
    protected val default: T? = null,
    /**
     * If true and a [default] is specified, [makePrompt] will add the [rendered][renderValue]
     * default to the prompt
     */
    protected val showDefault: Boolean = true,
    /**
     * If true and [choices] are specified, [makePrompt] will add the [rendered][renderValue]
     * choices to the prompt
     */
    protected val showChoices: Boolean = true,
    /**
     * If true, the user's input will be treated like a password and hidden from the screen. This
     * value will be ignored on platforms where it is not supported.
     */
    protected val hideInput: Boolean = false,
    /**  The set of values that the user must choose from. */
    protected val choices: Collection<T> = emptyList(),
    /** A string to append after [prompt] when showing the user the prompt */
    protected val promptSuffix: String = ": ",
    /**
     * The message to show the user if [choices] is specified and they enter a value that isn't one
     * of the choices.
     */
    protected val invalidChoiceMessage: String = "Invalid value, choose from ",
) {
    /**
     * Convert the string entered by the user into the final type
     *
     * Returns a [Valid][ConversionResult.Valid] result with the converted value or an
     * [Invalid][ConversionResult.Invalid] with an error message.
     */
    protected abstract fun convert(input: String): ConversionResult<T>


    /**
     * Convert a value of this type to a string to show the user.
     *
     * This is called when displaying the [default] or [choices].
     */
    protected open fun renderValue(value: T): String = value.toString()

    /**
     * Given a [prompt] string, return the widget to show to the user.
     */
    protected open fun makePrompt(): Widget {
        return Text(buildString {
            append(terminal.theme.style("prompt.prompt")(prompt))
            if (showChoices && choices.isNotEmpty()) {
                append(" ")
                append(terminal.theme.style("prompt.choices")(makePromptChoices()))
            }

            if (showDefault && default != null) {
                append(" ")
                append(terminal.theme.style("prompt.default")(makePromptDefault(default)))
            }
            append(promptSuffix)
        })
    }

    /**  Return a string to add to the prompt to show the user the default. */
    protected open fun makePromptDefault(default: T) = "(${renderValue(default)})"

    /**  Return a string to add to the prompt to show the user the choices. */
    protected open fun makePromptChoices() =
        choices.joinToString(prefix = "[", postfix = "]") { renderValue(it) }

    /**
     * Create the message to show the user when [choices] is defined and the entered value isn't a valid choice
     */
    protected open fun makeInvalidChoiceMessage(): Widget {
        return Text(terminal.theme.style("prompt.choices.invalid")(buildString {
            append(invalidChoiceMessage)
            append(makePromptChoices())
        }))
    }

    /**
     * Called before the prompt is printed.
     *
     * Does nothing by default, but you can print extra messages here for example.
     */
    protected open fun beforePrompt() {}

    /**
     * Called when [convert] returns a [ConversionResult.Invalid].
     *
     * By default, this prints the [message] with the `danger` style
     */
    protected open fun handleInvalidInput(message: String) {
        if (message.isNotBlank()) {
            terminal.danger(message)
        }
    }

    /**
     * Run the prompt, asking the user for input.
     *
     * @return The [converted][convert] user input, or `null` if EOF was reached before this function was called.
     */
    open fun ask(): T? {
        while (true) {
            beforePrompt()
            terminal.print(makePrompt())
            val line = terminal.readLineOrNull(hideInput) ?: return null
            if (line.isEmpty() && default != null) {
                return default
            }
            val value = when (val result = convert(line)) {
                is ConversionResult.Valid<T> -> result.value
                is ConversionResult.Invalid -> {
                    handleInvalidInput(result.message)
                    continue
                }
            }

            if (choices.isNotEmpty() && value !in choices) {
                terminal.println(makeInvalidChoiceMessage())
                continue
            }

            return value
        }
    }
}

/**
 * A [Prompt] that returns the user input unchanged.
 *
 * @property allowBlank If false, the user will be prompted for a new input if they enter a blank value
 */
class StringPrompt(
    prompt: String,
    terminal: Terminal,
    default: String? = null,
    showDefault: Boolean = false, // !default.isNullOrBlank(), disabled due to KT-59326
    showChoices: Boolean = true,
    hideInput: Boolean = false,
    choices: List<String> = emptyList(),
    promptSuffix: String = ": ",
    invalidChoiceMessage: String = "Invalid value, choose from ",
    private val allowBlank: Boolean = true,
) : Prompt<String>(
    prompt,
    terminal,
    default,
    showDefault,
    showChoices,
    hideInput,
    choices,
    promptSuffix,
    invalidChoiceMessage
) {
    override fun convert(input: String): ConversionResult<String> {
        if (!allowBlank && input.isBlank()) {
            return ConversionResult.Invalid("")
        }
        return ConversionResult.Valid(input)
    }
}

/**
 * A boolean prompt that asks for a yes or no response.
 *
 * @param prompt The message asking for input to show the user
 * @param terminal The terminal to use
 * @param default The value to return if the user enters an empty line, or `null` to require a value
 * @param uppercaseDefault If true and [default] is not `null`, the default choice will be shown in uppercase.
 * @param showChoices If true, the choices will be added to the [prompt]
 * @param choiceStrings The strings to accept for `true` and `false` inputs
 * @param promptSuffix A string to append after [prompt] when showing the user the prompt
 * @param invalidChoiceMessage The message to show the user if they enter a value that isn't one of the [choiceStrings].
 */
class YesNoPrompt(
    prompt: String,
    terminal: Terminal,
    default: Boolean? = null,
    private val uppercaseDefault: Boolean = true,
    showChoices: Boolean = true,
    private val choiceStrings: List<String> = listOf("y", "n"),
    promptSuffix: String = ": ",
    invalidChoiceMessage: String = "Invalid value, choose from ",
) : Prompt<Boolean>(
    prompt,
    terminal,
    default,
    false,
    showChoices,
    false,
    listOf(true, false),
    promptSuffix,
    invalidChoiceMessage
) {
    override fun convert(input: String): ConversionResult<Boolean> {
        return when (input.trim().lowercase()) {
            choiceStrings[0] -> ConversionResult.Valid(true)
            choiceStrings[1] -> ConversionResult.Valid(false)
            else -> ConversionResult.Invalid(buildString {
                append(invalidChoiceMessage)
                append(makePromptChoices())
            })
        }
    }

    override fun makePromptChoices(): String {
        return choices.joinToString("/", prefix = "[", postfix = "]") { renderValue(it) }
    }

    override fun renderValue(value: Boolean): String {
        val s = if (value) choiceStrings[0] else choiceStrings[1]
        return if (uppercaseDefault && value == default) s.uppercase() else s
    }
}

/**
 * A prompt that requires the user to enter the same value twice.
 *
 * You can create an instance of this class by passing in two [Prompt]s to the constructor, or by
 * using the [create] or [createString] methods.
 */
class ConfirmationPrompt<T : Any>(
    private val firstPrompt: Prompt<T>,
    private val secondPrompt: Prompt<T>,
    private val valueMismatchMessage: String = "Values do not match, try again",
) {
    companion object {
        /**
         * Create a ConfirmationPrompt from two strings which are passed to a [builder] to construct
         * the prompt instances.
         *
         * ### Example
         *
         * ```kotlin
         * ConfirmationPrompt.create(
         *    "Delete files?", "Are you sure?"
         * ) { YesNoPrompt(it, terminal) }
         * ```
         */
        fun <T : Any> create(
            firstPrompt: String,
            secondPrompt: String,
            valueMismatchMessage: String = "Values do not match, try again",
            builder: (String) -> Prompt<T>,
        ): ConfirmationPrompt<T> {
            return ConfirmationPrompt(
                builder(firstPrompt),
                builder(secondPrompt),
                valueMismatchMessage
            )
        }

        /**
         * Create a ConfirmationPrompt that uses [StringPrompt]s to ask for input.
         *
         * All parameters are passed to the [StringPrompt] constructor.
         */
        fun createString(
            firstPrompt: String,
            secondPrompt: String,
            terminal: Terminal,
            default: String? = null,
            showDefault: Boolean = false, // !default.isNullOrBlank(), disabled due to KT-59326
            showChoices: Boolean = true,
            hideInput: Boolean = false,
            choices: List<String> = emptyList(),
            promptSuffix: String = ": ",
            invalidChoiceMessage: String = "Invalid value, choose from ",
            valueMismatchMessage: String = "Values do not match, try again",
        ): ConfirmationPrompt<String> {
            return create(firstPrompt, secondPrompt, valueMismatchMessage) {
                StringPrompt(
                    it,
                    terminal,
                    default,
                    showDefault,
                    showChoices,
                    hideInput,
                    choices,
                    promptSuffix,
                    invalidChoiceMessage
                )
            }
        }
    }

    /**
     * Run the prompt, asking the user for input.
     *
     * @return The [converted][Prompt.convert] user input, or `null` if EOF was reached before this
     *   function was called.
     */
    fun ask(): T? {
        while (true) {
            val value = firstPrompt.ask() ?: return null
            val secondValue = secondPrompt.ask() ?: return null
            if (value == secondValue) {
                return value
            } else {
                firstPrompt.terminal.danger(valueMismatchMessage)
            }
        }
    }
}
