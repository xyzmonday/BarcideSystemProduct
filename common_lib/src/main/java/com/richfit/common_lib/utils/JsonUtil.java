package com.richfit.common_lib.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * @param
 * @author modnay JSON工具类
 */
public class JsonUtil {

    private static final Gson gson = new Gson();

    public static Map<String, Object> json2Map(final String json) {
        return gson.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    public static String map2Json(Map<String,Object> map) {
        return gson.toJson(map);

    }

    public static String object2Json(Object object) {
        return gson.toJson(object);
    }

}