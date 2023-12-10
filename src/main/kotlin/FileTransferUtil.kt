import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

object FileTransferUtil {

    private const val MAX_FILE_SIZE_IN_MB = 0.25
    private const val BUFFER_SIZE = 1024 // 1 KB

    private fun isSplitRequired(file: File): Boolean {
        if (!file.exists() || !file.isFile) return false
        return file.length() > (MAX_FILE_SIZE_IN_MB * 1024 * 1024)
    }

    fun copyFile(srcFile: File, destDir: File, backupInfo: BackupInfo, needToStoreMetadata: Boolean = true): Boolean {
        if (!srcFile.exists() || !srcFile.isFile) {
            println("File not exists or it's not a file....")
            return false
        }

        if (!destDir.exists()) destDir.mkdirs()
        val destFile = File(destDir, srcFile.name)
        if (destFile.exists()) destFile.delete()

        val needToSplit = isSplitRequired(srcFile)

        if (!needToSplit) copyFileContent(srcFile, destFile, backupInfo, needToStoreMetadata)
        else copySplitFileContent(srcFile, destDir, backupInfo, needToStoreMetadata)

        return false
    }

    private fun copyFileContent(srcFile: File, destFile: File, backupInfo: BackupInfo, needToStoreMetadata: Boolean) {
        val fileInputStream = FileInputStream(srcFile)
        val fileOutputStream = FileOutputStream(destFile)
        try {
            val buffer = ByteArray(BUFFER_SIZE)
            var bufferRead: Int
            while (fileInputStream.read(buffer).also { bufferRead = it } != -1) {
                fileOutputStream.write(buffer, 0, bufferRead)
            }
            if (needToStoreMetadata) {
                val fileInfo = FileInfo(
                    destFile.path, false, destFile.name, destFile.nameWithoutExtension, destFile.length(), 0
                )
                backupInfo.backupFileList.add(fileInfo)
            }
        } catch (e: Exception) {
            println("Error : ${e.localizedMessage}")
        } finally {
            fileOutputStream.flush()
            fileOutputStream.close()
            fileInputStream.close()
        }
    }

    private fun copySplitFileContent(
        srcFile: File,
        destDir: File,
        backupInfo: BackupInfo,
        needToStoreMetadata: Boolean
    ) {
        val destSubDir = File(destDir, srcFile.nameWithoutExtension)
        if (!destSubDir.exists()) destSubDir.mkdirs()

        val fileInputStream = FileInputStream(srcFile)
        val srcFileNameWithOutExt = srcFile.nameWithoutExtension

        var totalWriteCompleted = 0
        val totalSize = srcFile.length()
        var count = 1L

        try {
            while (totalWriteCompleted < totalSize) {
                println("totalWriteCompleted : $totalWriteCompleted && totalSize : $totalSize")
                val outputFile = File(destSubDir, "$srcFileNameWithOutExt-${count}.data")
                val fos = FileOutputStream(outputFile)

                var fileWriteCompleted = 0
                val buffer = ByteArray(BUFFER_SIZE)
                var bufferRead: Int

                while (fileWriteCompleted < MAX_FILE_SIZE_IN_MB * 1024 * 1024) {
                    bufferRead = fileInputStream.read(buffer)
                    if (bufferRead != -1) {
                        fos.write(buffer, 0, bufferRead)
                        fileWriteCompleted += bufferRead
                        totalWriteCompleted += bufferRead
                    } else break
                }
                fos.flush()
                fos.close()
                count++
            }
            if (needToStoreMetadata) {
                val fileInfo = FileInfo(
                    destSubDir.path, true, srcFile.name, srcFile.nameWithoutExtension, srcFile.length(), count - 1
                )
                backupInfo.backupFileList.add(fileInfo)
            }
        } catch (e: Exception) {
            println("Error : ${e.localizedMessage}")
        } finally {
            fileInputStream.close()
        }
    }

    fun restoreFiles(restoreDir: File, backupInfo: BackupInfo) {
        for(fileInfo in backupInfo.backupFileList) {
            restoreFile(restoreDir, fileInfo)
        }
    }

    private fun restoreFile(restoreDir: File, fileInfo: FileInfo) {
        val fos = FileOutputStream(File(restoreDir, fileInfo.fileName))

        val buffer = ByteArray(BUFFER_SIZE)
        var bufferRead: Int

        if(fileInfo.isSplit) {
            for(i in 1 .. fileInfo.noOfSplit) {
                val fileInputStream = FileInputStream(File(fileInfo.filePath, "${fileInfo.fileNameWithOutExt}-$i.data"))
                while(fileInputStream.read(buffer).also { bufferRead = it } != -1) {
                    fos.write(buffer, 0, bufferRead)
                }
                fileInputStream.close()
            }
        }
        else {
            val fileInputStream = FileInputStream(fileInfo.filePath)
            while(fileInputStream.read(buffer).also { bufferRead = it } != -1) {
                fos.write(buffer, 0, bufferRead)
            }
            fileInputStream.close()
        }
        fos.flush()
        fos.close()
    }

}