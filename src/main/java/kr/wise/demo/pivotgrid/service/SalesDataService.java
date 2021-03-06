package kr.wise.demo.pivotgrid.service;

import java.io.BufferedOutputStream;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotgrid.repository.SalesDataRepository;
import kr.wise.demo.pivotgrid.util.ParamUtils;
import kr.wise.demo.pivotgrid.util.PivotGridJsonUtils;

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
        List<GroupParam> groupParams = null;
        List<SummaryParam> groupSummaryParams = null;
        List<SummaryParam> totalSummaryParams = null;
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

            if (!groupParams.isEmpty()) {
                log.debug(
                        "Group aggregation data request invoked. filter: {}, group: {}, groupSummary: {}, totalSummary: {}, paging: {}",
                        filter, group, groupSummary, totalSummary, paging);

                final DataAggregation aggregation = dataAggregator.createDataAggregation(dataFrame, rootFilter,
                        groupParams, groupSummaryParams, totalSummaryParams, pagingParam);
                PivotGridJsonUtils.writeSummaryContainerToJson(gen, aggregation, null, "data",
                        aggregation.getPaging(), aggregation.isPagingApplied());
            }
            else {
                log.debug("Simple data request invoked. skip: {}, take: {}", skip, take);
                PivotGridJsonUtils.writeTabularDataToJson(gen, dataFrame, skip, take);
            }
        }
        catch (Exception e) {
            log.error("Failed to process data aggregation.", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        finally {
            IOUtils.closeQuietly(gen, bos, sos);
            IOUtils.closeQuietly(csvDataReader);
        }
    }
}
