package kr.wise.demo.pivotmatrix.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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

        if (childGroups != null && !childGroups.isEmpty()) {
            for (DataGroup dataGroup : childGroups) {
                fillRowAndColSummaryDimensions(dataGroup, rowDimensionMaxDepth, rowDimension,
                        colDimension);
            }
        }

        final SummaryMatrix matrix = new SummaryMatrix(rowDimension, colDimension);

        fillSummaryValuesToCells(matrix, dataAggregation, rowDimensionMaxDepth);

        return matrix;
    }

    private static void fillRowAndColSummaryDimensions(final DataGroup baseGroup,
            final int rowDimensionMaxDepth, final SummaryDimension baseRowDimension,
            final SummaryDimension baseColDimension) {
        final int curDepth = baseGroup.getDepth();
        final List<DataGroup> childGroups = baseGroup.getChildDataGroups();

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

            if (childGroups != null && !childGroups.isEmpty()) {
                for (DataGroup childDataGroup : childGroups) {
                    fillRowAndColSummaryDimensions(childDataGroup, rowDimensionMaxDepth,
                            baseRowDimension, childDimension);
                }
            }
        }
    }

    private static void fillSummaryValuesToCells(final SummaryMatrix matrix,
            final AbstractSummaryContainer<?> baseContainer, final int rowDimensionMaxDepth) {
        final String path = baseContainer.getPath();
        final String rowPath;
        final String colPath;
        final int offset = StringUtils.ordinalIndexOf(path, "/", rowDimensionMaxDepth + 1);
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

        if (rowIndex >= 0 && colIndex >= 0) {
            matrix.summaryCells[rowIndex][colIndex] = new SummaryCell()
                    .addSummaryValues(toSummaryValueList(baseContainer.getSummary()));
        }

        final List<DataGroup> childGroups = baseContainer.getChildDataGroups();

        if (childGroups != null && !childGroups.isEmpty()) {
            for (DataGroup childDataGroup : childGroups) {
                fillSummaryValuesToCells(matrix, childDataGroup, rowDimensionMaxDepth);
            }
        }
    }

    private static List<SummaryValue> toSummaryValueList(final List<BigDecimal> values) {
        if (values == null) {
            return null;
        }

        return values.stream().map(v -> new SummaryValue(null, SummaryType.SUM, v))
                .collect(Collectors.toList());
    }
}
