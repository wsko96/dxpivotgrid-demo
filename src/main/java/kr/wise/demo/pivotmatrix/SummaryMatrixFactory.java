package kr.wise.demo.pivotmatrix;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.Paging;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotmatrix.impl.DefaultSummaryMatrixImpl;

public final class SummaryMatrixFactory {

    private static Logger log = LoggerFactory.getLogger(SummaryMatrixFactory.class);

    private SummaryMatrixFactory() {
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

        while (offset < flattendedRowDimensions.length) {
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
        return new DefaultSummaryMatrixImpl(matrix.getRowGroupParams(), matrix.getColGroupParams(),
                matrix.getSummaryParams(), rowDimension, matrix.getColSummaryDimension());
    }

    public static SummaryMatrix createSummaryMatrixFromFullyExpandedDataAggregation(
            final DataAggregation dataAggregation, final List<GroupParam> rowGroupParams,
            final List<GroupParam> colGroupParams, final List<SummaryParam> summaryParams) {
        final int rowDimensionMaxDepth = rowGroupParams.size();
        final SummaryDimension rowDimension = new SummaryDimension();
        final SummaryDimension colDimension = new SummaryDimension();

        final List<DataGroup> childGroups = dataAggregation.getChildDataGroups();
        final int childCount = childGroups != null ? childGroups.size() : 0;

        if (childCount > 0) {
            for (DataGroup dataGroup : childGroups) {
                fillRowAndColSummaryDimensions(dataGroup, rowDimensionMaxDepth, rowDimension,
                        colDimension);
            }
        }

        final DefaultSummaryMatrixImpl matrix = new DefaultSummaryMatrixImpl(rowGroupParams,
                colGroupParams, summaryParams, rowDimension, colDimension);

        fillSummaryValuesOfDataGroups(matrix, dataAggregation, rowDimensionMaxDepth);

        calculateEmptyParentSummaryCells(matrix);

        return matrix;
    }

    private static void fillRowAndColSummaryDimensions(final DataGroup baseGroup,
            final int rowDimensionMaxDepth, final SummaryDimension baseRowDimension,
            final SummaryDimension baseColDimension) {
        final int curDepth = baseGroup.getDepth();
        final List<DataGroup> childGroups = baseGroup.getChildDataGroups();
        final int childCount = childGroups != null ? childGroups.size() : 0;

        SummaryDimension childDimension;

        if (curDepth <= rowDimensionMaxDepth) {
            childDimension = baseRowDimension.getChild(baseGroup.getKey());

            if (childDimension == null) {
                childDimension = baseRowDimension
                        .addChild(new SummaryDimension(baseGroup.getKey()));
            }

            if (childGroups != null && !childGroups.isEmpty()) {
                for (DataGroup childDataGroup : childGroups) {
                    fillRowAndColSummaryDimensions(childDataGroup, rowDimensionMaxDepth,
                            childDimension, baseColDimension);
                }
            }
        }
        else {
            childDimension = baseColDimension.getChild(baseGroup.getKey());

            if (childDimension == null) {
                childDimension = baseColDimension
                        .addChild(new SummaryDimension(baseGroup.getKey()));
            }

            if (childCount > 0) {
                for (DataGroup childDataGroup : childGroups) {
                    fillRowAndColSummaryDimensions(childDataGroup, rowDimensionMaxDepth,
                            baseRowDimension, childDimension);
                }
            }
        }
    }

    private static void fillSummaryValuesOfDataGroups(final DefaultSummaryMatrixImpl matrix,
            final AbstractSummaryContainer<?> baseContainer, final int rowDimensionMaxDepth) {
        final List<DataGroup> childGroups = baseContainer.getChildDataGroups();
        final int childCount = childGroups != null ? childGroups.size() : 0;

        if (childCount > 0) {
            for (DataGroup childDataGroup : childGroups) {
                fillSummaryValuesOfDataGroups(matrix, childDataGroup, rowDimensionMaxDepth);
            }
        }

        final Pair<Integer, Integer> pair = findRowColIndexPair(baseContainer, matrix,
                rowDimensionMaxDepth);

        if (pair != null) {
            final List<SummaryValue> summaryValues = temporarilyToSummaryValueList(
                    baseContainer.getSummary());
            final SummaryCell[][] summaryCells = matrix.getSummaryCells();
            summaryCells[pair.getLeft()][pair.getRight()].addSummaryValues(summaryValues);
        }
    }

    private static void calculateEmptyParentSummaryCells(final SummaryMatrix matrix) {
        final SummaryCell[][] summaryCells = matrix.getSummaryCells();

        for (int i = matrix.getRows() - 1; i >= 0; i--) {
            for (int j = matrix.getCols() - 1; j >= 0; j--) {
                final SummaryCell cell = summaryCells[i][j];

                if (!cell.hasSummaryValue()) {
                    final List<Integer> colChildIndices = cell.getColChildCellIndices();
                    final int colChildrenRowIndex = cell.getColChildrenRowIndex();

                    if (colChildIndices != null && !colChildIndices.isEmpty()) {
                        SummaryCell[] childCells = new SummaryCell[colChildIndices.size()];
                        int k = 0;
                        for (Integer index : colChildIndices) {
                            childCells[k++] = summaryCells[colChildrenRowIndex][index];
                        }
                        final List<SummaryValue> summaryValues = aggregateSummaryValuesOfCells(
                                childCells, 0, childCells.length);
                        if (summaryValues != null) {
                            cell.addSummaryValues(summaryValues);
                        }
                    }
                }

                if (!cell.hasSummaryValue()) {
                    final List<Integer> rowChildIndices = cell.getRowChildCellIndices();
                    final int rowChildrenColIndex = cell.getRowChildrenColIndex();

                    if (rowChildIndices != null && !rowChildIndices.isEmpty()) {
                        SummaryCell[] childCells = new SummaryCell[rowChildIndices.size()];
                        int k = 0;
                        for (Integer index : rowChildIndices) {
                            childCells[k++] = summaryCells[index][rowChildrenColIndex];
                        }
                        final List<SummaryValue> summaryValues = aggregateSummaryValuesOfCells(
                                childCells, 0, childCells.length);
                        if (summaryValues != null) {
                            cell.addSummaryValues(summaryValues);
                        }
                    }
                }
            }
        }
    }

    private static List<SummaryValue> aggregateSummaryValuesOfCells(
            final SummaryCell[] summaryCells, final int beginIndex, final int endIndex) {
        BigDecimal sum = new BigDecimal(0);

        for (int i = beginIndex; i < endIndex; i++) {
            final SummaryCell cell = summaryCells[i];

            if (cell == null || !cell.hasSummaryValue()) {
                return null;
            }

            final List<SummaryValue> summaryValues = cell.getSummaryValues();
            // FIXME
            sum = sum.add(summaryValues.get(0).getRepresentingValue());
        }

        SummaryValue summaryValue = new SummaryValue(null, SummaryType.SUM, sum);
        return Arrays.asList(summaryValue);
    }

    private static Pair<Integer, Integer> findRowColIndexPair(
            final AbstractSummaryContainer<?> container, final DefaultSummaryMatrixImpl matrix,
            final int rowDimensionMaxDepth) {
        final String path = container.getPath();
        final String rowPath;
        final String colPath;
        final int offset = StringUtils.ordinalIndexOf(path, SummaryDimension.PATH_DELIMITER,
                rowDimensionMaxDepth + 1);

        if (offset == -1) {
            rowPath = path;
            colPath = "";
        }
        else {
            rowPath = path.substring(0, offset);
            colPath = path.substring(offset);
        }

        final int rowIndex = matrix.getRowIndexByDimensionPath(rowPath);
        final int colIndex = matrix.getColIndexByDimensionPath(colPath);

        if (rowIndex < 0 || colIndex < 0) {
            return null;
        }

        return Pair.of(rowIndex, colIndex);
    }

    // FIXME
    private static List<SummaryValue> temporarilyToSummaryValueList(final List<BigDecimal> values) {
        if (values == null) {
            return null;
        }

        return values.stream().map(v -> new SummaryValue(null, SummaryType.SUM, v))
                .collect(Collectors.toList());
    }
}
