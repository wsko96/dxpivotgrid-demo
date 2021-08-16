package kr.wise.demo.pivotgrid.aggregator;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.model.SummaryContainer;
import kr.wise.demo.pivotgrid.model.SummaryType;
import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;

@Service
public class DataAggregator {

    public DataAggregation createDataAggregation(final DataFrame dataFrame,
            final FilterParam rootFilter, final List<GroupParam> groupParams,
            final List<SummaryParam> groupSummaryParams, final List<SummaryParam> totalSummaryParams,
            final PagingParam pagingParam)
            throws Exception {
        final DataAggregation aggregation = new DataAggregation();
        final DataAggregation aggregationForPaging = pagingParam != null
                && pagingParam.getRowGroupCount() > 0 ? new DataAggregation() : null;

        for (Iterator<DataRow> it = dataFrame.iterator(); it.hasNext();) {
            final DataRow row = it.next();

            if (rootFilter != null && !isIncludedByRootFilter(row, rootFilter)) {
                continue;
            }

            aggregation.incrementRowCount();
            updateSummaryContainerSummary(aggregation, row, totalSummaryParams);

            AbstractSummaryContainer<?> parentGroup = aggregation;

            for (GroupParam groupParam : groupParams) {
                final String columnName = groupParam.getSelector();
                final String dateInterval = groupParam.getGroupInterval();
                final String key = row.getStringValue(columnName, dateInterval);

                DataGroup childDataGroup = parentGroup.getChildDataGroup(key);
                if (childDataGroup == null) {
                    childDataGroup = parentGroup.addChildDataGroup(key);
                }

                childDataGroup.incrementRowCount();
                updateSummaryContainerSummary(childDataGroup, row, groupSummaryParams);

                parentGroup = childDataGroup;
            }
        }

        return aggregation;
    }

    private void f() {
        
    }

    private <T> void updateSummaryContainerSummary(final SummaryContainer<T> summaryContainer,
            final DataRow dataRow, final List<SummaryParam> summaryParams) {
        if (summaryParams.isEmpty()) {
            return;
        }

        final int size = summaryParams.size();

        if (summaryContainer.getSummary() == null) {
             for (int i = 0; i < size; i++) {
                summaryContainer.addSummaryValue(new BigDecimal(0));
            }
        }

        final List<BigDecimal> groupSummary = summaryContainer.getSummary();

        for (int i = 0; i < size; i++) {
            final SummaryParam groupSummaryParam = summaryParams.get(i);
            final String summaryColumnName = groupSummaryParam.getSelector();
            final String summaryTypeName = StringUtils
                    .upperCase(groupSummaryParam.getSummaryType());
            final SummaryType summaryType = StringUtils.isEmpty(summaryTypeName) ? null
                    : SummaryType.valueOf(summaryTypeName);

            final BigDecimal summaryValue = groupSummary.get(i);
            final BigDecimal rowValue = dataRow.getBigDecimalValue(summaryColumnName);

            switch (summaryType) {
            case SUM:
                groupSummary.set(i, summaryValue.add(rowValue));
                break;
            case COUNT:
                groupSummary.set(i, summaryValue.add(new BigDecimal(1)));
                break;
            case MIN:
                groupSummary.set(i, summaryValue.min(rowValue));
                break;
            case MAX:
                groupSummary.set(i, summaryValue.max(rowValue));
                break;
            case AVERAGE:
                final int rowCount = summaryContainer.getRowCount();
                groupSummary.set(i, summaryValue.multiply(new BigDecimal(rowCount - 1))
                        .add(rowValue).divide(new BigDecimal(rowCount)));
                break;
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
