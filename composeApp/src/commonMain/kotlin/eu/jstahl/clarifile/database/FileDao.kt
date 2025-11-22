package eu.jstahl.clarifile.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFile(file: FileEntity): Long

    // The Transaction annotation is required because this runs two queries atomically
    @Transaction
    @Query("SELECT * FROM files")
    abstract fun getAllFilesWithTags(): Flow<List<FileWithTags>>

    @Transaction
    @Query("SELECT * FROM files WHERE id = :id")
    abstract suspend fun getFileWithTags(id: Long): FileWithTags?

    @Transaction
    @Query("SELECT id FROM files")
    abstract fun getAllFiles(): Flow<List<Long>>

    // 2. OR Logic: Files that match at least one tag
    @Transaction
    @Query("""
        SELECT DISTINCT f.id FROM files f
        INNER JOIN file_tags ft ON f.id = ft.fileId
        INNER JOIN tags t ON ft.tagId = t.id
        WHERE t.content IN (:tags)
    """)
    abstract fun getFilesByAnyTag(tags: List<String>): Flow<List<Long>>

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
    abstract fun getFilesByAllTags(tags: List<String>, tagCount: Int): Flow<List<Long>>

    @Transaction
    @Query("SELECT f.name FROM files f WHERE id = :id")
    abstract suspend fun getFileNameByID(id: Long): String

    @Transaction
    @Query("UPDATE files SET name = :newName WHERE id = :id")
    abstract suspend fun updateNameByID(id: Long, newName: String)

    @Transaction
    @Query("SELECT t.content FROM files f INNER JOIN file_tags ft ON f.id = ft.fileId INNER JOIN tags t ON ft.tagId = t.id WHERE f.id = :fileId ")
    abstract fun getFileTags(fileId: Long): Flow<List<String>>

    @Query("SELECT * FROM tags WHERE content = :content LIMIT 1")
    abstract suspend fun getTagByContent(content: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertFileTagCrossRef(crossRef: FileTag)

    @Query("DELETE FROM file_tags WHERE fileId = :fileId")
    abstract suspend fun removeTags(fileId: Long)

    @Transaction
    open suspend fun addTagToFile(fileId: Long, tagContent: String) {
        // 1. Check if tag exists
        var tagId = getTagByContent(tagContent)?.id

        // 2. If not, create it
        if (tagId == null) {
            // insertTag returns the new Row ID (Long)
            tagId = insertTag(TagEntity(content = tagContent))
        }

        // 3. Create the relation
        // Note: Casting Long to Int because your Entities use Int IDs
        insertFileTagCrossRef(FileTag(fileId = fileId, tagId = tagId))
    }
}