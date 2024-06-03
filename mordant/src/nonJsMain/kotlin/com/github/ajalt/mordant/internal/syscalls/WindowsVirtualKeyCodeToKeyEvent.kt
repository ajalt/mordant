package com.github.ajalt.mordant.internal.syscalls

// https://learn.microsoft.com/en-us/windows/win32/inputdev/virtual-key-codes
// https://developer.mozilla.org/en-US/docs/Web/API/UI_Events/Keyboard_event_key_values
internal object WindowsVirtualKeyCodeToKeyEvent {
    private val map: Map<UShort, String> = mapOf(
        (0x12).toUShort() to "Alt", // VK_MENU
        (0x14).toUShort() to "CapsLock", // VK_CAPITAL
        (0x11).toUShort() to "Control", // VK_CONTROL
        (0x5B).toUShort() to "Meta", // VK_LWIN
        (0x90).toUShort() to "NumLock", // VK_NUMLOCK
        (0x91).toUShort() to "ScrollLock", // VK_SCROLL
        (0x10).toUShort() to "Shift", // VK_SHIFT
        (0x0D).toUShort() to "Enter", // VK_RETURN
        (0x09).toUShort() to "Tab", // VK_TAB
        (0x20).toUShort() to " ", // VK_SPACE
        (0x28).toUShort() to "ArrowDown", // VK_DOWN
        (0x25).toUShort() to "ArrowLeft", // VK_LEFT
        (0x27).toUShort() to "ArrowRight", // VK_RIGHT
        (0x26).toUShort() to "ArrowUp", // VK_UP
        (0x23).toUShort() to "End", // VK_END
        (0x24).toUShort() to "Home", // VK_HOME
        (0x22).toUShort() to "PageDown", // VK_NEXT
        (0x21).toUShort() to "PageUp", // VK_PRIOR
        (0x08).toUShort() to "Backspace", // VK_BACK
        (0x0C).toUShort() to "Clear", // VK_CLEAR
        (0xF7).toUShort() to "CrSel", // VK_CRSEL
        (0x2E).toUShort() to "Delete", // VK_DELETE
        (0xF9).toUShort() to "EraseEof", // VK_EREOF
        (0xF8).toUShort() to "ExSel", // VK_EXSEL
        (0x2D).toUShort() to "Insert", // VK_INSERT
        (0x1E).toUShort() to "Accept", // VK_ACCEPT
        (0xF0).toUShort() to "Attn", // VK_OEM_ATTN
        (0x5D).toUShort() to "ContextMenu", // VK_APPS
        (0x1B).toUShort() to "Escape", // VK_ESCAPE
        (0x2B).toUShort() to "Execute", // VK_EXECUTE
        (0xF1).toUShort() to "Finish", // VK_OEM_FINISH
        (0x2F).toUShort() to "Help", // VK_HELP
        (0x13).toUShort() to "Pause", // VK_PAUSE
        (0xFA).toUShort() to "Play", // VK_PLAY
        (0x29).toUShort() to "Select", // VK_SELECT
        (0x2C).toUShort() to "PrintScreen", // VK_SNAPSHOT
        (0x5F).toUShort() to "Standby", // VK_SLEEP
        (0xF0).toUShort() to "Alphanumeric", // VK_OEM_ATTN
        (0x1C).toUShort() to "Convert", // VK_CONVERT
        (0x18).toUShort() to "FinalMode", // VK_FINAL
        (0x1F).toUShort() to "ModeChange", // VK_MODECHANGE
        (0x1D).toUShort() to "NonConvert", // VK_NONCONVERT
        (0xE5).toUShort() to "Process", // VK_PROCESSKEY
        (0x15).toUShort() to "HangulMode", // VK_HANGUL
        (0x19).toUShort() to "HanjaMode", // VK_HANJA
        (0x17).toUShort() to "JunjaMode", // VK_JUNJA
        (0xF3).toUShort() to "Hankaku", // VK_OEM_AUTO
        (0xF2).toUShort() to "Hiragana", // VK_OEM_COPY
        (0x15).toUShort() to "KanaMode", // VK_KANA
        (0xF1).toUShort() to "Katakana", // VK_OEM_FINISH
        (0xF5).toUShort() to "Romaji", // VK_OEM_BACKTAB
        (0xF4).toUShort() to "Zenkaku", // VK_OEM_ENLW
        (0x70).toUShort() to "F1", // VK_F1
        (0x71).toUShort() to "F2", // VK_F2
        (0x72).toUShort() to "F3", // VK_F3
        (0x73).toUShort() to "F4", // VK_F4
        (0x74).toUShort() to "F5", // VK_F5
        (0x75).toUShort() to "F6", // VK_F6
        (0x76).toUShort() to "F7", // VK_F7
        (0x77).toUShort() to "F8", // VK_F8
        (0x78).toUShort() to "F9", // VK_F9
        (0x79).toUShort() to "F10", // VK_F10
        (0x7A).toUShort() to "F11", // VK_F11
        (0x7B).toUShort() to "F12", // VK_F12
        (0x7C).toUShort() to "F13", // VK_F13
        (0x7D).toUShort() to "F14", // VK_F14
        (0x7E).toUShort() to "F15", // VK_F15
        (0x7F).toUShort() to "F16", // VK_F16
        (0x80).toUShort() to "F17", // VK_F17
        (0x81).toUShort() to "F18", // VK_F18
        (0x82).toUShort() to "F19", // VK_F19
        (0x83).toUShort() to "F20", // VK_F20
        (0x84).toUShort() to "F21", // VK_F21
        (0x85).toUShort() to "F22", // VK_F22
        (0x86).toUShort() to "F23", // VK_F23
        (0x87).toUShort() to "F24", // VK_F24
        (0xB3).toUShort() to "MediaPlayPause", // VK_MEDIA_PLAY_PAUSE
        (0xB2).toUShort() to "MediaStop", // VK_MEDIA_STOP
        (0xB0).toUShort() to "MediaTrackNext", // VK_MEDIA_NEXT_TRACK
        (0xB1).toUShort() to "MediaTrackPrevious", // VK_MEDIA_PREV_TRACK
        (0xAE).toUShort() to "AudioVolumeDown", // VK_VOLUME_DOWN
        (0xAD).toUShort() to "AudioVolumeMute", // VK_VOLUME_MUTE
        (0xAF).toUShort() to "AudioVolumeUp", // VK_VOLUME_UP
        (0xFB).toUShort() to "ZoomToggle", // VK_ZOOM
        (0xB4).toUShort() to "LaunchMail", // VK_LAUNCH_MAIL
        (0xB5).toUShort() to "LaunchMediaPlayer", // VK_LAUNCH_MEDIA_SELECT
        (0xB6).toUShort() to "LaunchApplication1", // VK_LAUNCH_APP1
        (0xB7).toUShort() to "LaunchApplication2", // VK_LAUNCH_APP2
        (0xA6).toUShort() to "BrowserBack", // VK_BROWSER_BACK
        (0xAB).toUShort() to "BrowserFavorites", // VK_BROWSER_FAVORITES
        (0xA7).toUShort() to "BrowserForward", // VK_BROWSER_FORWARD
        (0xAC).toUShort() to "BrowserHome", // VK_BROWSER_HOME
        (0xA8).toUShort() to "BrowserRefresh", // VK_BROWSER_REFRESH
        (0xAA).toUShort() to "BrowserSearch", // VK_BROWSER_SEARCH
        (0xA9).toUShort() to "BrowserStop", // VK_BROWSER_STOP
        (0x6E).toUShort() to "Decimal", // VK_DECIMAL
        (0x6A).toUShort() to "Multiply", // VK_MULTIPLY
        (0x6B).toUShort() to "Add", // VK_ADD
        (0x6F).toUShort() to "Divide", // VK_DIVIDE
        (0x6D).toUShort() to "Subtract", // VK_SUBTRACT
        (0x6C).toUShort() to "Separator", // VK_SEPARATOR
        (0x30).toUShort() to "0",
        (0x31).toUShort() to "1",
        (0x32).toUShort() to "2",
        (0x33).toUShort() to "3",
        (0x34).toUShort() to "4",
        (0x35).toUShort() to "5",
        (0x36).toUShort() to "6",
        (0x37).toUShort() to "7",
        (0x38).toUShort() to "8",
        (0x39).toUShort() to "9",
        (0x60).toUShort() to "0", // VK_NUMPAD0
        (0x61).toUShort() to "1",// VK_NUMPAD1
        (0x62).toUShort() to "2",// VK_NUMPAD2
        (0x63).toUShort() to "3",// VK_NUMPAD3
        (0x64).toUShort() to "4",// VK_NUMPAD4
        (0x65).toUShort() to "5",// VK_NUMPAD5
        (0x66).toUShort() to "6",// VK_NUMPAD6
        (0x67).toUShort() to "7",// VK_NUMPAD7
        (0x68).toUShort() to "8",// VK_NUMPAD8
        (0x69).toUShort() to "9",// VK_NUMPAD9
        (0x41).toUShort() to "a",
        (0x42).toUShort() to "b",
        (0x43).toUShort() to "c",
        (0x44).toUShort() to "d",
        (0x45).toUShort() to "e",
        (0x46).toUShort() to "f",
        (0x47).toUShort() to "g",
        (0x48).toUShort() to "h",
        (0x49).toUShort() to "i",
        (0x4A).toUShort() to "j",
        (0x4B).toUShort() to "k",
        (0x4C).toUShort() to "l",
        (0x4D).toUShort() to "m",
        (0x4E).toUShort() to "n",
        (0x4F).toUShort() to "o",
        (0x50).toUShort() to "p",
        (0x51).toUShort() to "q",
        (0x52).toUShort() to "r",
        (0x53).toUShort() to "s",
        (0x54).toUShort() to "t",
        (0x55).toUShort() to "u",
        (0x56).toUShort() to "v",
        (0x57).toUShort() to "w",
        (0x58).toUShort() to "x",
        (0x59).toUShort() to "y",
        (0x5A).toUShort() to "z",
    )

    fun getName(keyCode: UShort): String {
        return map[keyCode] ?: "Unidentified"
    }
}
