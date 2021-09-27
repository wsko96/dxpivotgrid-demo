package kr.wise.demo.pivotmatrix.impl;

import java.util.ArrayList;
import java.util.Collections;
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

import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;
import kr.wise.demo.pivotmatrix.SummaryCell;
import kr.wise.demo.pivotmatrix.SummaryDimension;
import kr.wise.demo.pivotmatrix.SummaryMatrix;

public class DefaultSummaryMatrixImpl implements SummaryMatrix {

    private static Logger log = LoggerFactory.getLogger(DefaultSummaryMatrixImpl.class);

    private List<GroupParam> rowGroupParams;
    private List<GroupParam> colGroupParams;
    private List<SummaryParam> summaryParams;

    private SummaryDimension rowSummaryDimension;
    private SummaryDimension colSummaryDimension;
    private SummaryDimension[] rowFlattenedSummaryDimensions;
    private SummaryDimension[] colFlattenedSummaryDimensions;
    private Map<String, Pair<Integer, SummaryDimension>> rowSummaryDimensionPathMap = new HashMap<>();
    private Map<String, Pair<Integer, SummaryDimension>> colSummaryDimensionPathMap = new HashMap<>();

    private int rows;
    private int cols;
    SummaryCell[][] summaryCells;

    private DefaultSummaryMatrixImpl() {
    }

    public DefaultSummaryMatrixImpl(final List<GroupParam> rowGroupParams,
            final List<GroupParam> colGroupParams, final List<SummaryParam> summaryParams,
            final SummaryDimension rowSummaryDimension,
            final SummaryDimension colSummaryDimension) {
        this.rowGroupParams = new ArrayList<>();
        if (rowGroupParams != null) {
            this.rowGroupParams.addAll(rowGroupParams);
        }
        this.colGroupParams = new ArrayList<>();
        if (colGroupParams != null) {
            this.colGroupParams.addAll(colGroupParams);
        }
        this.summaryParams = new ArrayList<>();
        if (summaryParams != null) {
            this.summaryParams.addAll(summaryParams);
        }

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

    @Override
    public List<GroupParam> getRowGroupParams() {
        return rowGroupParams != null ? Collections.unmodifiableList(rowGroupParams)
                : Collections.emptyList();
    }

    @Override
    public List<GroupParam> getColGroupParams() {
        return colGroupParams != null ? Collections.unmodifiableList(colGroupParams)
                : Collections.emptyList();
    }

    @Override
    public List<SummaryParam> getSummaryParams() {
        return summaryParams != null ? Collections.unmodifiableList(summaryParams)
                : Collections.emptyList();
    }

    public SummaryDimension getRowSummaryDimension() {
        return rowSummaryDimension;
    }

    public SummaryDimension getColSummaryDimension() {
        return colSummaryDimension;
    }

    @Override
    public SummaryDimension[] getRowFlattendSummaryDimensions() {
        return rowFlattenedSummaryDimensions;
    }

    @Override
    public SummaryDimension[] getColFlattendSummaryDimensions() {
        return colFlattenedSummaryDimensions;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getCols() {
        return cols;
    }

    @Override
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

    public SummaryCell[] getColumnSummaryCells(final int colIndex, final int rowBeginIndex,
            final int maxLength) {
        final int rowEndIndex = Math.min(rowBeginIndex + maxLength, rows);
        final SummaryCell[] cells = new SummaryCell[rowEndIndex - rowBeginIndex];

        for (int i = rowBeginIndex; i < rowEndIndex; i++) {
            cells[i - rowBeginIndex] = summaryCells[i][colIndex];
        }

        return cells;
    }

    public SummaryMatrix sliceRows(final List<Integer> rowIndices) {
        final DefaultSummaryMatrixImpl sliced = new DefaultSummaryMatrixImpl();

        sliced.rowGroupParams = new ArrayList<>();
        if (rowGroupParams != null) {
            sliced.rowGroupParams.addAll(rowGroupParams);
        }

        sliced.colGroupParams = new ArrayList<>();
        if (colGroupParams != null) {
            sliced.colGroupParams.addAll(colGroupParams);
        }

        sliced.summaryParams = new ArrayList<>();
        if (summaryParams != null) {
            sliced.summaryParams.addAll(summaryParams);
        }

        sliced.rowSummaryDimension = rowSummaryDimension;
        sliced.colSummaryDimension = colSummaryDimension;

        sliced.rows = rowIndices.size();
        sliced.cols = cols;

        sliced.summaryCells = new SummaryCell[rowIndices.size()][cols];

        sliced.rowFlattenedSummaryDimensions = new SummaryDimension[rowIndices.size()];
        int i = 0;
        for (int rowIndex : rowIndices) {
            sliced.rowFlattenedSummaryDimensions[i] = rowFlattenedSummaryDimensions[rowIndex];
            System.arraycopy(summaryCells[rowIndex], 0, sliced.summaryCells[i], 0, cols);
            ++i;
        }

        sliced.colFlattenedSummaryDimensions = new SummaryDimension[cols];
        if (cols > 0) {
            System.arraycopy(colFlattenedSummaryDimensions, 0, sliced.colFlattenedSummaryDimensions,
                    0, cols);
        }

        sliced.rowSummaryDimensionPathMap = rowSummaryDimensionPathMap;
        sliced.colSummaryDimensionPathMap = colSummaryDimensionPathMap;

        return sliced;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultSummaryMatrixImpl)) {
            return false;
        }

        final DefaultSummaryMatrixImpl that = (DefaultSummaryMatrixImpl) o;

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
                .append("colSummaryDimension", colSummaryDimension).append("summaryCells", sb)
                .toString();
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
