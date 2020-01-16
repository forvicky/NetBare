package com.nlp.netbaredemo.test;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zdd on 2019/11/7
 */
public class Test {

//    public static void initDeviceId(Context context) {
//        String string = Secure.getString(context.getContentResolver(), "android_id");
//        if (string == null) {
//            if (VERSION.SDK_INT >= 26) {
//                string = Build.getSerial();
//            } else {
//                string = Build.SERIAL;
//            }
//        }
//        if (string == null) {
//            string = UUID.randomUUID().toString();
//        }
//        e.setDeviceId(Base64.encodeToString(string.getBytes(),Base64.DEFAULT).toUpperCase());
//    }

    @Deprecated
    public static String jK(String str) {
        return V(str.getBytes());
    }

    public static String V(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer(bArr.length * 2);
        for (byte b : bArr) {
            int i = b & 255;
            if (i < 16) {
                stringBuffer.append("0");
            }
            stringBuffer.append(Long.toString((long) i, 16));
        }
        return stringBuffer.toString();
    }

//    public static String getDeviceId(){
//        if (string == null) {
//            if (VERSION.SDK_INT >= 26) {
//                string = Build.getSerial();
//            } else {
//                string = Build.SERIAL;
//            }
//        }
//        if (string == null) {
//            string = UUID.randomUUID().toString();
//        }
//
//    }


    @NonNull
    public static Cipher r(String str, int i) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(str.getBytes(), "AES");
        Cipher instance = Cipher.getInstance("AES/ECB/PKCS5Padding");
        instance.init(i, secretKeySpec);
        return instance;
    }

    @Nullable
    public static byte[] e(byte[] bArr, String str) {
        try {
            return r(str, 1).doFinal(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String aj(String str, String str2) {
        byte[] e = e(str.getBytes(), str2);
        return e != null ? Base64.encodeToString(e,Base64.DEFAULT) : "";
    }

    public static byte[] f(byte[] bArr, String str) {
        try {
            return r(str, 2).doFinal(bArr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String ak(String str, String str2) {
        byte[] f = f(Base64.decode(str,Base64.DEFAULT), str2);
        return f != null ? new String(f) : "";
    }

}
