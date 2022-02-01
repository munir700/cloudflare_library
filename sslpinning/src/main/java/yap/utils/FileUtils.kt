package yap.utils

import android.content.Context
import java.io.*

object FileUtils {

    @Throws(Exception::class)
    fun readFile(file: File): ByteArray {
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(
            FileInputStream(file)
        )

        inputBuffer.read(fileContents)
        inputBuffer.close()

        return fileContents
    }


    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, file: File) {
        val bos = BufferedOutputStream(FileOutputStream(file, false))
        bos.write(fileData)
        bos.flush()
        bos.close()
    }

    fun Context.createFilePath(): File {
        val cloudflareCertificates = "cloudflare_cer.cer"
        val filesDirectory: String = this.filesDir.absolutePath
        val filePath = filesDirectory + File.separator + cloudflareCertificates
        return File(filePath)
    }
}