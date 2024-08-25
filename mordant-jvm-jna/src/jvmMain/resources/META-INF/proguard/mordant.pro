# Keep rules for those who are using ProGuard.

-dontwarn org.graalvm.**
-dontwarn com.oracle.svm.core.annotate.**
-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,AnnotationDefault
