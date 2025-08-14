package com.egorhoot.chomba.utils

import android.content.Context
import android.graphics.Bitmap
import javax.inject.Inject
import android.util.Base64
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Encryptor @Inject constructor(
    private val secretKey: String
){
    fun encrypt(text: String): String {
        val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        val keySpec = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun getUserQRCode(userUid: String, size: Int, color: Color, backColor: Color): Bitmap {
        val secretText = encrypt(userUid)
        return generateQRCode(secretText, size, color, backColor)
    }

    private fun generateQRCode(text: String, size: Int, color: Color, backColor: Color): Bitmap {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap[x, y] = if (bitMatrix.get(
                        x,
                        y
                    )
                ) color.toArgb() else backColor.toArgb()
            }
        }
        return bitmap
    }

}