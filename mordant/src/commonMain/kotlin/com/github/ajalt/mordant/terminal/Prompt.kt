package com.github.ajalt.mordant.terminal

import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.widgets.Text

class PromptInputInvalid(message: String) : Exception(message)

/**
 * The base class for prompts.
 *
 * [ask] will print the prompt and ask for a line of user input. If [convert] raises a [PromptInputInvalid], its message
 * will be printed. This will repeat in a loop until the user input is valid.
 *
 */
abstract class Prompt<T>(
    // TODO: docs
    protected val prompt: String,
    protected val terminal: Terminal,
    protected val default: T? = null,
    protected val showDefault: Boolean = true,
    protected val showChoices: Boolean = true,
    protected val hideInput: Boolean = false,
    protected val choices: Collection<T> = emptyList(),
    protected val promptSuffix: String = ": ",
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
            append(terminal.theme.style("prompt.prompt")(prompt + promptSuffix))
            if (showChoices && choices.isNotEmpty()) {
                append(terminal.theme.style("prompt.choices")(choices.joinToString(prefix = " [",
                    postfix = "]") { renderValue(it) }))
            }

            if (showDefault && default != null) {
                append(terminal.theme.style("prompt.default")(choices.joinToString(prefix = " (",
                    postfix = ")") { renderValue(it) }))
            }
        })
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
            try {
                val value = convert(line)
            } catch (e: PromptInputInvalid) {
                handleInvalidInput(e.message!!)
            }

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
    showDefault: Boolean = true,
    showChoices: Boolean = true,
    hideInput: Boolean = false,
    choices: List<String> = emptyList(),
    promptSuffix: String = ": ",
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
) {
    override fun convert(input: String): String {
        if (!allowBlank && input.isBlank()) {
            throw PromptInputInvalid("")
        }
        return input
    }
}
