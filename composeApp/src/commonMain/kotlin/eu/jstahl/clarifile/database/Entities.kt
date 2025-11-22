package eu.jstahl.clarifile.database

import androidx.room.*

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String
)

@Entity(
    tableName = "file_tags",
    primaryKeys = ["fileId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = FileEntity::class, parentColumns = ["id"], childColumns = ["fileId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("fileId"), Index("tagId")] // Indexing is critical for join performance
)
data class FileTag(
    val fileId: Int,
    val tagId: Int
)

data class FileWithTags(
    @Embedded val file: FileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = FileTag::class,
            parentColumn = "fileId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)