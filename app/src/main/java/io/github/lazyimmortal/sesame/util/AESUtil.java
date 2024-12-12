package io.github.lazyimmortal.sesame.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";

    // 默认的初始化向量 (IV)
    private static final String DEFAULT_IV = "1234567890123456";

    /**
     * 生成AES密钥
     * @return 生成的密钥
     * @throws Exception 异常
     */
    private static SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(256); // 可以使用 128, 192 或 256 位密钥
        return keyGen.generateKey();
    }

    /**
     * 将密钥转换为字符串形式
     * @param secretKey SecretKey 对象
     * @return Base64 编码的密钥字符串
     */
    private static String keyToString(SecretKey secretKey) {
        return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
    }

    /**
     * 从字符串恢复密钥
     * @param keyString Base64 编码的密钥字符串
     * @return SecretKey 对象
     */
    private static SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.decode(keyString, Base64.DEFAULT);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * AES加密
     * @param data 要加密的数据
     * @param key 加密密钥
     * @param iv 初始化向量 (IV)
     * @return 加密后的字符串（Base64 编码）
     * @throws Exception 异常
     */
    private static String encrypt(String data, SecretKey key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(CHARSET));
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(data.getBytes(CHARSET));
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    /**
     * AES解密
     * @param encryptedData 要解密的数据（Base64 编码）
     * @param key 解密密钥
     * @param iv 初始化向量 (IV)
     * @return 解密后的字符串
     * @throws Exception 异常
     */
    private static String decrypt(String encryptedData, SecretKey key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(CHARSET));
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decodedData = Base64.decode(encryptedData, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(decodedData);
        return new String(decrypted, CHARSET);
    }

    public static native String encryptData(String data);
    private static String encryptData(String data, String key, String iv) {
        String result = null;
        try {
            result = encrypt(data, stringToKey(key), iv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static native String decryptData(String data);
    private static String decryptData(String data, String key, String iv) {
        String result = null;
        try {
            result = decrypt(data, stringToKey(key), iv);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String readZipFile(String zipFilePath, String filePath) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            ZipEntry entry = zipFile.getEntry(filePath);
            if (entry != null) {
                InputStream inputStream = zipFile.getInputStream(entry);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                inputStream.close();
                return content.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static String readAssetFile(Context context, String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        AssetManager assetManager = context.getAssets();

        try {
            // 从 "file:///android_asset/" 中提取实际的文件路径
            String assetFilePath = filePath.substring("file:///android_asset/".length());

            // 打开文件输入流
            InputStream inputStream = assetManager.open(assetFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // 逐行读取文件内容并追加到StringBuilder中
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }

            // 关闭流
            reader.close();
            inputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 返回读取的字符串内容
        return stringBuilder.toString();
    }

    public static String loadDecryptHtmlData(Context context) {
        String htmlData = readAssetFile(context, "file:///android_asset/web/js/index.js");
        return "<!DOCTYPE html><html lang=\"en\"><script>" + decryptData(htmlData) + "</script></html>";
    }

//    public static void main(String[] args) {
//        try {
//            // 生成密钥
//            SecretKey key = AESUtil.generateKey();
//            String keyString = AESUtil.keyToString(key);
//
//            // 示例数据
//            String originalData = "Hello, World!";
//            String iv = DEFAULT_IV; // 使用默认的初始化向量
//
//            // 加密
//            String encryptedData = AESUtil.encrypt(originalData, key, iv);
//            System.out.println("Encrypted: " + encryptedData);
//
//            // 解密
//            String decryptedData = AESUtil.decrypt(encryptedData, AESUtil.stringToKey(keyString), iv);
//            System.out.println("Decrypted: " + decryptedData);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}

