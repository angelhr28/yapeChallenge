package com.angelhr28.yapechallenge.data.storage

import android.content.Context
import com.angelhr28.yapechallenge.core.security.CryptoManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

/**
 * Gestor de archivos cifrados que almacena y recupera documentos
 * de forma segura usando [CryptoManager].
 *
 * @property context Contexto de la aplicacion para acceder al almacenamiento interno.
 * @property cryptoManager Administrador de cifrado/descifrado.
 */
class EncryptedFileManager(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    /** Directorio seguro dentro del almacenamiento interno de la aplicacion. */
    private val secureDir: File
        get() {
            val dir = File(context.filesDir, "secure_docs")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    /**
     * Cifra y guarda los datos en un archivo con nombre unico.
     *
     * @param data Bytes del archivo original.
     * @param extension Extension del archivo (ej. "pdf", "img").
     * @return Ruta absoluta del archivo cifrado.
     */
    fun saveEncryptedFile(data: ByteArray, extension: String): String {
        val fileName = "${UUID.randomUUID()}.$extension.enc"
        val file = File(secureDir, fileName)

        val inputStream = ByteArrayInputStream(data)
        file.outputStream().use { outputStream ->
            cryptoManager.encrypt(inputStream, outputStream)
        }

        return file.absolutePath
    }

    /**
     * Lee y descifra un archivo previamente cifrado.
     *
     * @param encryptedPath Ruta absoluta del archivo cifrado.
     * @return Bytes descifrados del archivo original.
     * @throws IllegalArgumentException Si el archivo no existe.
     */
    fun readDecryptedFile(encryptedPath: String): ByteArray {
        val file = File(encryptedPath)
        require(file.exists()) { "Encrypted file not found: $encryptedPath" }

        val outputStream = ByteArrayOutputStream()
        file.inputStream().use { inputStream ->
            cryptoManager.decrypt(inputStream, outputStream)
        }

        return outputStream.toByteArray()
    }

    /** Elimina el archivo cifrado en la ruta indicada, si existe. */
    fun deleteEncryptedFile(encryptedPath: String) {
        val file = File(encryptedPath)
        if (file.exists()) {
            file.delete()
        }
    }

    /** Retorna el tamano en bytes del arreglo de datos proporcionado. */
    fun getFileSize(data: ByteArray): Long = data.size.toLong()
}
