package com.angelhr28.yapechallenge.data.storage

import android.content.Context
import com.angelhr28.yapechallenge.core.security.CryptoManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class EncryptedFileManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    private val secureDir: File
        get() {
            val dir = File(context.filesDir, "secure_docs")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun saveEncryptedFile(data: ByteArray, extension: String): String {
        val fileName = "${UUID.randomUUID()}.$extension.enc"
        val file = File(secureDir, fileName)

        val inputStream = ByteArrayInputStream(data)
        file.outputStream().use { outputStream ->
            cryptoManager.encrypt(inputStream, outputStream)
        }

        return file.absolutePath
    }

    fun readDecryptedFile(encryptedPath: String): ByteArray {
        val file = File(encryptedPath)
        require(file.exists()) { "Encrypted file not found: $encryptedPath" }

        val outputStream = ByteArrayOutputStream()
        file.inputStream().use { inputStream ->
            cryptoManager.decrypt(inputStream, outputStream)
        }

        return outputStream.toByteArray()
    }

    fun deleteEncryptedFile(encryptedPath: String) {
        val file = File(encryptedPath)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getFileSize(data: ByteArray): Long = data.size.toLong()
}
