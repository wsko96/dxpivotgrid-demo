package kr.wise.demo.pivotgrid.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JacksonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private JacksonUtils() {
        
    }
    
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
