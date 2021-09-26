package kr.wise.demo.pivotmatrix;

import java.util.LinkedList;
import java.util.List;

import kr.wise.demo.pivotgrid.model.Paging;
import kr.wise.demo.pivotmatrix.impl.DefaultSummaryMatrixImpl;

public final class SummaryMatrixUtils {

    private SummaryMatrixUtils() {
    }

    public static SummaryMatrix slicePageSummaryMatrix(final SummaryMatrix matrix,
            final Paging paging, final int rowDimensionMaxDepth) {
        if (paging.getOffset() < 0 || paging.getLimit() <= 0) {
            return createEmptyPageSummaryMatrix(matrix);
        }

        final int distinctRows = matrix.getRows();

        final List<Integer> pageableRowIndices = new LinkedList<>();

        for (int i = 0; i < distinctRows; i++) {
            pageableRowIndices.add(i);
        }

        insertAncestorRowIndicesForPaging(pageableRowIndices, matrix, paging.getLimit(),
                rowDimensionMaxDepth);

        final int pageableTotalRows = pageableRowIndices.size();

        final int beginIndex = paging.getOffset();
        final int endIndex = Math.min(pageableTotalRows, beginIndex + paging.getLimit());

        final List<Integer> pagedRowIndices = pageableRowIndices.subList(beginIndex, endIndex);
        final int count = pagedRowIndices.size();

        paging.setTotal(pageableTotalRows);
        paging.setDistinctTotal(distinctRows);
        paging.setCount(count);

        if (beginIndex >= pageableTotalRows || count <= 0) {
            return createEmptyPageSummaryMatrix(matrix);
        }

        return matrix.sliceRows(pagedRowIndices);
    }

    private static void insertAncestorRowIndicesForPaging(final List<Integer> pageRowIndices,
            final SummaryMatrix matrix, final int pageSize, final int rowDimensionMaxDepth) {
        if (pageSize < rowDimensionMaxDepth + 1) {
            // If pageSize is smaller than group dimension count including the root,
            // then it's dangerous to continue due to potential infinite loop. So stop here then.
            return;
        }

        final SummaryDimension[] flattendedRowDimensions = matrix.getRowFlattendSummaryDimensions();

        int offset = 0;

        while (offset < pageRowIndices.size()) {
            final SummaryDimension rowDimension = flattendedRowDimensions[offset];
            SummaryDimension parentRowDimension = rowDimension.getParent();

            while (parentRowDimension != null) {
                final int parentRowIndex = matrix
                        .getRowIndexByDimensionPath(parentRowDimension.getPath());

                if (parentRowIndex >= 0) {
                    pageRowIndices.add(offset, parentRowIndex);
                    parentRowDimension = parentRowDimension.getParent();
                }
            }

            offset += pageSize;
        }
    }

    private static SummaryMatrix createEmptyPageSummaryMatrix(final SummaryMatrix matrix) {
        final SummaryDimension rowDimension = new SummaryDimension();
        return new DefaultSummaryMatrixImpl(rowDimension, matrix.getColSummaryDimension());
    }

}
