package kr.wise.demo.pivotgrid.aggregator;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.model.SummaryContainer;
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;

@Service
public class DataAggregator {

    public DataAggregation createDataAggregation(final DataFrame dataFrame,
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
            String key = row.getStringValue(columnName, dateInterval);

            DataGroup firstGroup = aggregation.getChildDataGroup(key);
            if (firstGroup == null) {
                firstGroup = aggregation.addChildDataGroup(firstGroupParam, key);
            }

            firstGroup.incrementRowCount();
            updateDataGroupSummary(firstGroup, row, groupSummaryParams);

            DataGroup parentGroup = firstGroup;

            for (int i = 1; i < groupParams.length; i++) {
                final GroupParam groupParam = groupParams[i];
                columnName = groupParam.getSelector();
                dateInterval = groupParam.getGroupInterval();
                key = row.getStringValue(columnName, dateInterval);

                DataGroup itemGroup = parentGroup.getChildDataGroup(key);
                if (itemGroup == null) {
                    itemGroup = parentGroup.addChildDataGroup(groupParam, key);
                }

                itemGroup.incrementRowCount();
                updateDataGroupSummary(itemGroup, row, groupSummaryParams);

                parentGroup = itemGroup;
            }
        }

        return aggregation;
    }

    private <T> void updateDataGroupSummary(final SummaryContainer<T> summaryContainer,
            final DataRow dataRow, final SummaryParam[] summaryParams) {
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
            final BigDecimal rowValue = dataRow.getBigDecimalValue(summaryColumnName);

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

    private boolean isIncludedByRootFilter(final DataRow row, final FilterParam rootFilter) {
        final FilterParam firstChild = rootFilter.getFirstChild();

        if (firstChild == null) {
            return true;
        }

        if (firstChild.hasChild()) {
            return isIncludedByContainerFilter(row, firstChild);
        }
        else {
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

        final String rowValue = row.getStringValue(columnName, dateInterval);

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
}
