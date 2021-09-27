package kr.wise.demo.pivotmatrix;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;

public final class SummaryMatrixUtils {

    private SummaryMatrixUtils() {
    }

    public static void writeSummaryMatrixToJson(final JsonGenerator gen, final SummaryMatrix matrix)
            throws IOException {
        gen.writeStartObject();

        gen.writeEndObject();
    }

}
