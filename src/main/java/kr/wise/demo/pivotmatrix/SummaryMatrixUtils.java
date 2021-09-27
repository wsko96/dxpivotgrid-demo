package kr.wise.demo.pivotmatrix;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotmatrix.impl.DefaultSummaryMatrixImpl;

public final class SummaryMatrixUtils {

    private static Logger log = LoggerFactory.getLogger(SummaryMatrixUtils.class);

    private SummaryMatrixUtils() {
    }

    public static void writeSummaryMatrixToJson(final JsonGenerator gen, final SummaryMatrix matrix)
            throws IOException {
        gen.writeStartObject();

        gen.writeFieldName("meta");

        gen.writeStartObject();
        gen.writeObjectField("rowGroupParams", matrix.getRowGroupParams());
        gen.writeObjectField("colGroupParams", matrix.getColGroupParams());
        gen.writeObjectField("summaryParams", matrix.getSummaryParams());

        gen.writeFieldName("rowSummaryDimension");
        writeSummaryDimensionToJson(gen, matrix.getRowSummaryDimension(), true);
        gen.writeFieldName("colSummaryDimension");
        writeSummaryDimensionToJson(gen, matrix.getColSummaryDimension(), true);

        gen.writeFieldName("rowFlattendSummaryDimensions");
        SummaryDimension[] summaryDimensions = matrix.getRowFlattendSummaryDimensions();
        gen.writeStartArray();
        if (summaryDimensions != null) {
            for (SummaryDimension summaryDimension : summaryDimensions) {
                writeSummaryDimensionToJson(gen, summaryDimension, false);
            }
        }
        gen.writeEndArray();

        gen.writeFieldName("colFlattendSummaryDimensions");
        summaryDimensions = matrix.getColFlattendSummaryDimensions();
        gen.writeStartArray();
        if (summaryDimensions != null) {
            for (SummaryDimension summaryDimension : summaryDimensions) {
                writeSummaryDimensionToJson(gen, summaryDimension, false);
            }
        }
        gen.writeEndArray();

        gen.writeEndObject();

        gen.writeFieldName("matrix");
        gen.writeStartObject();
        gen.writeNumberField("rows", matrix.getRows());
        gen.writeNumberField("cols", matrix.getCols());
        gen.writeObjectField("cells", matrix.getSummaryCells());
        gen.writeEndObject();

        gen.writeEndObject();
    }

    private static void writeSummaryDimensionToJson(final JsonGenerator gen,
            final SummaryDimension summaryDimension, final boolean includeChildren)
            throws IOException {
        gen.writeStartObject();
        gen.writeStringField("key", summaryDimension.getKey());
        gen.writeStringField("path", summaryDimension.getPath());
        gen.writeNumberField("depth", summaryDimension.getDepth());

        if (includeChildren) {
            gen.writeObjectField("children", summaryDimension.getChildren());
        }

        gen.writeEndObject();
    }

    public static SummaryMatrix readSummaryMatrixFromJson(final ObjectMapper objectMapper,
            final Reader reader) throws IOException {
        final JsonNode rootNode = objectMapper.readTree(reader);

        List<GroupParam> rowGroupParams = null;
        List<GroupParam> colGroupParams = null;
        List<SummaryParam> summaryParams = null;
        SummaryDimension rowSummaryDimension = null;
        SummaryDimension colSummaryDimension = null;

        final JsonNode metaNode = rootNode.get("meta");
        rowGroupParams = readGroupParamsFromJsonNode(objectMapper, metaNode.get("rowGroupParams"));
        colGroupParams = readGroupParamsFromJsonNode(objectMapper, metaNode.get("colGroupParams"));
        summaryParams = readSummaryParamsFromJsonNode(objectMapper, metaNode.get("summaryParams"));
        rowSummaryDimension = objectMapper.treeToValue(metaNode.get("rowSummaryDimension"),
                SummaryDimension.class);
        colSummaryDimension = objectMapper.treeToValue(metaNode.get("colSummaryDimension"),
                SummaryDimension.class);

        log.debug(
                "rowGroupParams: {}, colGroupParams: {}, summaryParams: {}, rowSummaryDimension: {}, colSummaryDimension: {}",
                rowGroupParams, colGroupParams, summaryParams, rowSummaryDimension,
                colSummaryDimension);

        final DefaultSummaryMatrixImpl matrix = new DefaultSummaryMatrixImpl(rowGroupParams,
                colGroupParams, summaryParams, rowSummaryDimension, colSummaryDimension);

        final JsonNode matrixNode = rootNode.get("matrix");
        final int rows = matrixNode.get("rows").asInt();
        final int cols = matrixNode.get("cols").asInt();
        final SummaryCell[][] summaryCells = readSummaryCells(objectMapper, rows, cols,
                (ArrayNode) matrixNode.get("cells"));
        matrix.setSummaryCells(summaryCells);

        return matrix;
    }

    private static List<GroupParam> readGroupParamsFromJsonNode(final ObjectMapper objectMapper,
            final JsonNode jsonNode) throws IOException {
        final List<GroupParam> groupParams = new ArrayList<>();

        if (jsonNode != null && jsonNode.isArray()) {
            final ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (JsonNode itemNode : arrayNode) {
                if (itemNode.isObject()) {
                    final GroupParam groupParam = objectMapper.treeToValue(itemNode,
                            GroupParam.class);
                    groupParams.add(groupParam);
                }
            }
        }

        return groupParams;
    }

    private static List<SummaryParam> readSummaryParamsFromJsonNode(final ObjectMapper objectMapper,
            final JsonNode jsonNode) throws IOException {
        final List<SummaryParam> summaryParams = new ArrayList<>();

        if (jsonNode != null && jsonNode.isArray()) {
            final ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (JsonNode itemNode : arrayNode) {
                if (itemNode.isObject()) {
                    final SummaryParam summaryParam = objectMapper.treeToValue(itemNode,
                            SummaryParam.class);
                    summaryParams.add(summaryParam);
                }
            }
        }

        return summaryParams;
    }

    private static SummaryCell[][] readSummaryCells(final ObjectMapper objectMapper, final int rows,
            final int cols, final ArrayNode arrayNode) throws IOException {
        final SummaryCell[][] cells = new SummaryCell[rows][cols];

        int r = 0;
        for (JsonNode rowNode : arrayNode) {
            final ArrayNode rowArrayNode = (ArrayNode) rowNode;
            int c = 0;
            for (JsonNode itemNode : rowArrayNode) {
                final SummaryCell cell = new SummaryCell();
                final ArrayNode summaryValuesNode = (ArrayNode) itemNode.get("vs");
                for (JsonNode summaryValueNode : summaryValuesNode) {
                    final SummaryValue summaryValue = objectMapper.treeToValue(summaryValueNode, SummaryValue.class);
                    cell.addSummaryValue(summaryValue);
                }
                cells[r][c] = cell;
                c++;
            }
            r++;
        }

        return cells;
    }
}
