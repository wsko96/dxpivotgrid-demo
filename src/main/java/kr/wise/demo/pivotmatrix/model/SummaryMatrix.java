package kr.wise.demo.pivotmatrix.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryMatrix {

    private static Logger log = LoggerFactory.getLogger(SummaryMatrix.class);

    private final SummaryDimension rowSummaryDimension;
    private final SummaryDimension colSummaryDimension;
    private final SummaryDimension[] rowFlattenedSummaryDimensions;
    private final SummaryDimension[] colFlattenedSummaryDimensions;
    private final Map<String, Pair<Integer, SummaryDimension>> rowSummaryDimensionPathMap = new HashMap<>();
    private final Map<String, Pair<Integer, SummaryDimension>> colSummaryDimensionPathMap = new HashMap<>();

    private final int rows;
    private final int cols;
    final SummaryCell[][] summaryCells;

    public SummaryMatrix(final SummaryDimension rowSummaryDimension, final SummaryDimension colSummaryDimension) {
        this.rowSummaryDimension = rowSummaryDimension;
        this.colSummaryDimension = colSummaryDimension;

        final List<SummaryDimension> flattendList = new LinkedList<>();
        fillSummaryDimensionsToList(flattendList, rowSummaryDimension, rowSummaryDimensionPathMap);
        rowFlattenedSummaryDimensions = flattendList
                .toArray(new SummaryDimension[flattendList.size()]);

        flattendList.clear();
        fillSummaryDimensionsToList(flattendList, colSummaryDimension, colSummaryDimensionPathMap);
        colFlattenedSummaryDimensions = flattendList
                .toArray(new SummaryDimension[flattendList.size()]);

        rows = rowFlattenedSummaryDimensions.length;
        cols = colFlattenedSummaryDimensions.length;

        summaryCells = new SummaryCell[rows][cols];
        initSummaryCells();
    }

    private void initSummaryCells() {
        final SummaryCell rootCell = new SummaryCell();
        summaryCells[0][0] = rootCell;

        for (int i = 0; i < rows; i++) {
            final SummaryCell cell = new SummaryCell();

            final List<Integer> indices = new LinkedList<>();
            for (int index = 1; index < cols; index++) {
                SummaryDimension dimension = colFlattenedSummaryDimensions[index];
                if (dimension.getDepth() == 1) {
                    indices.add(index);
                }
                cell.setColChildCellIndices(indices);
                cell.setColChildrenRowIndex(i);
            }

            summaryCells[i][0] = cell;
        }

        for (int j = 1; j < cols; j++) {
            final SummaryCell cell = new SummaryCell();

            final List<Integer> indices = new LinkedList<>();
            for (int index = 1; index < rows; index++) {
                SummaryDimension dimension = rowFlattenedSummaryDimensions[index];
                if (dimension.getDepth() == 1) {
                    indices.add(index);
                }
                cell.setRowChildCellIndices(indices);
                cell.setRowChildrenColIndex(j);
            }

            summaryCells[0][j] = cell;
        }

        for (int i = 1; i < rows; i++) {
            final SummaryDimension rowDimension = rowFlattenedSummaryDimensions[i];

            for (int j = 1; j < cols; j++) {
                final SummaryCell cell = new SummaryCell();
                summaryCells[i][j] = cell;

                final SummaryDimension colDimension = colFlattenedSummaryDimensions[j];

                if (colDimension.hasChild()) {
                    final List<Integer> indices = new LinkedList<>();
                    final int childDepth = colDimension.getDepth() + 1;
                    for (int index = j + 1; index < cols; index++) {
                        SummaryDimension childDimension = colFlattenedSummaryDimensions[index];
                        if (childDimension.getDepth() != childDepth) {
                            break;
                        }
                        indices.add(index);
                    }
                    cell.setColChildCellIndices(indices);
                    cell.setColChildrenRowIndex(i);
                }

                if (rowDimension.hasChild()) {
                    final List<Integer> indices = new LinkedList<>();
                    final int childDepth = rowDimension.getDepth() + 1;
                    for (int index = i + 1; index < rows; index++) {
                        SummaryDimension childDimension = rowFlattenedSummaryDimensions[index];
                        if (childDimension.getDepth() != childDepth) {
                            break;
                        }
                        indices.add(index);
                    }
                    cell.setRowChildCellIndices(indices);
                    cell.setRowChildrenColIndex(j);
                }
            }
        }
    }

    public SummaryDimension getRowSummaryDimensions() {
        return rowSummaryDimension;
    }

    public SummaryDimension getColSummaryDimensions() {
        return colSummaryDimension;
    }

    public SummaryDimension[] getRowFlattendSummaryDimensions() {
        return rowFlattenedSummaryDimensions;
    }

    public SummaryDimension[] getColFlattendSummaryDimensions() {
        return colFlattenedSummaryDimensions;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public SummaryCell[][] getSummaryCells() {
        return summaryCells;
    }

    public int getRowIndexByDimensionPath(final String path) {
        final Pair<Integer, SummaryDimension> pair = rowSummaryDimensionPathMap.get(path);
        return pair != null ? pair.getLeft() : -1;
    }

    public int getColIndexByDimensionPath(final String path) {
        final Pair<Integer, SummaryDimension> pair = colSummaryDimensionPathMap.get(path);
        return pair != null ? pair.getLeft() : -1;
    }

    public SummaryCell[] getColumnSummaryCells(final int colIndex, final int rowBeginIndex, final int maxLength) {
        final int rowEndIndex = Math.min(rowBeginIndex + maxLength, rows);
        final SummaryCell[] cells = new SummaryCell[rowEndIndex - rowBeginIndex];

        for (int i = rowBeginIndex; i < rowEndIndex; i++) {
            cells[i - rowBeginIndex] = summaryCells[i][colIndex];
        }

        return cells;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SummaryMatrix)) {
            return false;
        }

        final SummaryMatrix that = (SummaryMatrix) o;

        if (rows != that.rows || cols != that.cols) {
            return false;
        }

        if (!Objects.equals(rowSummaryDimension, that.rowSummaryDimension)) {
            return false;
        }

        if (!Objects.equals(colSummaryDimension, that.colSummaryDimension)) {
            return false;
        }

        if (!Objects.equals(summaryCells, that.summaryCells)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(rows).append(cols).append(rowSummaryDimension)
                .append(colSummaryDimension).append(summaryCells).toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(1024);

        sb.append("\n[\n");
        for (int i = 0; i < rows; i++) {
            sb.append("  [ ");
            for (int j = 0; j < cols; j++) {
                final SummaryCell cell = summaryCells[i][j];
                if (cell == null) {
                    sb.append("null");
                }
                else if (!cell.hasSummaryValue()) {
                    sb.append("(null)");
                }
                else {
                    sb.append(cell.getSummaryValues().get(0).getRepresentingValue());
                }
                sb.append(", ");
            }
            sb.append("]\n");
        }
        sb.append("]\n");

        return new ToStringBuilder(this).append("rowSummaryDimension", rowSummaryDimension)
                .append("colSummaryDimension", colSummaryDimension)
                .append("summaryCells", sb).toString();
    }

    private void fillSummaryDimensionsToList(final List<SummaryDimension> list,
            final SummaryDimension base,
            final Map<String, Pair<Integer, SummaryDimension>> summaryDimensionPathMap) {
        list.add(base);
        summaryDimensionPathMap.put(base.getPath(), Pair.of(list.size() - 1, base));

        if (base.hasChild()) {
            for (SummaryDimension child : base.getChildren()) {
                fillSummaryDimensionsToList(list, child, summaryDimensionPathMap);
            }
        }
    }
}
