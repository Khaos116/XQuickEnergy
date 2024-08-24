package tkaxv7s.xposed.sesame.util;

/**
 * Author:XX
 * Date:2024/8/21
 * Time:10:38
 */

import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MyDecryptor {
  private static String decode2(String str, SecretKey secretKey, String str2) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(2, secretKey, new IvParameterSpec(str2.getBytes("UTF-8")));
    return new String(cipher.doFinal(Base64.decode(str, 0)), "UTF-8");
  }

  private static String decode1(String str) {
    try {
      byte[] decode = Base64.decode("MXlpZWhheThlOWFvaGEzLTI7YWYnYXIyMTI0cnNvcHM=", 0);
      return decode2(str, new SecretKeySpec(decode, 0, decode.length, "AES"), "beaf63cdb43c2d87");
    } catch (Throwable unused) {
      return "你可能遇到了一点问题";
    }
  }

  public static String decode() {
    List<String> list = new ArrayList<>();
    list.add("2X/5g2AajAQdxeItCyvTnA==");//Android
    list.add("jdWjucQyDWRuaLe0TE3d9Q==");//media
    list.add("O9MA0Bb9MG5W8mGBITYdrpB8r3E8OcL49D11xJupBVw=");//com.eg.android.AlipayGphone
    list.add("Wn7fJMi/lE/ivBZf1ajxSHDOVO4pai+islpuN7yLPTfOlamG5qRft3sbEe27ttN6pNW2o4LrTLPMLvNil7xntww/lwaLHxlG3ehgkRTTdjE=");//911a25780c985934ca4de6598a39a1ada9393c6a348a5c6abdf520bb2232dcbd
    list.add("PYLmmZmK747NTGAysUurHHqwHtEGudjG/N8OvjdAgJ0=");//io.github.lazyimmortal.sesame
    list.add("f4WSlCzx4rSMKT1rYTKIdp/HXV0LfcDm2Cw2IvjhkNXyvDW0JLyMAulc88BmW865m2N6C9zBKJwjOkK+55d80rwLqLSWO9xDzN8dKFo0EpY=");//6d65721df84388561a0e1fda3c1059d7a5430dc8c2195e26fd40437af0d5428d
    list.add("vQQqiwPS9PYRVumgKOWMvG3qWlBNB6KznPq/VeBh/ec=");//芝麻开门:阿里巴巴
    list.add("SNPgHco1OERSfcR1FUtSyOW3QamrQeiiQ9zZpfTc1ws=");//芝麻关门:四十大盗
    list.add("oyqJOAQ6SpmgJyREFnvqAw==");//sesameX
    list.add("YOZLYmz+vb0TUAxGo4EMDQ==");//XRadiant
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      sb.append("\n===========================\n").append(s).append("\n\n").append(decode1(s));
    }
    sb.append("\n===========================\n");
    return sb.toString().trim();
  }

  private static String encode1(String plainText, SecretKey secretKey, String iv) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv.getBytes("UTF-8")));
    byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
    return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP); // Android Base64 encode
  }

  public static void encode() throws Exception {
    // 密钥和 IV
    byte[] keyBytes = Base64.decode("MXlpZWhheThlOWFvaGEzLTI7YWYnYXIyMTI0cnNvcHM=", Base64.NO_WRAP);
    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
    String iv = "beaf63cdb43c2d87";

    // 明文
    String plainText = "芝麻关门:四十大盗";

    // 加密
    String encryptedData = encode1(plainText, secretKey, iv);
    Log.e("加密结果", encryptedData);
  }



  public static String decryption1(String str) {
    try {
      return decryption2(str, decryption3("ZDF5aWVoYXk4ZTlhb2hhMy0yO2FmJ2FyMjEyNHJ3b2Q="), "beaf63cdb43c2d87");
    } catch (Throwable unused) {
      return "";
    }
  }

  private static String decryption2(String str, SecretKey secretKey, String str2) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(2, secretKey, new IvParameterSpec(str2.getBytes("UTF-8")));
    return new String(cipher.doFinal(Base64.decode(str, 0)), "UTF-8");
  }

  private static SecretKey decryption3(String str) {
    byte[] decode = Base64.decode(str, 0);
    return new SecretKeySpec(decode, 0, decode.length, "AES");
  }

  public static String decryption() {//1.5.3新的加密
    List<String> list = new ArrayList<>();
    list.add("AYoYTW4L4xdSpJtd1Kg94A==");//sesameX
    list.add("RuUShDaVmiZ3pD+4lyqyOw==");//XRadiant
    list.add("WjtGKWUARdH31WNpwVstVA==");//SHA-256
    list.add("DuCLys3bnnRP8BCWU9Kjsg==");// /data/app/
    list.add("Y53myRrjK9p/cTOKIaw29Q==");//META-INF/
    list.add("k6pMepzxyZZmi1QPf86mPA==");//.RSA
    list.add("chceeQrTF3W+Cb25ODcQqg==");//X.509
    list.add("wTbb+WMKnwfaNlV6XYaSDRtSnsgAxiWfjYhWk2EGS68=");//leo.xposed.sesameX
    list.add("S/xUJClYXTwaQ12TkdBqQ3E8sBql2dNSln+jCmNq5j8=");//com.leo.xposed.xradiant
    list.add("S/xUJClYXTwaQ12TkdBqQ6LUjyedVtdk5a29O1yIGmc=");//com.leo.xposed.xradiant.App
    list.add("I+JrdxONCJAGcayEt9VOS/eLyEuj9qG+ZIJUpkB3mZs=");//com.eg.android.AlipayGphone
    list.add("7PR5REAqnmSjXrTd4FAQLuCoLvYVc1IlfsNrUWWQQVY574m0PtIGVND7Cx8yb1RoEz16ryokNinv6vmmFdsmCwM+X8MhHHBNZR9kOKJRS6k=");//911a25780c985934ca4de6598a39a1ada9393c6a348a5c6abdf520bb2232dcbd
    list.add("HU5z8Zn6D6H0Nv4eQwa4twKaSdWpM6tdR44cCEpffdg=");//io.github.lazyimmortal.sesame
    list.add("sCItxkHcv8Tp/+HmyqKG2yA9wtP4sTXdQRrkX7CQE+7+AmY7JEmHnVlYsqfbT6z6utB0t4KLYRPUUllJ0APY0fR2yl5tcINO9y8ZPRfet/g=");//6d65721df84388561a0e1fda3c1059d7a5430dc8c2195e26fd40437af0d5428d
    list.add("woWQPSZ0dtkPPBOdly12nA==");//Android
    list.add("kBRJo115aoSYuXIirbT2LA==");//media
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      sb.append("\n===========================\n").append(s).append("\n\n").append(decryption1(s));
    }
    sb.append("\n===========================\n");
    return sb.toString().trim();
  }
}


    //"AYoYTW4L4xdSpJtd1Kg94A=="//sesameX
    //"RuUShDaVmiZ3pD+4lyqyOw=="//XRadiant
    //"WjtGKWUARdH31WNpwVstVA=="//SHA-256
    //"DuCLys3bnnRP8BCWU9Kjsg=="// /data/app/
    //"Y53myRrjK9p/cTOKIaw29Q=="//META-INF/
    //"k6pMepzxyZZmi1QPf86mPA=="//.RSA
    //"chceeQrTF3W+Cb25ODcQqg=="//X.509
    //"wTbb+WMKnwfaNlV6XYaSDRtSnsgAxiWfjYhWk2EGS68="//leo.xposed.sesameX
    //"S/xUJClYXTwaQ12TkdBqQ3E8sBql2dNSln+jCmNq5j8="//com.leo.xposed.xradiant
    //"S/xUJClYXTwaQ12TkdBqQ6LUjyedVtdk5a29O1yIGmc="//com.leo.xposed.xradiant.App
    //"I+JrdxONCJAGcayEt9VOS/eLyEuj9qG+ZIJUpkB3mZs="//com.eg.android.AlipayGphone
    //"7PR5REAqnmSjXrTd4FAQLuCoLvYVc1IlfsNrUWWQQVY574m0PtIGVND7Cx8yb1RoEz16ryokNinv6vmmFdsmCwM+X8MhHHBNZR9kOKJRS6k="//911a25780c985934ca4de6598a39a1ada9393c6a348a5c6abdf520bb2232dcbd
    //"HU5z8Zn6D6H0Nv4eQwa4twKaSdWpM6tdR44cCEpffdg="//io.github.lazyimmortal.sesame
    //"sCItxkHcv8Tp/+HmyqKG2yA9wtP4sTXdQRrkX7CQE+7+AmY7JEmHnVlYsqfbT6z6utB0t4KLYRPUUllJ0APY0fR2yl5tcINO9y8ZPRfet/g="//6d65721df84388561a0e1fda3c1059d7a5430dc8c2195e26fd40437af0d5428d
    //"woWQPSZ0dtkPPBOdly12nA=="//Android
    //"kBRJo115aoSYuXIirbT2LA=="//media