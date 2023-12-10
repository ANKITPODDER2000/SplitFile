import FileUtil.ROOT_DIR
import java.io.File

fun main() {
    val rootFile = File(ROOT_DIR)
    println("Root file Path : ${rootFile.path} isDirectory : ${rootFile.isDirectory}")
    FileUtil.makeBackupFolder()
    FileUtil.restoreBackup()
}