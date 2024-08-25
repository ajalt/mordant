# Mordant JVM FFM

This is a JVM-only module that adds a `TerminalInterface` implementation that uses the Java Foreign
Functions and Memory APIs. It requires JDK 22 or newer, and, like all usages of FFM, requires that
you add `--enable-native-access=ALL-UNNAMED` to your `java` command line arguments or
`Enable-Native-Access: ALL-UNNAMED` to the manifest of your executable JAR.

```kotlin
implementation("com.github.ajalt.mordant:mordant-jvm-ffm:$mordantVersion")
```
