package otp.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtilities {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T fromJson(InputStream is, Class<T> clazz) throws IOException {
        if (is == null) return null;
        return MAPPER.readValue(is, clazz);
    }

    public static <T> T fromJson(String jsonString, Class<T> clazz) throws IOException {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        return MAPPER.readValue(jsonString, clazz);
    }

    public static String toJson(Object obj) throws IOException {
        return MAPPER.writeValueAsString(obj);
    }
}