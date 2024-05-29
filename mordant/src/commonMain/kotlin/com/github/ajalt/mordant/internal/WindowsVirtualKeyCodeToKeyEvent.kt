package com.example

// https://learn.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes
// https://developer.mozilla.org/en-US/docs/Web/API/UI_Events/Keyboard_event_key_values
object WindowsVirtualKeyCodeToKeyEvent {
    private val map = mapOf(
        (0x12).toShort() to "Alt", // VK_MENU
        (0x14).toShort() to "CapsLock", // VK_CAPITAL
        (0x11).toShort() to "Control", // VK_CONTROL
        (0x5B).toShort() to "Meta", // VK_LWIN
        (0x90).toShort() to "NumLock", // VK_NUMLOCK
        (0x91).toShort() to "ScrollLock", // VK_SCROLL
        (0x10).toShort() to "Shift", // VK_SHIFT
        (0x0D).toShort() to "Enter", // VK_RETURN
        (0x09).toShort() to "Tab", // VK_TAB
        (0x20).toShort() to " ", // VK_SPACE
        (0x28).toShort() to "ArrowDown", // VK_DOWN
        (0x25).toShort() to "ArrowLeft", // VK_LEFT
        (0x27).toShort() to "ArrowRight", // VK_RIGHT
        (0x26).toShort() to "ArrowUp", // VK_UP
        (0x23).toShort() to "End", // VK_END
        (0x24).toShort() to "Home", // VK_HOME
        (0x22).toShort() to "PageDown", // VK_NEXT
        (0x21).toShort() to "PageUp", // VK_PRIOR
        (0x08).toShort() to "Backspace", // VK_BACK
        (0x0C).toShort() to "Clear", // VK_CLEAR
        (0xF7).toShort() to "CrSel", // VK_CRSEL
        (0x2E).toShort() to "Delete", // VK_DELETE
        (0xF9).toShort() to "EraseEof", // VK_EREOF
        (0xF8).toShort() to "ExSel", // VK_EXSEL
        (0x2D).toShort() to "Insert", // VK_INSERT
        (0x1E).toShort() to "Accept", // VK_ACCEPT
        (0xF0).toShort() to "Attn", // VK_OEM_ATTN
        (0x5D).toShort() to "ContextMenu", // VK_APPS
        (0x1B).toShort() to "Escape", // VK_ESCAPE
        (0x2B).toShort() to "Execute", // VK_EXECUTE
        (0xF1).toShort() to "Finish", // VK_OEM_FINISH
        (0x2F).toShort() to "Help", // VK_HELP
        (0x13).toShort() to "Pause", // VK_PAUSE
        (0xFA).toShort() to "Play", // VK_PLAY
        (0x29).toShort() to "Select", // VK_SELECT
        (0x2C).toShort() to "PrintScreen", // VK_SNAPSHOT
        (0x5F).toShort() to "Standby", // VK_SLEEP
        (0xF0).toShort() to "Alphanumeric", // VK_OEM_ATTN
        (0x1C).toShort() to "Convert", // VK_CONVERT
        (0x18).toShort() to "FinalMode", // VK_FINAL
        (0x1F).toShort() to "ModeChange", // VK_MODECHANGE
        (0x1D).toShort() to "NonConvert", // VK_NONCONVERT
        (0xE5).toShort() to "Process", // VK_PROCESSKEY
        (0x15).toShort() to "HangulMode", // VK_HANGUL
        (0x19).toShort() to "HanjaMode", // VK_HANJA
        (0x17).toShort() to "JunjaMode", // VK_JUNJA
        (0xF3).toShort() to "Hankaku", // VK_OEM_AUTO
        (0xF2).toShort() to "Hiragana", // VK_OEM_COPY
        (0x15).toShort() to "KanaMode", // VK_KANA
        (0xF1).toShort() to "Katakana", // VK_OEM_FINISH
        (0xF5).toShort() to "Romaji", // VK_OEM_BACKTAB
        (0xF4).toShort() to "Zenkaku", // VK_OEM_ENLW
        (0x70).toShort() to "F1", // VK_F1
        (0x71).toShort() to "F2", // VK_F2
        (0x72).toShort() to "F3", // VK_F3
        (0x73).toShort() to "F4", // VK_F4
        (0x74).toShort() to "F5", // VK_F5
        (0x75).toShort() to "F6", // VK_F6
        (0x76).toShort() to "F7", // VK_F7
        (0x77).toShort() to "F8", // VK_F8
        (0x78).toShort() to "F9", // VK_F9
        (0x79).toShort() to "F10", // VK_F10
        (0x7A).toShort() to "F11", // VK_F11
        (0x7B).toShort() to "F12", // VK_F12
        (0x7C).toShort() to "F13", // VK_F13
        (0x7D).toShort() to "F14", // VK_F14
        (0x7E).toShort() to "F15", // VK_F15
        (0x7F).toShort() to "F16", // VK_F16
        (0x80).toShort() to "F17", // VK_F17
        (0x81).toShort() to "F18", // VK_F18
        (0x82).toShort() to "F19", // VK_F19
        (0x83).toShort() to "F20", // VK_F20
        (0xB3).toShort() to "MediaPlayPause", // VK_MEDIA_PLAY_PAUSE
        (0xB2).toShort() to "MediaStop", // VK_MEDIA_STOP
        (0xB0).toShort() to "MediaTrackNext", // VK_MEDIA_NEXT_TRACK
        (0xB1).toShort() to "MediaTrackPrevious", // VK_MEDIA_PREV_TRACK
        (0xAE).toShort() to "AudioVolumeDown", // VK_VOLUME_DOWN
        (0xAD).toShort() to "AudioVolumeMute", // VK_VOLUME_MUTE
        (0xAF).toShort() to "AudioVolumeUp", // VK_VOLUME_UP
        (0xFB).toShort() to "ZoomToggle", // VK_ZOOM
        (0xB4).toShort() to "LaunchMail", // VK_LAUNCH_MAIL
        (0xB5).toShort() to "LaunchMediaPlayer", // VK_LAUNCH_MEDIA_SELECT
        (0xB6).toShort() to "LaunchApplication1", // VK_LAUNCH_APP1
        (0xB7).toShort() to "LaunchApplication2", // VK_LAUNCH_APP2
        (0xA6).toShort() to "BrowserBack", // VK_BROWSER_BACK
        (0xAB).toShort() to "BrowserFavorites", // VK_BROWSER_FAVORITES
        (0xA7).toShort() to "BrowserForward", // VK_BROWSER_FORWARD
        (0xAC).toShort() to "BrowserHome", // VK_BROWSER_HOME
        (0xA8).toShort() to "BrowserRefresh", // VK_BROWSER_REFRESH
        (0xAA).toShort() to "BrowserSearch", // VK_BROWSER_SEARCH
        (0xA9).toShort() to "BrowserStop", // VK_BROWSER_STOP
        (0x6E).toShort() to "Decimal", // VK_DECIMAL
        (0x6A).toShort() to "Multiply", // VK_MULTIPLY
        (0x6B).toShort() to "Add", // VK_ADD
        (0x6F).toShort() to "Divide", // VK_DIVIDE
        (0x6D).toShort() to "Subtract", // VK_SUBTRACT
        (0x6C).toShort() to "Separator", // VK_SEPARATOR
        (0x60).toShort() to "0", // VK_NUMPAD0
        (0x61).toShort() to "1",// VK_NUMPAD1
        (0x62).toShort() to "2",// VK_NUMPAD2
        (0x63).toShort() to "3",// VK_NUMPAD3
        (0x64).toShort() to "4",// VK_NUMPAD4
        (0x65).toShort() to "5",// VK_NUMPAD5
        (0x66).toShort() to "6",// VK_NUMPAD6
        (0x67).toShort() to "7",// VK_NUMPAD7
        (0x68).toShort() to "8",// VK_NUMPAD8
        (0x69).toShort() to "9",// VK_NUMPAD9
    )

    fun getName(keyCode: Short): String {
        return map[keyCode] ?: "Unidentified"
    }
}
