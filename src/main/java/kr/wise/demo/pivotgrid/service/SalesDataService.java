package kr.wise.demo.pivotgrid.service;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.wise.demo.pivotgrid.aggregator.DataAggregator;
import kr.wise.demo.pivotgrid.impl.csv.CSVDataReader;
import kr.wise.demo.pivotgrid.impl.csv.CSVDataReaderDataFrame;
import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.model.Paging;
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotgrid.repository.SalesDataRepository;
import kr.wise.demo.pivotgrid.util.ParamUtils;

@RestController
public class SalesDataService {

    private static Logger log = LoggerFactory.getLogger(SalesDataService.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SalesDataRepository repository;

    @Autowired
    private DataAggregator dataAggregator;

    @GetMapping("/sales")
    public void all(HttpServletResponse response,
            @RequestParam(name = "skip", required = false, defaultValue = "0") int skip,
            @RequestParam(name = "take", required = false, defaultValue = "0") int take,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "group", required = false) String group,
            @RequestParam(name = "groupSummary", required = false) String groupSummary,
            @RequestParam(name = "totalSummary", required = false) String totalSummary,
            @RequestParam(name = "paging", required = false) String paging) {
        response.setContentType("application/json");

        FilterParam rootFilter = null;
        GroupParam[] groupParams = null;
        SummaryParam[] groupSummaryParams = null;
        SummaryParam[] totalSummaryParams = null;
        PagingParam pagingParam = null;

        try {
            final ArrayNode filterParamsNode = StringUtils.isNotBlank(filter)
                    ? (ArrayNode) objectMapper.readTree(filter) : null;
            rootFilter = ParamUtils.toFilterParam(filterParamsNode);

            final ArrayNode groupParamsNode = StringUtils.isNotBlank(group)
                    ? (ArrayNode) objectMapper.readTree(group) : null;
            groupParams = ParamUtils.toGroupParams(objectMapper, groupParamsNode);

            final ArrayNode groupSummaryParamsNode = StringUtils.isNotBlank(groupSummary)
                    ? (ArrayNode) objectMapper.readTree(groupSummary) : null;
            groupSummaryParams = ParamUtils.toSummaryParams(objectMapper, groupSummaryParamsNode);

            final ArrayNode totalSummaryParamsNode = StringUtils.isNotBlank(totalSummary)
                    ? (ArrayNode) objectMapper.readTree(totalSummary) : null;
            totalSummaryParams = ParamUtils.toSummaryParams(objectMapper, totalSummaryParamsNode);

            final ObjectNode pagingParamNode = StringUtils.isNotBlank(paging)
                    ? (ObjectNode) objectMapper.readTree(paging) : null;
            pagingParam = ParamUtils.toPagingParam(objectMapper, pagingParamNode);
        }
        catch (Exception e) {
            log.error("Failed to parse params.", e);
        }

        CSVDataReader csvDataReader = null;
        ServletOutputStream sos = null;
        BufferedOutputStream bos = null;
        JsonGenerator gen = null;

        try {
            sos = response.getOutputStream();
            bos = new BufferedOutputStream(sos);
            gen = objectMapper.createGenerator(bos);

            csvDataReader = repository.findAll();
            final DataFrame dataFrame = new CSVDataReaderDataFrame(csvDataReader);

            if (ArrayUtils.isNotEmpty(groupParams)) {
                log.debug(
                        "Group aggregation data request invoked. filter: {}, group: {}, groupSummary: {}, totalSummary: {}, paging: {}",
                        filter, group, groupSummary, totalSummary, paging);
                final DataAggregation aggregation = dataAggregator.createDataAggregation(dataFrame,
                        rootFilter, groupParams, groupSummaryParams, totalSummaryParams);

                // 1. sort any child groups before writing.

                // 2. cut groups to include only paginated groups
                if (pagingParam != null && pagingParam.getOffset() >= 0 && pagingParam.getLimit() > 0
                        && pagingParam.getRowGroupCount() > 0) {
                    final Set<String> groupParamsInfoSet = new HashSet<>();
                    Arrays.stream(groupParams)
                            .forEach((param) -> groupParamsInfoSet.add(param.getSelector()));
                    if (pagingParam.getRowGroupParams().stream()
                            .noneMatch((param) -> !groupParamsInfoSet.contains(param.getSelector()))) {
                        aggregation.setOffset(pagingParam.getOffset());
                        aggregation.setLimit(pagingParam.getLimit());
                    }
                }

                writeSummaryContainerToJson(gen, aggregation, null, "data", aggregation.getPaging());
            }
            else {
                log.debug("Simple data request invoked. skip: {}, take: {}", skip, take);
                writeTabularDataToJson(gen, dataFrame, skip, take);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(gen, bos, sos);
            IOUtils.closeQuietly(csvDataReader);
        }
    }

    private void writeSummaryContainerToJson(final JsonGenerator gen,
            final AbstractSummaryContainer<?> summaryContainer, final String key,
            final String childDataGroupArrayFieldName, final Paging paging) throws IOException {
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

        if (paging != null) {
            gen.writeFieldName("paging");
            gen.writeStartObject();
            gen.writeNumberField("offset", paging.getOffset());
            gen.writeNumberField("limit", paging.getLimit());
            gen.writeNumberField("total", paging.getTotal());
            gen.writeEndObject();
        }

        gen.writeFieldName(childDataGroupArrayFieldName);
        final List<DataGroup> childDataGroups = summaryContainer.getChildDataGroups();
        if (childDataGroups == null) {
            gen.writeNull();
        }
        else {
            gen.writeStartArray();
            for (DataGroup childDataGroup : childDataGroups) {
                writeSummaryContainerToJson(gen, childDataGroup, childDataGroup.getKey(), "items", null);
            }
            gen.writeEndArray();
        }

        gen.writeEndObject();
    }

    private void writeTabularDataToJson(final JsonGenerator gen, final DataFrame dataFrame,
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
