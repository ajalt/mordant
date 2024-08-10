# Keep rules for those who are using ProGuard.

-keep class com.sun.jna.** { *; }
-keep class * implements com.sun.jna.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations,AnnotationDefault
