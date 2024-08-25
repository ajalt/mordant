# Mordant JVM JNA

This is a JVM-only module that adds a `TerminalInterface` implementation that uses [JNA]. This
module supports all JDK versions, but links to a bundled native library, so it increases your JAR
size.

```kotlin
implementation("com.github.ajalt.mordant:mordant-jvm-jna:$mordantVersion")
```

[JNA]: https://github.com/java-native-access/jna
