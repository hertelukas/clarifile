package eu.jstahl.clarifile

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import androidx.room.Room
import androidx.room.RoomDatabase
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.database.AppDatabase
import eu.jstahl.clarifile.database.getRoomDatabase
import eu.jstahl.clarifile.frontend.App
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // Sandbox root
    val dbFilePath = NSHomeDirectory() + "/clarifile.db"

    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}

fun MainViewController() = ComposeUIViewController {
    val database = remember {
        val builder = getDatabaseBuilder()

        getRoomDatabase(builder)
    }

    val dao = remember { database.fileDao() }

    // 3. Inject dependencies (DAO + iOS File Storage)
    val storage = remember {
        Storage(dao, IosFileStorage())
    }

    App(storage)
}