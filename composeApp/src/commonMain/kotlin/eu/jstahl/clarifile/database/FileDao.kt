package eu.jstahl.clarifile.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFileTagCrossRef(crossRef: FileTag)

    @Query("SELECT * FROM tags WHERE content = :content LIMIT 1")
    suspend fun getTagByContent(content: String): TagEntity?

    // The Transaction annotation is required because this runs two queries atomically
    @Transaction
    @Query("SELECT * FROM files")
    fun getAllFilesWithTags(): Flow<List<FileWithTags>>

    @Transaction
    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getFileWithTags(id: Int): FileWithTags?
}