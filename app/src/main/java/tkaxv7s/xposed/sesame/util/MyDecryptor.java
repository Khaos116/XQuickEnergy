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
}