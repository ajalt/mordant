# Changelog

## Unreleased

## 2.1.0
### Added
- Added `ConfirmationPrompt` that asks the user to enter the same value twice, which is commonly used for password inputs.

## 2.0.1
### Added
- Include metadata in JVM jars to support GraalVM native-image.

### Fixed
- Fix animations printing an extra frame after `stop` is called when running in the IntelliJ console. [(#105)](https://github.com/ajalt/mordant/issues/105)

## 2.0.0
### Deprecated
- Deprecated `TerminalColors` is favor of `TextColors` and `Terminal.theme`


## 2.0.0-beta14
### Added
- `Terminal.rawPrint` which allows you to print ANSI codes manually. [(#91)](https://github.com/ajalt/mordant/issues/91)
- Option to disable trailing line breaks on animations.
- `Terminal.print`, `println`, and `rawPrint` now accept a `stderr` parameter that will print to stderr (if available). 

### Changed
- Fix typo in enum name: renamed `Borders.TOM_BOTTOM` to `Borders.TOP_BOTTOM`. [(#100)](https://github.com/ajalt/mordant/issues/100)
- The terminal cursor will now be hidden when `progressAnimation` is running.

### Removed
- Removed `TerminalRecorder.currentContent`. Use `stdout()`, `stderr()` or `output()` instead.
- Removed `Terminal.forStdErr()` and `TerminalInterface.forStdErr()`. Use `Terminal.println(stderr=true)` instead.

## 2.0.0-beta13
### Added
- Add `Animation.stop()` to stop an animation without clearing it. [(#95)](https://github.com/ajalt/mordant/issues/95)
- Animations now support resuming after a call to `stop` or `clear`. [(#94)](https://github.com/ajalt/mordant/issues/94)
- `TextStyles.reset`, `TextStyles.resetForeground`, and `TextStyles.resetBackground` to clear existing styles.

### Fixed
- Fix `TerminalInfo.interactive` not including `outputInteractive`
- Fix prompts on JS targets that were broken by KT-55817 

### Changed
- **Source-incompatible change:** All boolean fields on `TextStyle` are now nullable. A null field indicates no change to the previous value when adding or nesting styles.
- `verticalLayout{}` now defaults `TextAlign.NONE`, meaning it won't add any trailing whitespace to lines. You can return to the old behavior with `align = TextAlign.LEFT`.
- When nesting styles, the outer style will now override inner styles at the start of a string. (e.g. `red(blue("x")) == red("x")`) 
- Definition List terms and entries can now be empty

## 2.0.0-beta12
### Fixed
- Switch back to calling `stty` for `detectTerminalSize` on macOS. [(#86)](https://github.com/ajalt/mordant/issues/86)
- `OverflowWrap` is now properly ignored when using a non-wrapping `Whitespace` value. 

## 2.0.0-beta11
### Added
- Tables and other layouts can now be completely empty [(#82)](https://github.com/ajalt/mordant/issues/82)

### Changed
- Update Kotlin to 1.8.0

### Removed
- Removed JS/Legacy publication. The JS target only publishes artifacts built with the IR compiler.

## 2.0.0-beta10
### Changed
- JVM: terminal detection now uses JNA to call kernel functions directly. 
- Interactive state of stdin and stdout are now detected separately. 
- Terminal size detection is now fast since it does not need a subprocess.

### Fixed
- Detect terminal correctly when running in the IntelliJ terminal tab, but not through a run action. [(#76)](https://github.com/ajalt/mordant/issues/76)

### Deprecated 
- `timeoutMs` parameter to `TerminalDetection.updateTerminalSize`. This function is now fast on all platforms. 

## 2.0.0-beta9
### Changed
- Stop stripping trailing newline from text when using `Whitespace.PRE` [(#75)](https://github.com/ajalt/mordant/issues/75)


## 2.0.0-beta8
### Added
- Implemented `hideInput` for prompts on native targets [(#63)](https://github.com/ajalt/mordant/issues/63)
- Improve cell-width calculation for emoji sequences like skin tone modifiers [(#64)](https://github.com/ajalt/mordant/issues/64)
- Added `Theme.plus` to combine two themes
- Added `Padding.plus` to combine two padding values

### Changed
- Replaced most of the `Padding` constructor and `Widget.withPadding` overloads with a unified builder interface
- Renamed the top level `row` and `column` builders to `horizonalLayout` and `verticalLayout`, respectively
- Update Kotlin to 1.7.20
- Kotlin/Native: use new default memory manager. Objects are no longer frozen.

### Removed 
- Removed `buildWidget`. Use `horizonalLayout` and `verticalLayout` instead.

### Fixed
- Terminal detection would sometimes incorrectly identify the process as running in IntelliJ [(#72)](https://github.com/ajalt/mordant/issues/72)
- `updateTerminalSize` would sometimes fail to find the `stty` command [(#66)](https://github.com/ajalt/mordant/issues/66)

## 2.0.0-beta7
### Added
- Functionality for reading user input: `Terminal.readLineOrNull`, `Terminal.prompt` and various `Prompt` classes
- `TerminalRecorder` that saves output to memory rather than printing it.
- `TerminalRecorder.outputAsHtml()` that can render recorded output as an html file.
- 
### Changed
- When building tables, `borders` has been renamed `cellBorders`, and `outerBorder: Boolean` has been replaced with `tableBorders: Borders?`, which allows more control over the table's outside borders. [(#58)](https://github.com/ajalt/mordant/issues/58)
- Update Kotlin to 1.7.0

### Fixed
- Avoid clobbering output when using `Terminal.forStdErr` while an animation is running. [(#54)](https://github.com/ajalt/mordant/issues/54)

### Deprecated
- Deprecated the `VirtualTerminalInterface`. Use `TerminalRecorder` instead.

## 2.0.0-beta6
### Changed
- Update Kotlin to 1.6.20
- Publish JS target with the IR format in addition to LEGACY

### Fixed
- Fix race condition when using ProgressAnimation and adding interceptors in JVM [(#55)](https://github.com/ajalt/mordant/issues/55)

## 2.0.0-beta5
### Added
- Progress bars and other single-line animations are now supported in the IntelliJ console [(#49)](https://github.com/ajalt/mordant/issues/49)
- Added `bottomTitle` to `Panel`
- `Terminal.forStdErr` for printing to stderr rather than stdout
- Add `macosArm64` target for native M1 macs

### Changed
- Update Kotlin to 1.6.10
- *Breaking change*: Renamed `Table` and `Panel`'s `borderStyle` property to `borderType` and `borderTextStyle` to `borderStyle`
- *Breaking change*: Renamed `TerminalInfo`'s `stdinInteractive` and `stdoutInteractive` to `inputInteractive` and `outputInteractive`, respectively

### Fixed
- Fix regression in clearing animations [(#48)](https://github.com/ajalt/mordant/issues/48)

## 2.0.0-beta4
### Added
- `Spinner` widget that displays a looping animation
- `EmptyEidget` widget that can be used as a placeholder in layouts
- `row{}` and `column{}` widget layouts that create a single row/column of widgets

### Fixed
- Reduced flickering on high frame rate animations

## 2.0.0-beta3
### Changed
- Update Kotlin to 1.5.31
- Update Colormath to 3.0. If you use and colormath colors directly, you may need to update your imports.

### Fixed
- Fixed exception thrown when parsing markdown tables with empty cells
- Fixed rendering of markdown image reference links and link content

## 2.0.0-beta2
### Added
- Published artifacts for macOS

### Changed
- Update Kotlin to 1.5.10
- All text instances and print functions now default to preformatted whitespace, meaning that spaces and newlines will be preserved. You can explicitly pass `Whitespace.NORMAL` to restore the previous behavior.

## 2.0.0-beta1
### Added
- `Table.contentToCsv` to render a table's cells to csv format
- Added support for JavaScript and linux native targets 
- Getter properties for standard theme styles

### Changed
- Update Kotlin to 1.4.31
- Improve terminal capabilities detection

## 2.0.0-alpha2
### Added
- `Terminal.progressAnimation` builder to create a customizable progress bar animation
- Improved cursor APIs and added ability to produce cursor ANSI codes as a string
- Add ability to override detected terminal interactivity separately from the ANSI capabilities  [(#7)](https://github.com/ajalt/mordant/issues/7)

### Changed
- Rework theming system to simplify customization

## 2.0.0-alpha1
Mordant 2.0 is a rewrite that retains the simple APIs of Mordant 1.0, and adds support for rendering
complex widgets.

### Added
- Added renderable widgets, including tables, panels, and lists
- Added markdown rendering
- Added a theme system to customize text styles on an entire terminal instance
- Added animations that automatically clear the previous frame when redrawing

### Changed
- Improved terminal capability detection
- ANSI colors and styles can now be applied through the `TextColors` and `TextStyles` top-level
  objects, and `Terminal.print` will downsample th resulting strings based on the detected terminal
  capabilities.

## 1.2.1
### Changed
- Improve support for color detection in IntelliJ and VS Code terminals

## 1.2.0
### Added
- Add functions for generating ANSI cursor movement
- Add ability to generate ANSI color codes from any colormath color object
- Update colormath to 1.2.0

## 1.1.0
### Added
- Add support for XYZ and LAB color spaces

## 1.0.0
- Initial Release
