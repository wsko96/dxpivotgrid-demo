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
        fillSummaryContainersToFlatList(list, dataAggregation, maxDepth, true);
        insertAncestorsForPaging(list, limit, maxDepth);

        final int total = list.size();

        dataAggregation.getPaging().setTotal(total);

        if (offset >= total) {
            return;
        }

        dataAggregation.getPaging().setOffset(offset);
        dataAggregation.getPaging().setLimit(limit);

        final int endIndex = Math.min(offset + limit, total);
        final List<AbstractSummaryContainer<?>> pagedList = list.subList(offset, endIndex);
        final int pageRowCount = Math.min(limit, pagedList.size());
        dataAggregation.getPaging().setCount(pageRowCount);

        int i = 0;
        for (AbstractSummaryContainer<?> container : pagedList) {
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

    public static void insertAncestorsForPaging(final List<AbstractSummaryContainer<?>> list,
            final int pageSize, final int maxDepth) {
        if (pageSize < maxDepth + 1) {
            // If pageSize is smaller than group dimension count including the root,
            // then it's dangerous to continue due to potential infinite loop. So stop here then.
            return;
        }

        int offset = 0;

        while (offset < list.size()) {
            final AbstractSummaryContainer<?> firstContainerInPage = list.get(offset);
            AbstractSummaryContainer<?> parent = firstContainerInPage.getParent();

            while (parent != null) {
                list.add(offset, parent);
                parent = parent.getParent();
            }

            offset += pageSize;
        }
    }
}
