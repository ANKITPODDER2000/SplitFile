import com.google.gson.Gson
import java.lang.Exception

class BackupInfo private constructor() {
    var backupPath: String = ""
    val backupFileList = arrayListOf<FileInfo>()

    companion object {
        private var sInstance: BackupInfo? = null
        private val gson = Gson()
        fun getInstance(): BackupInfo {
            if (sInstance == null) sInstance = BackupInfo()
            return sInstance!!
        }

        fun fromJson(json: String): BackupInfo {
            return try {
                gson.fromJson(json, BackupInfo::class.java)
            } catch (e: Exception) {
                BackupInfo()
            }
        }
    }

    fun toJson(): String {
        return gson.toJson(this)
    }

}

data class FileInfo(
    val filePath: String,
    val isSplit: Boolean,
    val fileName: String,
    val fileNameWithOutExt: String,
    val fileSize: Long,
    val noOfSplit: Long
)