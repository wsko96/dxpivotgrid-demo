package kr.wise.demo.pivotgrid.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.JsonGenerator;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.model.Paging;

public final class PivotGridJsonUtils {

    private PivotGridJsonUtils() {
    }

    public static void writeSummaryContainerToJson(final JsonGenerator gen,
            final AbstractSummaryContainer<?> summaryContainer, final String key,
            final String childDataGroupArrayFieldName, final Paging paging, final boolean visibleOnly) throws IOException {
        gen.writeStartObject();

        if (key != null) {
            gen.writeStringField("key", key);
        }

        gen.writeFieldName("summary");

        gen.writeStartArray();
        final List<BigDecimal> summary = summaryContainer.getSummary();
        if (summary != null) {
            for (BigDecimal value : summary) {
                gen.writeNumber(value);
            }
        }
        gen.writeEndArray();

        if (paging != null && paging.getOffset() >= 0 && paging.getLimit() > 0) {
            gen.writeFieldName("paging");
            gen.writeStartObject();
            gen.writeNumberField("offset", paging.getOffset());
            gen.writeNumberField("limit", paging.getLimit());
            gen.writeNumberField("count", paging.getCount());
            gen.writeNumberField("total", paging.getTotal());
            gen.writeEndObject();
        }

        gen.writeFieldName(childDataGroupArrayFieldName);
        final List<DataGroup> childDataGroups = summaryContainer.getChildDataGroups(visibleOnly);
        if (childDataGroups == null) {
            gen.writeNull();
        }
        else {
            gen.writeStartArray();
            for (DataGroup childDataGroup : childDataGroups) {
                writeSummaryContainerToJson(gen, childDataGroup, childDataGroup.getKey(), "items", null, visibleOnly);
            }
            gen.writeEndArray();
        }

        gen.writeEndObject();
    }

    public static void writeTabularDataToJson(final JsonGenerator gen, final DataFrame dataFrame,
            final int skip, final int take) throws IOException {
        gen.writeStartArray();

        final Iterator<DataRow> it = dataFrame.iterator();

        if (skip > 0) {
            for (int i = 0; i < skip && it.hasNext(); i++) {
                it.next();
            }
        }

        final String[] columnNames = dataFrame.getColumnNames();
        int iterCount = 0;

        while (it.hasNext()) {
            final DataRow row = it.next();

            if (take > 0 && ++iterCount > take) {
                break;
            }

            gen.writeStartObject();

            for (String columnName : columnNames) {
                final String value = row.getStringValue(columnName);

                if (NumberUtils.isParsable(value)) {
                    gen.writeNumberField(columnName, new BigDecimal(value));
                }
                else {
                    gen.writeStringField(columnName, value);
                }
            }

            gen.writeEndObject();
        }

        gen.writeEndArray();
    }
}
