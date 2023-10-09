# Keep rules for those who are using ProGuard.

-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keepattributes *Annotation*
-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.core.annotate.Delete
