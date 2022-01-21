package yap.sslpinning

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
}