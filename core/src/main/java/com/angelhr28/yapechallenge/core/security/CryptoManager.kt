package com.angelhr28.yapechallenge.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Gestor de cifrado que utiliza AES-256-GCM a traves del Android KeyStore.
 */
class CryptoManager {

    companion object {
        private const val KEYSTORE_TYPE = "AndroidKeyStore"
        private const val KEY_ALIAS = "yape_challenge_aes256_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
    }

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_TYPE).apply { load(null) }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_TYPE
        )
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }

    /**
     * Cifra datos de un flujo de entrada y escribe el resultado en el flujo de salida.
     *
     * @param inputStream flujo con los datos en texto plano.
     * @param outputStream flujo donde se escriben los datos cifrados.
     */
    fun encrypt(inputStream: InputStream, outputStream: OutputStream) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        outputStream.write(iv.size)
        outputStream.write(iv)

        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            val encryptedChunk = cipher.update(buffer, 0, bytesRead)
            if (encryptedChunk != null) {
                outputStream.write(encryptedChunk)
            }
        }
        val finalChunk = cipher.doFinal()
        if (finalChunk != null) {
            outputStream.write(finalChunk)
        }
        outputStream.flush()
    }

    /**
     * Descifra datos de un flujo de entrada y escribe el resultado en el flujo de salida.
     *
     * @param inputStream flujo con los datos cifrados.
     * @param outputStream flujo donde se escriben los datos descifrados.
     */
    fun decrypt(inputStream: InputStream, outputStream: OutputStream) {
        val ivSize = inputStream.read()
        val iv = ByteArray(ivSize)
        inputStream.read(iv)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

        val encryptedBytes = inputStream.readBytes()
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        outputStream.write(decryptedBytes)
        outputStream.flush()
    }

    /**
     * Cifra un arreglo de bytes y retorna el resultado incluyendo el IV.
     *
     * @param data datos en texto plano a cifrar.
     * @return arreglo de bytes con IV + datos cifrados.
     */
    fun encryptBytes(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)

        return ByteArray(1 + iv.size + encrypted.size).apply {
            this[0] = iv.size.toByte()
            iv.copyInto(this, 1)
            encrypted.copyInto(this, 1 + iv.size)
        }
    }

    /**
     * Descifra un arreglo de bytes previamente cifrado con [encryptBytes].
     *
     * @param data datos cifrados que incluyen el IV.
     * @return arreglo de bytes descifrados.
     */
    fun decryptBytes(data: ByteArray): ByteArray {
        val ivSize = data[0].toInt()
        val iv = data.copyOfRange(1, 1 + ivSize)
        val encrypted = data.copyOfRange(1 + ivSize, data.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

        return cipher.doFinal(encrypted)
    }
}
