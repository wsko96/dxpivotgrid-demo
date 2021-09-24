package kr.wise.demo.pivotmatrix.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataGroup;

public final class SummaryMatrixUtils {

    private static Logger log = LoggerFactory.getLogger(SummaryMatrixUtils.class);

    private SummaryMatrixUtils() {
    }

    public static SummaryMatrix createSummaryMatrixFromFullyExpandedDataAggregation(
            final DataAggregation dataAggregation, final int rowDimensionMaxDepth) {
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

        final SummaryMatrix matrix = new SummaryMatrix(rowDimension, colDimension);

        final Map<AbstractSummaryContainer<?>, Pair<Integer, Integer>> rowColIndexPairCacheMap = new HashMap<>();
        fillSummaryValuesOfLeafDataGroups(matrix, dataAggregation, rowDimensionMaxDepth,
                rowColIndexPairCacheMap);

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

    private static void fillSummaryValuesOfLeafDataGroups(final SummaryMatrix matrix,
            final AbstractSummaryContainer<?> baseContainer, final int rowDimensionMaxDepth,
            final Map<AbstractSummaryContainer<?>, Pair<Integer, Integer>> rowColIndexPairCacheMap) {
        final List<DataGroup> childGroups = baseContainer.getChildDataGroups();
        final int childCount = childGroups != null ? childGroups.size() : 0;

        if (childCount > 0) {
            for (DataGroup childDataGroup : childGroups) {
                fillSummaryValuesOfLeafDataGroups(matrix, childDataGroup, rowDimensionMaxDepth,
                        rowColIndexPairCacheMap);
            }
        }
        else {
            Pair<Integer, Integer> pair = rowColIndexPairCacheMap.get(baseContainer);

            if (pair == null) {
                pair = resolveRowColIndexPair(baseContainer, matrix, rowDimensionMaxDepth);
                rowColIndexPairCacheMap.put(baseContainer, pair);
            }

            if (pair != null) {
                final List<SummaryValue> summaryValues = temporarilyToSummaryValueList(
                        baseContainer.getSummary());
                matrix.summaryCells[pair.getLeft()][pair.getRight()]
                        .addSummaryValues(summaryValues);
            }
        }
    }

    private static void calculateEmptyParentSummaryCells(final SummaryMatrix matrix) {
        for (int i = matrix.getRows() - 1; i >= 0; i--) {
            for (int j = matrix.getCols() - 1; j >= 0; j--) {
                final SummaryCell cell = matrix.summaryCells[i][j];

                if (!cell.hasSummaryValue()) {
                    final List<Integer> colChildIndices = cell.getColChildCellIndices();
                    final int colChildrenRowIndex = cell.getColChildrenRowIndex();

                    if (colChildIndices != null && !colChildIndices.isEmpty()) {
                        SummaryCell[] childCells = new SummaryCell[colChildIndices.size()];
                        int k = 0;
                        for (Integer index : colChildIndices) {
                            childCells[k++] = matrix.summaryCells[colChildrenRowIndex][index];
                        }
                        final List<SummaryValue> summaryValues = aggregateSummaryValuesOfCells(
                                childCells, 0, childCells.length);
                        if (summaryValues != null) {
                            cell.addSummaryValues(summaryValues);
                        }
                    }

                    if (!cell.hasSummaryValue()) {
                        final List<Integer> rowChildIndices = cell.getRowChildCellIndices();
                        final int rowChildrenColIndex = cell.getRowChildrenColIndex();

                        if (rowChildIndices != null && !rowChildIndices.isEmpty()) {
                            SummaryCell[] childCells = new SummaryCell[rowChildIndices.size()];
                            int k = 0;
                            for (Integer index : rowChildIndices) {
                                childCells[k++] = matrix.summaryCells[index][rowChildrenColIndex];
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

    private static Pair<Integer, Integer> resolveRowColIndexPair(
            final AbstractSummaryContainer<?> container, final SummaryMatrix matrix,
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
