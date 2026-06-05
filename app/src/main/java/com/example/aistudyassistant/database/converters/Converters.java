package com.example.aistudyassistant.database.converters;

import androidx.room.TypeConverter;
import com.example.aistudyassistant.database.entities.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class Converters {
    private static final Gson gson = new Gson();

    // Chuyển đổi danh sách tin nhắn Chat
    @TypeConverter
    public static List<ChatMessage> fromChatMessageString(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<ChatMessage>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromChatMessageList(List<ChatMessage> list) {
        if (list == null) return null;
        return gson.toJson(list);
    }

    // Chuyển đổi Map (dùng cho stats và settings trong Profile)
    @TypeConverter
    public static Map<String, String> fromMapString(String value) {
        if (value == null) return null;
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, String> map) {
        if (map == null) return null;
        return gson.toJson(map);
    }

    // Chuyển đổi danh sách String (dùng cho userAnswers hoặc câu hỏi)
    @TypeConverter
    public static List<String> fromStringList(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String toListString(List<String> list) {
        if (list == null) return null;
        return gson.toJson(list);
    }
}
