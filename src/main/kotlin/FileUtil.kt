import FileTransferUtil.copyFile
import java.io.File
import java.io.FileOutputStream
import kotlin.math.round

object FileUtil {
    const val ROOT_DIR = "D:\\Code Practice\\Kotlin\\SplitFile\\FileTransfer"
    private const val BACKUP_DIR_NAME = "Backup"
    private const val RESTORE_DIR_NAME = "Restore"
    private const val METADATA_FILE_NAME = "metadata.json"

    private val availableFiles = arrayListOf<File>()

    private fun getAllFiles(file: File, printLog: Boolean = false) {
        if (!file.exists()) return
        if (file.name == BACKUP_DIR_NAME || file.name == RESTORE_DIR_NAME) return

        if (file.isDirectory) {
            file.listFiles()?.let {
                for (childFile in it) {
                    if (childFile.isFile) storeFileInfo(childFile, printLog)
                    else getAllFiles(childFile)
                }
            }
        } else if (file.isFile) storeFileInfo(file, printLog)
    }

    private fun storeFileInfo(file: File, printLog: Boolean) {
        availableFiles.add(file)
        if (printLog)
            println(
                "File name : ${file.name} name with out ext : ${file.nameWithoutExtension} size : ${
                    round(
                        file.length().toDouble() / (1024 * 1024)
                    )
                } mb..."
            )
    }

    fun makeBackupFolder() {
        val rootFile = File(ROOT_DIR)
        getAllFiles(rootFile)
        val backupFile = File(ROOT_DIR, BACKUP_DIR_NAME)
        if (backupFile.exists())
            backupFile.deleteRecursively()

        val backupInfo = BackupInfo.getInstance()
        backupInfo.backupPath = backupFile.path


        availableFiles.forEach {
            copyFile(it, backupFile, backupInfo)
        }
        val metadataFile = writeMetaDataFile(backupInfo.toJson())
        copyFile(metadataFile, backupFile, backupInfo, false)
        File(ROOT_DIR, METADATA_FILE_NAME).deleteOnExit()
    }

    private fun writeMetaDataFile(str: String): File {
        val metadataFile = File(ROOT_DIR, METADATA_FILE_NAME)
        if(metadataFile.exists()) metadataFile.delete()

        val fos = FileOutputStream(metadataFile)
        fos.write(str.toByteArray())

        fos.flush()
        fos.close()

        return metadataFile
    }

    fun restoreBackup() {
        val backupFile = File(ROOT_DIR, BACKUP_DIR_NAME)
        if(!backupFile.exists()) {
            println("Backup dir not exist....")
            return
        }

        val metadataFile = File(backupFile.path, METADATA_FILE_NAME)
        if(!metadataFile.exists()) {
            println("Metadata file not exist.....")
            return
        }

        val jsonData = readJsonContent(metadataFile)
        val backupInfo = BackupInfo.fromJson(jsonData)

        val restoreDir = File(ROOT_DIR, RESTORE_DIR_NAME)
        if(!restoreDir.exists()) restoreDir.mkdirs()

        FileTransferUtil.restoreFiles(restoreDir, backupInfo)
    }

    private fun readJsonContent(file: File): String {
        return file.readText()
    }
}