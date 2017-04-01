package com.richfit.common_lib.utils;

import android.text.TextUtils;

import com.richfit.common_lib.exception.ArgumentException;
import com.richfit.common_lib.exception.InstanceException;
import com.richfit.common_lib.exception.NullException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import okhttp3.Request;
import okio.Buffer;
import okio.ByteString;

/**
 * Created by monday on 2016/12/30.
 */

public class CommonUtil {
    private CommonUtil() {

    }

    // 转化十六进制编码为字符串
    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            s = new String(baKeyword, "gb2312");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static String toHexString(String ss) {
        String str= null;
        try {
            byte[] tbyte = ss.getBytes("GB2312");
            String s = new String(tbyte,"ISO-8859-1");
            str = "";
            for (int i=0;i<s.length();i++) {
                int ch = (int)s.charAt(i);
                String s4 = Integer.toHexString(ch);
                str = str + s4;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ss;
        }
        return str.toUpperCase();
    }

    public static String toUpperCase(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        return str.toUpperCase();
    }

    public static String Obj2String(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString().trim();
    }

    public static void putAll(Map<String,Object> originMap,Map<String,Object> targetMap) {
        if(originMap != null && targetMap != null) {
            originMap.putAll(targetMap);
        }
    }

    /**
     * 不为空
     */
    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullException("Can not be empty.");
        }
        return obj;
    }

    /**
     * 不小于0
     */
    public static long checkNotLessThanZero(long number) {
        if (number < 0) {
            throw new ArgumentException("Can not be < 0.");
        }

        return number;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException("length=" + arrayLength
                    + "; regionStart=" + offset
                    + "; regionLength=" + count);
        }
    }

    public static Class<?> getRawType(Type type) {
        if (type == null) throw new NullPointerException("type == null");

        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    public static <T> T newInstance(Class<T> clazz) throws InstanceException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new InstanceException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new InstanceException(e.getMessage());
        }
    }

    /**
     * 根据Request生成哈希值
     *
     * @param request
     * @return
     */
    public static String getHash(Request request) {
        StringBuilder str = new StringBuilder();
        str.append('[');
        str.append(request.method());
        str.append(']');
        str.append('[');
        str.append(request.url());
        str.append(']');

        try {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            str.append(buffer.readByteString().sha1().hex());
        } catch (IOException e) {
            return "";
        }

        str.append('-');
        str.append(ByteString.of(request.headers().toString().getBytes()).sha1().hex());

        return str.toString();
    }


}
