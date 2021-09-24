package kr.wise.demo.pivotmatrix.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class SummaryMatrix {

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
                sb.append(cell != null && cell.hasSummaryValue()
                        ? cell.getSummaryValues().get(0).getRepresentingValue() : null)
                        .append(", ");
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
