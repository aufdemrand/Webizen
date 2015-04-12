package net.aufdemrand.webizen.objects

import org.apache.commons.lang.RandomStringUtils
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64

public class Encryptor {

    public static def _
    public static def __

    public static init() {
        _ = RandomStringUtils.random(16, true, true)
        __ = RandomStringUtils.random(16, true, true)
    }

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(__.getBytes("UTF-8"))
            SecretKeySpec skeySpec = new SecretKeySpec(_.getBytes("UTF-8"), "AES")
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            byte[] encrypted = cipher.doFinal(value.getBytes())
            return Base64.encodeBase64String(encrypted)
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(__.getBytes("UTF-8"))
            SecretKeySpec skeySpec = new SecretKeySpec(_.getBytes("UTF-8"), "AES")
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted))
            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}