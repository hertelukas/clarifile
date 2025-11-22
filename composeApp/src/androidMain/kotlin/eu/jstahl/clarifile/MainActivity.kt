package eu.jstahl.clarifile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import eu.jstahl.clarifile.backend.Storage
import eu.jstahl.clarifile.database.AppDatabase
import eu.jstahl.clarifile.database.getRoomDatabase
import eu.jstahl.clarifile.frontend.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val builder = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clarifile.db"
        )


        val database = getRoomDatabase(builder)
        val dao = database.fileDao()
        val fileStorage = AndroidFileStorage(applicationContext)

        val storage = Storage(dao, fileStorage)

        setContent {
            // 3. Pass Storage to the App
            App(storage)
        }
    }
}

