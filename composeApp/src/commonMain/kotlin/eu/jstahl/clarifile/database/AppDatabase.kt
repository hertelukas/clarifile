package eu.jstahl.clarifile.database

import androidx.room.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [FileEntity::class, TagEntity::class, FileTag::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class) // KMP specific
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
}

// This allows the KMP compiler to generate the platform-specific implementation
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
