package com.github.ajalt.mordant.input.coroutines

import com.github.ajalt.mordant.input.*
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow

/**
 * Enter raw mode, emit input events until the flow in cancelled, then exit raw mode.
 *
 * @param mouseTracking The type of mouse tracking to enable.
 */
fun Terminal.receiveEventsFlow(
    mouseTracking: MouseTracking = MouseTracking.Normal,
): Flow<InputEvent> = flow {
    enterRawMode(mouseTracking).use {
        while (true) emit(it.readEvent())
    }
}

/**
 * Enter raw mode, emit [KeyboardEvent]s until the flow in cancelled, then exit raw mode.
 */
fun Terminal.receiveKeyEventsFlow(
): Flow<KeyboardEvent> = receiveEventsFlow(MouseTracking.Off).filterIsInstance()

/**
 * Enter raw mode, emit [MouseEvent]s until the flow in cancelled, then exit raw mode.
 *
 * @param mouseTracking The type of mouse tracking to enable.
 */
fun Terminal.receiveMouseEventsFlow(
    mouseTracking: MouseTracking = MouseTracking.Normal,
): Flow<MouseEvent> {
    require(mouseTracking != MouseTracking.Off) {
        "Mouse tracking must be enabled to receive mouse events"
    }
    return receiveEventsFlow(mouseTracking).filterIsInstance()
}
