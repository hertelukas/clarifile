package eu.jstahl.clarifile

import eu.jstahl.clarifile.backend.FileStorage
import eu.jstahl.clarifile.backend.GeoLocation
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject

class IosFileStorage : FileStorage {
    private val fileManager = NSFileManager.defaultManager

    // Load the app Documents directory (sandbox)
    private val documentsUrl: NSURL?
        get() = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).firstOrNull() as? NSURL

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun saveFile(sourcePath: String, id: Long) {
        val docs = documentsUrl ?: return

        // Create Folder with id
        val folderUrl = docs.URLByAppendingPathComponent(id.toString()) ?: return

        // Check if folder exists, if not create it
        if (!fileManager.fileExistsAtPath(folderUrl.path!!)) {
            fileManager.createDirectoryAtURL(
                folderUrl,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }

        // Calcualte Source and Dest
        // sourcePath comes from FileKit - raw path string
        val sourceUrl = NSURL.fileURLWithPath(sourcePath)
        // Use the filename from the source
        val fileName = sourceUrl.lastPathComponent ?: "file"
        val destUrl = folderUrl.URLByAppendingPathComponent(fileName) ?: return

        // Copy
        if (fileManager.fileExistsAtPath(destUrl.path!!)) {
            fileManager.removeItemAtURL(destUrl, null)
        }

        fileManager.copyItemAtURL(sourceUrl, destUrl, null)
    }

    @OptIn(ExperimentalForeignApi::class)

    override fun getAbsolutePath(id: Long): String {
        val docs = documentsUrl ?: return ""
        val folderUrl = docs.URLByAppendingPathComponent(id.toString()) ?: return ""

        // List contents of directory
        val contents = fileManager.contentsOfDirectoryAtURL(
            folderUrl,
            includingPropertiesForKeys = null,
            options = 0u,
            error = null
        )

        // Return first file path found
        return (contents?.firstOrNull() as? NSURL)?.path ?: ""
    }

    override fun open(id: Long) {
        val path = getAbsolutePath(id)
        if (path.isEmpty()) return

        val url = NSURL.fileURLWithPath(path)

        // Used to preview files
        val controller = UIDocumentInteractionController.interactionControllerWithURL(url)

        // Delegate to handle the preview
        val delegate = object : NSObject(), UIDocumentInteractionControllerDelegateProtocol {
            override fun documentInteractionControllerViewControllerForPreview(
                controller: UIDocumentInteractionController
            ): UIViewController {
                // Hack to get the root view controller
                return UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: UIViewController()
            }
        }

        controller.delegate = delegate
        controller.presentPreviewAnimated(true)
    }

    // Metadata extractor only supported for the JVM as of now
    override fun getGpsLocation(id: Long): GeoLocation? {
        return null
    }
}