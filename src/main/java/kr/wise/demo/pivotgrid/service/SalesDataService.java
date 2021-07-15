package kr.wise.demo.pivotgrid.service;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.GroupSummaryParam;
import kr.wise.demo.pivotgrid.repository.SalesDataRepository;
import kr.wise.demo.pivotgrid.util.JacksonUtils;
import kr.wise.demo.pivotgrid.util.ParamUtils;

@RestController
public class SalesDataService {

    private static final String[] SALES_COLUMN_NAMES = { "id", "region", "country", "city",
            "amount", "date", };

    @Autowired
    private SalesDataRepository repository;

    /**
     * 
     * $$$$$ filter: $$$$$ group:
     * [{"selector":"region","isExpanded":false},{"selector":"date","groupInterval":"year","isExpanded":false}] $$$$$
     * totalSummary: [{"selector":"amount","summaryType":"sum"}] $$$$$ groupSummary:
     * [{"selector":"amount","summaryType":"sum"}]
     * 
     * $$$$$ filter: $$$$$ group: [{"selector":"date","groupInterval":"year","isExpanded":false}] $$$$$ totalSummary: []
     * $$$$$ groupSummary: [{"selector":"amount","summaryType":"sum"}]
     * 
     * @param skip
     * @param take
     * @param filter
     * @param group
     * @param totalSummary
     * @param groupSummary
     * @return
     */
    @GetMapping("/sales")
    public Object all(@RequestParam(name = "skip", required = false, defaultValue = "0") int skip,
            @RequestParam(name = "take", required = false, defaultValue = "0") int take,
            @RequestParam(name = "filter", required = false) String filter,
            @RequestParam(name = "group", required = false) String group,
            @RequestParam(name = "totalSummary", required = false) String totalSummary,
            @RequestParam(name = "groupSummary", required = false) String groupSummary) {
        final ArrayNode dataArray = repository.findAll();

        GroupParam[] groupParams = null;
        GroupSummaryParam[] groupSummaryParams = null;

        try {
            final ArrayNode groupParamsNode = StringUtils.isNotBlank(group)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(group) : null;
            groupParams = ParamUtils.toGroupParams(groupParamsNode);

            final ArrayNode groupSummaryParamsNode = StringUtils.isNotBlank(groupSummary)
                    ? (ArrayNode) JacksonUtils.getObjectMapper().readTree(groupSummary) : null;
            groupSummaryParams = ParamUtils.toGroupSummaryParams(groupSummaryParamsNode);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if (ArrayUtils.isNotEmpty(groupParams)) {
            try {
                final DataFrame dataFrame = new ArrayNodeDataFrame(dataArray, SALES_COLUMN_NAMES);
                final DataAggregation aggregation = createDataAggregation(dataFrame, groupParams,
                        groupSummaryParams);
                return aggregation;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

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

    private DataAggregation createDataAggregation(final DataFrame dataFrame,
            final GroupParam[] groupParams, final GroupSummaryParam[] groupSummaryParams)
            throws Exception {
        final DataAggregation aggregation = new DataAggregation();

        for (Iterator<DataRow> it = dataFrame.iterator(); it.hasNext();) {
            final DataRow row = it.next();

            GroupParam groupParam = groupParams[0];
            String columnName = groupParam.getSelector();
            String dateInterval = groupParam.getGroupInterval();
            String key = row.getString(columnName, dateInterval);

            DataGroup group = aggregation.getGroup(key);
            if (group == null) {
                group = aggregation.addGroup(key);
            }

            DataGroup prevItem = group;

            for (int i = 1; i < groupParams.length; i++) {
                groupParam = groupParams[i];
                columnName = groupParam.getSelector();
                dateInterval = groupParam.getGroupInterval();
                key = row.getString(columnName, dateInterval);

                DataGroup item = prevItem.getItem(key);
                if (item == null) {
                    item = prevItem.addItem(key);
                }

                for (GroupSummaryParam groupSummaryParam : groupSummaryParams) {
                    final String summaryColumnName = groupSummaryParam.getSelector();
                    final BigDecimal value = row.getBigDecimal(summaryColumnName);
                    final List<BigDecimal> summary = item.getSummary();
                    if (summary.isEmpty()) {
                        summary.add(value);
                    }
                    else {
                        final BigDecimal sum = summary.get(0);
                        summary.set(0, sum.add(value));
                    }
                }
            }

            for (GroupSummaryParam groupSummaryParam : groupSummaryParams) {
                final String summaryColumnName = groupSummaryParam.getSelector();
                final BigDecimal value = row.getBigDecimal(summaryColumnName);
                final List<BigDecimal> summary = group.getSummary();
                if (summary.isEmpty()) {
                    summary.add(value);
                }
                else {
                    final BigDecimal sum = summary.get(0);
                    summary.set(0, sum.add(value));
                }
            }
        }

        return aggregation;
    }
}
