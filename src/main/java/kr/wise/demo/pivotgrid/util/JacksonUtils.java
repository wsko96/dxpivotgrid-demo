package kr.wise.demo.pivotgrid.util;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JacksonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();

    private JacksonUtils() {
        
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static ObjectNode csvRecordToObjectNode(final CSVRecord record, final List<String> headers) {
        final ObjectNode objectNode = getObjectMapper().createObjectNode();
        for (String header : headers) {
            final String value = record.get(header);
            if (NumberUtils.isParsable(value)) {
                objectNode.put(header, new BigDecimal(value));
            } else {
                objectNode.put(header, value);
            }
        }
        return objectNode;
    }
}
