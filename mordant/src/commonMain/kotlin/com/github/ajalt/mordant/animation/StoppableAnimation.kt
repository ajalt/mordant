package com.github.ajalt.mordant.animation

interface StoppableAnimation {
    /**
     * Stop this animation without removing it from the screen.
     *
     * Anything printed to the terminal after this call will be printed below this last frame of
     * this animation.
     */
    fun stop()

    /**
     * Stop this animation and remove it from the screen.
     */
    fun clear()
}
