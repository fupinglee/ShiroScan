package com.fuping;

import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class ShiroAESCrypto {

    public static String encrypt(byte[] serialized, byte[] key) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"

        int sizeInBytes = 16;
        byte[] iv = new byte[sizeInBytes];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.nextBytes(iv);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec =  new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
        byte[] encrypted = cipher.doFinal(serialized);
        byte[] output;
        output = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);
        return (new BASE64Encoder().encode(output)).replaceAll("\r\n","");
    }


    public static String decrypt(byte[] ciphertext, byte[] key ) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"

        byte[] encrypted = ciphertext;
        int ivByteSize = 16;
        byte[] iv = new byte[ivByteSize];
        System.arraycopy(ciphertext, 0, iv, 0, ivByteSize);
        int encryptedSize = ciphertext.length - ivByteSize;
        encrypted = new byte[encryptedSize];
        System.arraycopy(ciphertext, ivByteSize, encrypted, 0, encryptedSize);
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec =  new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
        byte[] output = cipher.doFinal(encrypted);

        return new String(output);
    }
}
