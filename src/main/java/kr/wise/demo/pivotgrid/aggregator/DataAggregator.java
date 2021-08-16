package kr.wise.demo.pivotgrid.aggregator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger log = LoggerFactory.getLogger(DataAggregator.class);

    public DataAggregation createDataAggregation(final DataFrame dataFrame,
            final FilterParam rootFilter, final List<GroupParam> groupParams,
            final List<SummaryParam> groupSummaryParams, final List<SummaryParam> totalSummaryParams,
            final PagingParam pagingParam)
            throws Exception {
        final DataAggregation dataAggregation = new DataAggregation();

        boolean fullPagingMode = false;
        boolean pagingRelevantViewMode = false;
        List<GroupParam> effectivePagingRowGroupParams = Collections.emptyList();

        if (pagingParam != null) {
            final int pageRowGroupCount = pagingParam.getRowGroupCount();
            effectivePagingRowGroupParams = getPagingRowGroupParamsInGroupParams(pagingParam,
                    groupParams);
            final int effectivePagingRowGroupCount = effectivePagingRowGroupParams.size();

            if (effectivePagingRowGroupCount > 0) {
                fullPagingMode = effectivePagingRowGroupCount == pageRowGroupCount;
                pagingRelevantViewMode = effectivePagingRowGroupCount < pageRowGroupCount;
            }
        }

        final DataAggregation pageAggregation = !fullPagingMode && pagingRelevantViewMode
                ? new DataAggregation() : null;

        for (Iterator<DataRow> it = dataFrame.iterator(); it.hasNext();) {
            final DataRow row = it.next();

            if (rootFilter != null && !isIncludedByRootFilter(row, rootFilter)) {
                continue;
            }

            dataAggregation.incrementRowCount();
            updateSummaryContainerSummary(dataAggregation, row, totalSummaryParams);

            contributeDataRowToDataAggregationOnEachGroup(row, dataAggregation, groupParams, groupSummaryParams);

            if (pageAggregation != null) {
                contributeDataRowToDataAggregationOnEachGroup(row, pageAggregation,
                        pagingParam.getRowGroupParams(), Collections.emptyList());
            }
        }

        if (fullPagingMode) {
            DataAggregationUtils.markPaginatedSummaryContainersVisible(dataAggregation, pagingParam);
            dataAggregation.setPagingApplied(true);
        }
        else if (pagingRelevantViewMode) {
            DataAggregationUtils.markPaginatedSummaryContainersVisible(pageAggregation, pagingParam);
            pageAggregation.setPagingApplied(true);

            DataAggregationUtils.resetContainersVisible(dataAggregation, true);
            DataAggregationUtils.markRelevantSummaryContainersVisible(dataAggregation,
                    pageAggregation, pagingParam.getRowGroupParams(), 0);
            dataAggregation.setPagingApplied(true);
        }

        return dataAggregation;
    }

    private void contributeDataRowToDataAggregationOnEachGroup(final DataRow row,
            final DataAggregation dataAggregation, final List<GroupParam> groupParams,
            List<SummaryParam> groupSummaryParams) {
        AbstractSummaryContainer<?> parentGroup = dataAggregation;

        for (GroupParam groupParam : groupParams) {
            parentGroup.setChildDataGroupKey(groupParam.getKey());

            final String columnName = groupParam.getSelector();
            final String dateInterval = groupParam.getGroupInterval();
            final String key = row.getStringValue(columnName, dateInterval);

            DataGroup childDataGroup = parentGroup.getChildDataGroup(key);
            if (childDataGroup == null) {
                childDataGroup = parentGroup.addChildDataGroup(key);
            }

            if (!groupSummaryParams.isEmpty()) {
                childDataGroup.incrementRowCount();
                updateSummaryContainerSummary(childDataGroup, row, groupSummaryParams);
            }

            parentGroup = childDataGroup;
        }
    }

    private <T> void updateSummaryContainerSummary(final SummaryContainer<T> summaryContainer,
            final DataRow dataRow, final List<SummaryParam> summaryParams) {
        final int size = summaryParams != null ? summaryParams.size() : 0;

        if (size == 0) {
            return;
        }

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

    private List<GroupParam> getPagingRowGroupParamsInGroupParams(final PagingParam pagingParam,
            final List<GroupParam> groupParams) {
        List<GroupParam> rowGroupParams = null;

        if (pagingParam != null && pagingParam.getRowGroupCount() > 0) {
            final Set<String> groupParamKeys = groupParams.stream()
                    .map((groupParam) -> groupParam.getKey()).collect(Collectors.toSet());

            for (GroupParam rowGroupParam : pagingParam.getRowGroupParams()) {
                if (groupParamKeys.contains(rowGroupParam.getKey())) {
                    if (rowGroupParams == null) {
                        rowGroupParams = new ArrayList<>();
                    }

                    rowGroupParams.add(rowGroupParam);
                }
            }
        }

        return rowGroupParams != null ? rowGroupParams : Collections.emptyList();
    }
}
