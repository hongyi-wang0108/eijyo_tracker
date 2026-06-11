# Keep kotlinx.serialization metadata for serialized data classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.eijyo.tracker.** {
    kotlinx.serialization.KSerializer serializer(...);
}
