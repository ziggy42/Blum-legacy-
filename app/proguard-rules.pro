# Twitter4j
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

# Search
-keep class android.support.v7.widget.SearchView { *; }

# JSOUP
-keep class org.jsoup.** { *; }
-keep interface org.jsoup.** { *; }


# Workaround for play-services-ads
-keep class com.google.android.gms.** { *; }
-keep public class com.google.android.gms.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-dontwarn com.google.android.gms.**