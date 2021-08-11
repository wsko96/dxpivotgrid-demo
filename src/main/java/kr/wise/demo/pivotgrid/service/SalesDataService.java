package kr.wise.demo.pivotgrid.service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.model.SummaryContainer;
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotgrid.repository.CSVDataSet;
import kr.wise.demo.pivotgrid.repository.SalesDataRepository;
import kr.wise.demo.pivotgrid.util.JacksonUtils;
import kr.wise.demo.pivotgrid.util.ParamUtils;

@RestController
public class SalesDataService {

    private static Logger log = LoggerFactory.getLogger(SalesDataService.class);

    @Autowired
    private SalesDataRepository repository;

    @GetMapping("/sales")
    public Object all(@RequestParam(name = "skip", required = false, defaultValue = "0") int skip,
            @RequestParam(name = "take", required = false, defaultValue = "0") int take,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "group", required = false) String group,
            @RequestParam(name = "groupSummary", required = false) String groupSummary,
            @RequestParam(name = "totalSummary", required = false) String totalSummary) {
        final CSVDataSet csvDataSet = repository.findAll();

        FilterParam rootFilter = null;
        GroupParam[] groupParams = null;
        SummaryParam[] groupSummaryParams = null;
        SummaryParam[] totalSummaryParams = null;

        try {
            final ArrayNode filterParamsNode = StringUtils.isNotBlank(filter)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(filter) : null;
            rootFilter = ParamUtils.toFilterParam(filterParamsNode);

            final ArrayNode groupParamsNode = StringUtils.isNotBlank(group)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(group) : null;
            groupParams = ParamUtils.toGroupParams(groupParamsNode);

            final ArrayNode groupSummaryParamsNode = StringUtils.isNotBlank(groupSummary)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(groupSummary) : null;
            groupSummaryParams = ParamUtils.toSummaryParams(groupSummaryParamsNode);

            final ArrayNode totalSummaryParamsNode = StringUtils.isNotBlank(totalSummary)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(totalSummary) : null;
            totalSummaryParams = ParamUtils.toSummaryParams(totalSummaryParamsNode);
        }
        catch (Exception e) {
            log.error("Failed to parse params.", e);
        }

        if (ArrayUtils.isNotEmpty(groupParams)) {
            log.debug(
                    "Group aggregation data request invoked. filter: {}, group: {}, groupSummary: {}, totalSummary: {}",
                    filter, group, groupSummary, totalSummary);

            try {
                DataFrame dataFrame = new CSVDataSetDataFrame(csvDataSet);
                final DataAggregation aggregation = createDataAggregation(dataFrame, rootFilter,
                        groupParams, groupSummaryParams, totalSummaryParams);
                return aggregation;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        log.debug("Simple data request invoked. skip: {}, take: {}", skip, take);

        final List<String> headers = csvDataSet.getHeaders();
        final List<CSVRecord> records = csvDataSet.getRecords();

        final ArrayNode jsonArray = JacksonUtils.getObjectMapper().createArrayNode();
        int rowIndex = 0;
        for (CSVRecord record : records) {
            if (take > 0 && rowIndex >= take) {
                break;
            }
            jsonArray.add(JacksonUtils.csvRecordToObjectNode(record, headers));
            rowIndex++;
        }

        return jsonArray;
    }

    private boolean isIncludedByRootFilter(final DataRow row, final FilterParam rootFilter) {
        final FilterParam firstChild = rootFilter.getFirstChild();

        if (firstChild == null) {
            return true;
        }

        if (firstChild.hasChild()) {
            return isIncludedByContainerFilter(row, firstChild);
        } else {
            return isIncludedByLeafFilter(row, firstChild);
        }
    }

    private boolean isIncludedByContainerFilter(final DataRow row,
            final FilterParam containerFilter) {
        if (containerFilter.getChildCount() < 2) {
            return false;
        }

        final String operator = containerFilter.getOperator();
        final FilterParam childFilter1 = containerFilter.getChildren().get(0);
        final FilterParam childFilter2 = containerFilter.getChildren().get(1);

        if ("and".equals(operator)) {
            return (childFilter1.hasChild() ? isIncludedByContainerFilter(row, childFilter1)
                    : isIncludedByLeafFilter(row, childFilter1))
                    && (childFilter2.hasChild() ? isIncludedByContainerFilter(row, childFilter2)
                            : isIncludedByLeafFilter(row, childFilter2));
        }

        if ("or".equals(operator)) {
            return (childFilter1.hasChild() ? isIncludedByContainerFilter(row, childFilter1)
                    : isIncludedByLeafFilter(row, childFilter1))
                    || (childFilter2.hasChild() ? isIncludedByContainerFilter(row, childFilter2)
                            : isIncludedByLeafFilter(row, childFilter2));
        }

        return true;
    }

    private boolean isIncludedByLeafFilter(final DataRow row, final FilterParam leafFilter) {
        final String operator = leafFilter.getOperator();
        final String[] selectorTokens = StringUtils.split(leafFilter.getSelector(), ".", 2);
        final String columnName = selectorTokens[0];
        final String dateInterval = selectorTokens.length > 1 ? selectorTokens[1] : null;
        final String comparingValue = leafFilter.getComparingValue();

        final String rowValue = row.getString(columnName, dateInterval);

        if ("=".equals(operator)) {
            if (!Objects.equals(comparingValue, rowValue)) {
                return false;
            }
        }
        else if ("<".equals(operator)) {
            if (comparingValue.compareTo(rowValue) <= 0) {
                return false;
            }
        }
        else if (">".equals(operator)) {
            if (comparingValue.compareTo(rowValue) >= 0) {
                return false;
            }
        }
        else if ("<=".equals(operator)) {
            if (comparingValue.compareTo(rowValue) < 0) {
                return false;
            }
        }
        else if (">=".equals(operator)) {
            if (comparingValue.compareTo(rowValue) > 0) {
                return false;
            }
        }

        return true;
    }

    private DataAggregation createDataAggregation(final DataFrame dataFrame,
            final FilterParam rootFilter, final GroupParam[] groupParams,
            final SummaryParam[] groupSummaryParams, final SummaryParam[] totalSummaryParams)
            throws Exception {
        final DataAggregation aggregation = new DataAggregation();

        for (Iterator<DataRow> it = dataFrame.iterator(); it.hasNext();) {
            final DataRow row = it.next();

            if (rootFilter != null && !isIncludedByRootFilter(row, rootFilter)) {
                continue;
            }

            aggregation.incrementRowCount();
            updateDataGroupSummary(aggregation, row, totalSummaryParams);

            final GroupParam firstGroupParam = groupParams[0];
            String columnName = firstGroupParam.getSelector();
            String dateInterval = firstGroupParam.getGroupInterval();
            String key = row.getString(columnName, dateInterval);

            DataGroup firstGroup = aggregation.getGroup(key);
            if (firstGroup == null) {
                firstGroup = aggregation.addGroup(key);
            }

            firstGroup.incrementRowCount();
            updateDataGroupSummary(firstGroup, row, groupSummaryParams);

            DataGroup parentGroup = firstGroup;

            for (int i = 1; i < groupParams.length; i++) {
                final GroupParam groupParam = groupParams[i];
                columnName = groupParam.getSelector();
                dateInterval = groupParam.getGroupInterval();
                key = row.getString(columnName, dateInterval);

                DataGroup itemGroup = parentGroup.getItem(key);
                if (itemGroup == null) {
                    itemGroup = parentGroup.addItem(key);
                }

                itemGroup.incrementRowCount();
                updateDataGroupSummary(itemGroup, row, groupSummaryParams);

                parentGroup = itemGroup;
            }
        }

        return aggregation;
    }

    private <T> void updateDataGroupSummary(final SummaryContainer<T> summaryContainer, final DataRow dataRow,
            final SummaryParam[] summaryParams) {
        if (ArrayUtils.isEmpty(summaryParams)) {
            return;
        }

        if (summaryContainer.getSummary() == null) {
            for (int i = 0; i < summaryParams.length; i++) {
                summaryContainer.addSummaryValue(new BigDecimal(0));
            }
        }

        final List<BigDecimal> groupSummary = summaryContainer.getSummary();

        for (int i = 0; i < summaryParams.length; i++) {
            final SummaryParam groupSummaryParam = summaryParams[i];
            final String summaryColumnName = groupSummaryParam.getSelector();
            final String summaryType = groupSummaryParam.getSummaryType();

            final BigDecimal summaryValue = groupSummary.get(i);
            final BigDecimal rowValue = dataRow.getBigDecimal(summaryColumnName);

            if ("sum".equals(summaryType)) {
                groupSummary.set(i, summaryValue.add(rowValue));
            }
            else if ("count".equals(summaryType)) {
                groupSummary.set(i, summaryValue.add(new BigDecimal(1)));
            }
            else if ("min".equals(summaryType)) {
                groupSummary.set(i, summaryValue.min(rowValue));
            }
            else if ("max".equals(summaryType)) {
                groupSummary.set(i, summaryValue.max(rowValue));
            }
            else if ("average".equals(summaryType)) {
                final int rowCount = summaryContainer.getRowCount();
                groupSummary.set(i, summaryValue.multiply(new BigDecimal(rowCount - 1))
                        .add(rowValue).divide(new BigDecimal(rowCount)));
            }
        }
    }
}
