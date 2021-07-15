package kr.wise.demo.pivotgrid.service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.GroupSummaryParam;
import kr.wise.demo.pivotgrid.repository.SalesDataRepository;
import kr.wise.demo.pivotgrid.util.JacksonUtils;
import kr.wise.demo.pivotgrid.util.ParamUtils;

@RestController
public class SalesDataService {

    private static Logger log = LoggerFactory.getLogger(SalesDataService.class);

    private static final String[] SALES_COLUMN_NAMES = { "id", "region", "country", "city",
            "amount", "date", };

    @Autowired
    private SalesDataRepository repository;

    @GetMapping("/sales")
    public Object all(@RequestParam(name = "skip", required = false, defaultValue = "0") int skip,
            @RequestParam(name = "take", required = false, defaultValue = "0") int take,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "group", required = false) String group,
            @RequestParam(name = "groupSummary", required = false) String groupSummary,
            @RequestParam(name = "totalSummary", required = false) String totalSummary) {
        ArrayNode dataArray = repository.findAll();

        FilterParam[] filterParams = null;
        GroupParam[] groupParams = null;
        GroupSummaryParam[] groupSummaryParams = null;

        try {
            final ArrayNode filterParamsNode = StringUtils.isNotBlank(filter)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(filter) : null;
            filterParams = ParamUtils.toFilterParams(filterParamsNode);

            final ArrayNode groupParamsNode = StringUtils.isNotBlank(group)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(group) : null;
            groupParams = ParamUtils.toGroupParams(groupParamsNode);

            final ArrayNode groupSummaryParamsNode = StringUtils.isNotBlank(groupSummary)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(groupSummary) : null;
            groupSummaryParams = ParamUtils.toGroupSummaryParams(groupSummaryParamsNode);
        }
        catch (Exception e) {
            log.error("Failed to parse group and/or groupSummary params.", e);
        }

        if (ArrayUtils.isNotEmpty(groupParams)) {
            log.debug(
                    "Group aggregation data request invoked. filter: {}, group: {}, groupSummary: {}, totalSummary: {}",
                    filter, group, groupSummary, totalSummary);

            try {
                DataFrame dataFrame = new ArrayNodeDataFrame(dataArray, SALES_COLUMN_NAMES);
                final DataAggregation aggregation = createDataAggregation(dataFrame, filterParams,
                        groupParams, groupSummaryParams);
                return aggregation;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        log.debug("Simple data request invoked. skip: {}, take: {}", skip, take);

        final int size = dataArray.size();

        if (take > 0) {
            final int beginIndex = Math.min(Math.max(skip, 0), size);
            final int endIndex = Math.min(Math.max(take, 0), size - beginIndex);

            if (beginIndex != 0 || endIndex != size) {
                final ArrayNode subArray = JacksonUtils.getObjectMapper().createArrayNode();
                for (int i = beginIndex; i < endIndex; i++) {
                    subArray.add(dataArray.get(i));
                }
                return subArray;
            }
        }

        return dataArray;
    }

    private boolean isFilteredIn(final DataRow row, final FilterParam[] filterParams) {
        for (FilterParam filterParam : filterParams) {
            final String selector = filterParam.getSelector();
            final String operator = filterParam.getOperator();
            final String comparingValue = filterParam.getComparingValue();

            final String rowValue = row.getString(selector);

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
        }

        return true;
    }

    private DataAggregation createDataAggregation(final DataFrame dataFrame,
            final FilterParam[] filterParams, final GroupParam[] groupParams,
            final GroupSummaryParam[] groupSummaryParams) throws Exception {
        final DataAggregation aggregation = new DataAggregation();

        for (Iterator<DataRow> it = dataFrame.iterator(); it.hasNext();) {
            final DataRow row = it.next();

            if (ArrayUtils.isNotEmpty(filterParams) && !isFilteredIn(row, filterParams)) {
                continue;
            }

            GroupParam groupParam = groupParams[0];
            String columnName = groupParam.getSelector();
            String dateInterval = groupParam.getGroupInterval();
            String key = row.getString(columnName, dateInterval);

            DataGroup group = aggregation.getGroup(key);
            if (group == null) {
                group = aggregation.addGroup(key);
            }

            for (int i = 1; i < groupParams.length; i++) {
                groupParam = groupParams[i];
                columnName = groupParam.getSelector();
                dateInterval = groupParam.getGroupInterval();
                key = row.getString(columnName, dateInterval);

                DataGroup item = group.getItem(key);
                if (item == null) {
                    item = group.addItem(key);
                }

                item.incrementRowCount();
                updateDataGroupSummary(item, row, groupSummaryParams);
            }

            group.incrementRowCount();
            updateDataGroupSummary(group, row, groupSummaryParams);
        }

        return aggregation;
    }

    private void updateDataGroupSummary(final DataGroup dataGroup, final DataRow dataRow,
            final GroupSummaryParam[] groupSummaryParams) {
        if (ArrayUtils.isEmpty(groupSummaryParams)) {
            return;
        }

        if (dataGroup.getSummary() == null) {
            for (int i = 0; i < groupSummaryParams.length; i++) {
                dataGroup.addSummaryValue(new BigDecimal(0));
            }
        }

        final List<BigDecimal> groupSummary = dataGroup.getSummary();

        for (int i = 0; i < groupSummaryParams.length; i++) {
            final GroupSummaryParam groupSummaryParam = groupSummaryParams[i];
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
                final int rowCount = dataGroup.getRowCount();
                groupSummary.set(i, summaryValue.multiply(new BigDecimal(rowCount - 1))
                        .add(rowValue).divide(new BigDecimal(rowCount)));
            }
        }
    }
}
