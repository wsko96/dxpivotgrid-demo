package kr.wise.demo.pivotgrid.service;

import java.io.BufferedOutputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVRecord;
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

import kr.wise.demo.pivotgrid.aggregator.DataAggregator;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotgrid.repository.CSVDataReader;
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
            @RequestParam(name = "totalSummary", required = false) String totalSummary) {
        response.setContentType("application/json");

        FilterParam rootFilter = null;
        GroupParam[] groupParams = null;
        SummaryParam[] groupSummaryParams = null;
        SummaryParam[] totalSummaryParams = null;

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
        }
        catch (Exception e) {
            log.error("Failed to parse params.", e);
        }

        if (ArrayUtils.isNotEmpty(groupParams)) {
            log.debug(
                    "Group aggregation data request invoked. filter: {}, group: {}, groupSummary: {}, totalSummary: {}",
                    filter, group, groupSummary, totalSummary);
            writeAggregatedDataResponse(response, rootFilter, groupParams, groupSummaryParams,
                    totalSummaryParams);
        }
        else {
            log.debug("Simple data request invoked. skip: {}, take: {}", skip, take);
            writeTabularDataResponse(response, skip, take);
        }
    }

    private void writeAggregatedDataResponse(final HttpServletResponse response,
            final FilterParam rootFilter, final GroupParam[] groupParams,
            final SummaryParam[] groupSummaryParams, final SummaryParam[] totalSummaryParams) {
        response.setContentType("application/json");

        CSVDataReader csvDataReader = null;
        ServletOutputStream sos = null;
        BufferedOutputStream bos = null;
        JsonGenerator gen = null;

        try {
            sos = response.getOutputStream();
            bos = new BufferedOutputStream(sos);

            csvDataReader = repository.findAll();
            final DataFrame dataFrame = new CSVDataReaderDataFrame(csvDataReader);
            final DataAggregation aggregation = dataAggregator.createDataAggregation(dataFrame,
                    rootFilter, groupParams, groupSummaryParams, totalSummaryParams);

            gen = objectMapper.createGenerator(bos);
            gen.writeObject(aggregation);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(gen, bos, sos);
            IOUtils.closeQuietly(csvDataReader);
        }
    }

    private void writeTabularDataResponse(final HttpServletResponse response, final int skip,
            final int take) {
        response.setContentType("application/json");

        CSVDataReader csvDataReader = null;
        ServletOutputStream sos = null;
        BufferedOutputStream bos = null;
        JsonGenerator gen = null;

        try {
            csvDataReader = repository.findAll();

            sos = response.getOutputStream();
            bos = new BufferedOutputStream(sos);
            gen = objectMapper.createGenerator(bos);

            gen.writeStartArray();

            final PagedCSVRecordIterator recordIt = new PagedCSVRecordIterator(
                    csvDataReader.iterator(), skip, take);
            final List<String> headers = csvDataReader.getHeaders();

            while (recordIt.hasNext()) {
                final CSVRecord record = recordIt.next();

                gen.writeStartObject();

                for (String header : headers) {
                    final String value = record.get(header);

                    if (NumberUtils.isParsable(value)) {
                        gen.writeNumberField(header, new BigDecimal(value));
                    }
                    else {
                        gen.writeStringField(header, value);
                    }
                }

                gen.writeEndObject();
            }

            gen.writeEndArray();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(gen, bos, sos);
            IOUtils.closeQuietly(csvDataReader);
        }
    }
}
