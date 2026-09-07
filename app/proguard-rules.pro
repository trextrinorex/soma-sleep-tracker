# Somna ProGuard rules
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Keep Room entities and DAOs
-keep class com.somna.sleeptracker.data.local.entity.** { *; }
-keep class com.somna.sleeptracker.data.local.dao.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
