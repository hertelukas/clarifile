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

    @Transaction
    @Query("SELECT id FROM files")
    fun getAllFiles(): Flow<List<Long>>

    // 2. OR Logic: Files that match at least one tag
    @Transaction
    @Query("""
        SELECT DISTINCT f.id FROM files f
        INNER JOIN file_tags ft ON f.id = ft.fileId
        INNER JOIN tags t ON ft.tagId = t.id
        WHERE t.content IN (:tags)
    """)
    fun getFilesByAnyTag(tags: List<String>): Flow<List<Long>>

    // 3. AND Logic: Files that have ALL specified tags
    // We filter for files containing the tags, group by file,
    // and ensure the count of matches equals the number of requested tags.
    @Transaction
    @Query("""
        SELECT f.id FROM files f
        INNER JOIN file_tags ft ON f.id = ft.fileId
        INNER JOIN tags t ON ft.tagId = t.id
        WHERE t.content IN (:tags)
        GROUP BY f.id
        HAVING COUNT(DISTINCT t.content) = :tagCount
    """)
    fun getFilesByAllTags(tags: List<String>, tagCount: Int): Flow<List<Long>>

    @Transaction
    @Query("SELECT f.name FROM files f WHERE id = :id")
    suspend fun getFileNameByID(id: Long): String

}