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

        // 1. Build the Database
        // We use the standard Android Builder and pass it to your common configuration function
        val builder = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "clarifile.db"
        )

        // 2. Create dependencies
        // getRoomDatabase(builder) must be your common function returning AppDatabase
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

// Note: The Preview is commented out because 'Storage' requires a real Database/File system.
// To fix this, you would need to extract an interface (IStorage) and create a FakeStorage for previews.
/*
@Preview
@Composable
fun AppAndroidPreview() {
    App(Storage(..., ...))
}
*/