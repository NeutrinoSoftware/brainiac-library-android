-target 1.7

-allowaccessmodification
-useuniqueclassmembernames

# Crashlytics
-keepattributes SourceFile, LineNumberTable
-keep class com.crashlytics.android.**

#Keep Otto
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

#MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn io.realm.**

#materialtabstrip
-dontwarn sun.misc.Unsafe
-dontwarn sun.misc.Cleaner
-dontwarn com.google.common.collect.MinMaxPriorityQueue