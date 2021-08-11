package kr.wise.demo.pivotgrid.service;

import java.io.IOException;
import java.math.BigDecimal;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ClosingCSVRecordIteratorSerializer extends JsonSerializer<CloseableCSVRecordIterator> {

    @Override
    public void serialize(CloseableCSVRecordIterator iterator, JsonGenerator gen,
            SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        try {
            final String[] headers = iterator.getHeaders();
            final int headerCount = headers.length;

            gen.writeStartArray();

            while (iterator.hasNext()) {
                final CSVRecord record = iterator.next();

                gen.writeStartObject();

                for (int i = 0; i < headerCount; i++) {
                    final String value = record.get(i);
                    gen.writeObjectField(headers[i],
                            NumberUtils.isParsable(value) ? new BigDecimal(value) : value);
                }

                gen.writeEndObject();
            }

            gen.writeEndArray();
        }
        finally {
            IOUtils.closeQuietly(iterator); 
        }
    }
}
