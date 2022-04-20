package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.widgets.Text

class PromptInputInvalid(message: String) : Exception(message)

/**
 * The base class for prompts.
 *
 * [ask] will print the [prompt][makePrompt] and ask for a line of user input and return the result of passing that line
 * to [convert]. If [convert] instead raises [PromptInputInvalid], its message will be printed and the user will be
 * asked for input again.
 *
 *
 * ### Themes used
 *  - `prompt.prompt`: applied to the [prompt] string
 *  - `prompt.choices`: applied to the rendered [choices] values when shown in the prompt
 *  - `prompt.default`: applied to the rendered [default] value when shown in the prompt
 *  - `prompt.choices.invalid`: applied to the [invalidChoiceMessage] when shown
 *  - `danger`: applied to the [error message][PromptInputInvalid.message] raised by [convert]
 *
 * @property prompt The message asking for input to show the user
 * @property terminal The terminal to use
 * @property default The value to return if the user enters an empty line, or `null` to require a value
 * @property showDefault If true and a [default] is specified, [makePrompt] will add the
 *   [rendered][renderValue] default to the prompt
 * @property hideInput If true, the user's input will be treated like a password and hidden from
 *   the screen. [hideInput] will be ignored on platforms where it is not supported.
 * @property choices The set of values that the user must choose from.
 * @property promptSuffix A string to append after [prompt] when showing the user the prompt
 * @property invalidChoiceMessage The message to show the user if [choices] is specified,
 *   and they enter a value that isn't one of the choices.
 */
abstract class Prompt<T>(
    protected val prompt: String,
    protected val terminal: Terminal,
    protected val default: T? = null,
    protected val showDefault: Boolean = true,
    protected val showChoices: Boolean = true,
    protected val hideInput: Boolean = false,
    protected val choices: Collection<T> = emptyList(),
    protected val promptSuffix: String = ": ",
    protected val invalidChoiceMessage: String = "Invalid value, choose from ",
) {
    /**
     * Convert the string entered by the user into the final type
     *
     * If the input cannot be converted, raise a [PromptInputInvalid] with a message to print to the user
     */
    abstract protected fun convert(input: String): T


    /**
     * Convert a value of this type to a string to show the user.
     *
     * This is called when displaying the [default] or [choices].
     */
    open protected fun renderValue(default: T): String = default.toString()

    /**
     * Given a [prompt] string, return the widget to show to the user.
     */
    open protected fun makePrompt(): Widget {
        return Text(buildString {
            append(terminal.theme.style("prompt.prompt")(prompt))
            if (showChoices && choices.isNotEmpty()) {
                append(" ")
                append(terminal.theme.style("prompt.choices")(choices.joinToString(prefix = "[",
                    postfix = "]") { renderValue(it) }))
            }

            if (showDefault && default != null) {
                append(" ")
                append(terminal.theme.style("prompt.default")("(${renderValue(default)})"))
            }
            append(promptSuffix)
        })
    }

    /**
     * Create the message to show the user when [choices] is defined and the entered value isn't a valid choice
     */
    open protected fun makeInvalidChoiceMessage(): Widget {
        return Text(terminal.theme.style("prompt.choices.invalid")(buildString {
            append(invalidChoiceMessage)
            choices.joinTo(this, prefix = "[", postfix = "]") { renderValue(it) }
        }))
    }

    /**
     * Called before the prompt is printed.
     *
     * Does nothing by default.
     */
    protected fun beforePrompt() {}

    /**
     * Called when [convert] raises a [PromptInputInvalid] with the exception's [message].
     *
     * By default, this prints the [message] with the `danger` style
     */
    protected fun handleInvalidInput(message: String) {
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
            val value = try {
                convert(line)
            } catch (e: PromptInputInvalid) {
                handleInvalidInput(e.message!!)
                continue
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
    showDefault: Boolean = !default.isNullOrBlank(),
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
    override fun convert(input: String): String {
        if (!allowBlank && input.isBlank()) {
            throw PromptInputInvalid("")
        }
        return input
    }
}
