package kr.wise.demo.pivotmatrix.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
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

    public static SummaryMatrix createSummaryMatrix(final DataAggregation dataAggregation,
            final int rowDimensionMaxDepth) {
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

        fillSummaryValuesToCells(matrix, dataAggregation, rowDimensionMaxDepth);

        calculateEmptySummaryCellsBySummaryContainers(matrix, dataAggregation, rowDimensionMaxDepth);

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

    private static void fillSummaryValuesToCells(final SummaryMatrix matrix,
            final AbstractSummaryContainer<?> baseContainer, final int rowDimensionMaxDepth) {
        final Pair<Integer, Integer> pair = getRowColIndexPair(baseContainer, matrix,
                rowDimensionMaxDepth);
        final int rowIndex = pair.getLeft();
        final int colIndex = pair.getRight();

        if (rowIndex >= 0 && colIndex >= 0) {
            final List<SummaryValue> summaryValues = temporarilyToSummaryValueList(
                    baseContainer.getSummary());

            if (summaryValues != null) {
                matrix.summaryCells[rowIndex][colIndex] = new SummaryCell()
                        .addSummaryValues(summaryValues);
            }
        }

        final List<DataGroup> childGroups = baseContainer.getChildDataGroups();
        final int childCount = childGroups != null ? childGroups.size() : 0;

        if (childCount > 0) {
            for (DataGroup childDataGroup : childGroups) {
                fillSummaryValuesToCells(matrix, childDataGroup, rowDimensionMaxDepth);
            }
        }
    }

    private static void calculateEmptySummaryCellsBySummaryContainers(final SummaryMatrix matrix,
            final AbstractSummaryContainer<?> baseContainer, final int rowDimensionMaxDepth) {
        final List<DataGroup> childGroups = baseContainer.getChildDataGroups();
        final int childCount = childGroups != null ? childGroups.size() : 0;

        if (childCount > 0) {
            for (DataGroup childDataGroup : childGroups) {
                calculateEmptySummaryCellsBySummaryContainers(matrix, childDataGroup, rowDimensionMaxDepth);
            }

            final Pair<Integer, Integer> pair = getRowColIndexPair(baseContainer, matrix,
                    rowDimensionMaxDepth);
            final int rowIndex = pair.getLeft();
            final int colIndex = pair.getRight();

            if (rowIndex >= 0 && colIndex >= 0) {
                SummaryCell cell = matrix.summaryCells[rowIndex][colIndex];

                if (cell == null) {
                    final List<SummaryValue> summaryValues = calculateSummaryValuesOfChildren(
                            matrix, childGroups, rowDimensionMaxDepth);

                    if (summaryValues != null) {
                        cell = new SummaryCell().addSummaryValues(summaryValues);
                        matrix.summaryCells[rowIndex][colIndex] = cell;
                    }
                }
            }
        }
    }

    private static List<SummaryValue> calculateSummaryValuesOfChildren(final SummaryMatrix matrix,
            final List<DataGroup> childGroups, final int rowDimensionMaxDepth) {
        BigDecimal sum = new BigDecimal(0);

        for (DataGroup childGroup : childGroups) {
            final Pair<Integer, Integer> pair = getRowColIndexPair(childGroup, matrix,
                    rowDimensionMaxDepth);
            final int rowIndex = pair.getLeft();
            final int colIndex = pair.getRight();

            if (rowIndex >= 0 && colIndex >= 0) {
                final SummaryCell cell = matrix.summaryCells[rowIndex][colIndex];

                if (cell == null) {
                    return null;
                }

                final List<SummaryValue> summaryValues = cell.getSummaryValues();
                // FIXME
                sum = sum.add(summaryValues.get(0).getRepresentingValue());
            }
        }

        SummaryValue summaryValue = new SummaryValue(null, SummaryType.SUM, sum);
        return Arrays.asList(summaryValue);
    }

    private static Pair<Integer, Integer> getRowColIndexPair(
            final AbstractSummaryContainer<?> container, final SummaryMatrix matrix,
            final int rowDimensionMaxDepth) {
        Pair<Integer, Integer> pair = (Pair<Integer, Integer>) container
                .getAttribute("rowColIndexPair");

        if (pair != null) {
            return pair;
        }

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

        pair = Pair.of(rowIndex, colIndex);
        container.setAttribute("rowColIndexPair", pair);

        return pair;
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
