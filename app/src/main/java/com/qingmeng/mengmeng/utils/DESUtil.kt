package com.qingmeng.mengmeng.utils


import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import javax.crypto.*
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec


object DESUtil {

    //加密方式1
    fun encrypt(message: String, key: String): String {
        var result = ""
        try {
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")

            val desKeySpec = DESKeySpec(key.toByteArray(charset("UTF-8")))

            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secretKey = keyFactory.generateSecret(desKeySpec)
            val iv = IvParameterSpec(key.toByteArray(charset("UTF-8")))
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)
            result = toHexString(cipher.doFinal(message.toByteArray(charset("UTF-8"))))
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

        return result
    }

    //解密方式1
    fun decrypt(message: String, key: String): String {
        var result = ""
        try {
            val bytesrc = convertHexString(message)
            val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
            val desKeySpec = DESKeySpec(key.toByteArray(charset("UTF-8")))
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val secretKey = keyFactory.generateSecret(desKeySpec)
            val iv = IvParameterSpec(key.toByteArray(charset("UTF-8")))
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
            val retByte = cipher.doFinal(bytesrc)
            result = String(retByte)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

        return result
    }

    fun convertHexString(ss: String): ByteArray {
        val digest = ByteArray(ss.length / 2)
        for (i in digest.indices) {
            val byteString = ss.substring(2 * i, 2 * i + 2)
            val byteValue = Integer.parseInt(byteString, 16)
            digest[i] = byteValue.toByte()
        }

        return digest
    }

    fun toHexString(b: ByteArray): String {
        val hexString = StringBuffer()
        for (i in b.indices) {
            var plainText = Integer.toHexString(0xff and b[i].toInt())
            if (plainText.length < 2) {
                plainText = "0$plainText"
            }
            hexString.append(plainText)
        }
        return hexString.toString()
    }

    //加密方式2
    fun encryptECB(message: String, key: String): String? {
        var result = ""
        try {
            val desKey = DESKeySpec(key.toByteArray())
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val securekey = keyFactory.generateSecret(desKey)
            // Cipher对象实际完成加密操作
            val cipher = Cipher.getInstance("DES")
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, securekey)
            val dofi = cipher.doFinal(message.toByteArray(charset("UTF-8")))
            result = Base64.encode(dofi)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }

        return result
    }

    //解密方式2 为和IOS统一，当前使用方式2
    fun decryptECB(message: String, key: String): String {
        var result = ""
        try {
            val bytesrc = Base64.decode(message)
            val desKey = DESKeySpec(key.toByteArray())
            val keyFactory = SecretKeyFactory.getInstance("DES")
            val securekey = keyFactory.generateSecret(desKey)
            // Cipher对象实际完成加密操作
            val cipher = Cipher.getInstance("DES")
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, securekey)
            val retByte = cipher.doFinal(bytesrc)
            result = String(retByte)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

        return result
    }


}
