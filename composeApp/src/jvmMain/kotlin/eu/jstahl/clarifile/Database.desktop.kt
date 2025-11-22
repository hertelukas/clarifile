package eu.jstahl.clarifile

import androidx.room.Room
import androidx.room.RoomDatabase
import eu.jstahl.clarifile.database.AppDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "clarifile.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}
