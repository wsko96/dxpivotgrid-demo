package kr.wise.demo.pivotgrid.aggregator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;

final class DataAggregationUtils {

    private static Logger log = LoggerFactory.getLogger(DataAggregationUtils.class);

    private DataAggregationUtils() {
    }

    static void resetContainersVisibility(final AbstractSummaryContainer<?> base,
            final boolean visible) {
        base.setVisible(visible);
        final List<DataGroup> childDataGroups = base.getChildDataGroups();
        if (childDataGroups != null) {
            for (DataGroup childDataGroup : childDataGroups) {
                resetContainersVisibility(childDataGroup, visible);
            }
        }
    }

    static void markRelevantSummaryContainersVisible(
            final AbstractSummaryContainer<?> parentDataGroup,
            final AbstractSummaryContainer<?> parentPageGroup,
            final List<GroupParam> rowGroupParams, final int rowGroupParamIndex) {
        final GroupParam rowGroupParam = rowGroupParamIndex <= rowGroupParams.size() - 1
                ? rowGroupParams.get(rowGroupParamIndex) : null;

        log.debug(
                "rowGroupParam.getKey(): {}, parentDataGroup.getChildDataGroupKey(): {}, parentPageGroup.getChildDataGroupKey(): {}",
                rowGroupParam.getKey(), parentDataGroup.getChildDataGroupKey(),
                parentPageGroup.getChildDataGroupKey());

        if (!StringUtils.equals(rowGroupParam.getKey(), parentDataGroup.getChildDataGroupKey())
                || !StringUtils.equals(rowGroupParam.getKey(),
                        parentPageGroup.getChildDataGroupKey())) {
            return;
        }

        final List<DataGroup> childPageGroups = parentPageGroup.getChildDataGroups(true);
        final int childPageGroupCount = childPageGroups != null ? childPageGroups.size() : 0;

        final List<DataGroup> childDataGroups = parentDataGroup.getChildDataGroups();
        final int childDataGroupCount = childDataGroups != null ? childDataGroups.size() : 0;

        final Set<String> pageGroupKeyValues = childPageGroups.stream().map((g) -> g.getKey())
                .collect(Collectors.toSet());

        for (int i = 0; i < childDataGroupCount; i++) {
            final DataGroup childDataGroup = childDataGroups.get(i);
            final String childDataGroupKey = childDataGroup.getKey();

            if (!pageGroupKeyValues.contains(childDataGroupKey)) {
                childDataGroup.setVisible(false);
            }
            else {
                final DataGroup childPageGroup = (i <= childPageGroupCount - 1)
                        ? childPageGroups.get(i) : null;

                if (childPageGroup != null) {
                    markRelevantSummaryContainersVisible(childDataGroup, childPageGroup,
                            rowGroupParams, rowGroupParamIndex + 1);
                }
            }
        }
    }

    static void markPaginatedSummaryContainersVisible(final DataAggregation dataAggregation,
            final PagingParam pagingParam) {
        final int offset = pagingParam.getOffset();
        final int limit = pagingParam.getLimit();
        final List<GroupParam> rowGroupParams = pagingParam.getRowGroupParams();
        final int maxDepth = rowGroupParams.size();

        final List<AbstractSummaryContainer<?>> list = new LinkedList<>();
        DataAggregationUtils.fillSummaryContainersToFlatList(list, dataAggregation, maxDepth, true);

        final int total = list.size();

        dataAggregation.getPaging().setTotal(total);

        if (offset >= total) {
            return;
        }

        dataAggregation.getPaging().setOffset(offset);
        dataAggregation.getPaging().setLimit(limit);

        final int endIndex = Math.min(offset + limit, total);
        final List<AbstractSummaryContainer<?>> candidateList = list.subList(offset, endIndex);

        final AbstractSummaryContainer<?>[] containers = candidateList
                .toArray(new AbstractSummaryContainer<?>[candidateList.size()]);

        for (AbstractSummaryContainer<?> container : containers) {
            insertParentBeforeContainer(container, candidateList);
        }

        final int pageRowCount = Math.min(limit, candidateList.size());
        dataAggregation.getPaging().setCount(pageRowCount);

        int i = 0;
        for (AbstractSummaryContainer<?> container : candidateList) {
            if (++i > pageRowCount) {
                break;
            }

            if (container.getDepth() == maxDepth) {
                resetContainersVisibility(container, true);
            }
            else {
                container.setVisible(true);
            }
        }
    }

    static void fillSummaryContainersToFlatList(final List<AbstractSummaryContainer<?>> list,
            final AbstractSummaryContainer<?> base, final int maxDepth, final boolean parentFirst) {
        if (base.getDepth() > maxDepth) {
            return;
        }

        if (parentFirst) {
            list.add(base);
        }

        final List<DataGroup> childDataGroups = base.getChildDataGroups();
        if (childDataGroups != null) {
            for (DataGroup childDataGroup : childDataGroups) {
                fillSummaryContainersToFlatList(list, childDataGroup, maxDepth, parentFirst);
            }
        }

        if (!parentFirst) {
            list.add(base);
        }
    }

    private static void insertParentBeforeContainer(final AbstractSummaryContainer<?> container,
            final List<AbstractSummaryContainer<?>> candidateList) {
        final AbstractSummaryContainer<?> parent = container.getParent();

        if (parent != null && !candidateList.contains(parent)) {
            final int offset = candidateList.indexOf(container);
            candidateList.add(offset, parent);

            insertParentBeforeContainer(parent, candidateList);
        }
    }
}
