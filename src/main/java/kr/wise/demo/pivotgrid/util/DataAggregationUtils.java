package kr.wise.demo.pivotgrid.util;

import java.util.LinkedList;
import java.util.List;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;

public final class DataAggregationUtils {

    private DataAggregationUtils() {
    }

    public static void resetContainersVisible(final AbstractSummaryContainer<?> base,
            final boolean visible) {
        base.setVisible(visible);
        final List<DataGroup> childDataGroups = base.getChildDataGroups();
        if (childDataGroups != null) {
            for (DataGroup childDataGroup : childDataGroups) {
                resetContainersVisible(childDataGroup, visible);
            }
        }
    }

    public static void markPaginatedSummaryContainersVisible(final DataAggregation source,
            final PagingParam pagingParam, final List<GroupParam> effectiveRowGroupParams) {
        final int offset = pagingParam.getOffset();
        final int limit = pagingParam.getLimit();
        final List<GroupParam> rowGroupParams = pagingParam.getRowGroupParams();
        final int maxDepth = rowGroupParams.size();
        final boolean fullPagingMode = maxDepth == effectiveRowGroupParams.size();

        final List<AbstractSummaryContainer<?>> list = new LinkedList<>();
        DataAggregationUtils.fillSummaryContainersToFlatList(list, source, maxDepth, true);
        final int total = list.size();

        source.getPaging().setTotal(total);

        if (offset >= total) {
            return;
        }

        source.getPaging().setOffset(offset);
        source.getPaging().setLimit(limit);

        final int endIndex = Math.min(offset + limit, total);
        final List<AbstractSummaryContainer<?>> candidateList = list.subList(offset, endIndex);

        final AbstractSummaryContainer<?>[] containers = candidateList
                .toArray(new AbstractSummaryContainer<?>[candidateList.size()]);

        for (AbstractSummaryContainer<?> container : containers) {
            insertParentBeforeContainer(container, candidateList);
        }

        final int pageRowCount = Math.min(limit, candidateList.size());
        source.getPaging().setCount(pageRowCount);

        int i = 0;
        for (AbstractSummaryContainer<?> container : candidateList) {
            if (++i > pageRowCount) {
                break;
            }
            container.setVisible(true);
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
