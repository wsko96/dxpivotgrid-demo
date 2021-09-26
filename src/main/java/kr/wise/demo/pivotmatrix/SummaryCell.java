package kr.wise.demo.pivotmatrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SummaryCell {

    private final List<SummaryValue> summaryValues = new ArrayList<>();

    private final List<SummaryValue> unmodifiableSummaryValues = Collections
            .unmodifiableList(summaryValues);

    private List<Integer> rowChildCellIndices;
    private int rowChildrenColIndex;

    private List<Integer> colChildCellIndices;
    private int colChildrenRowIndex;

    public boolean hasSummaryValue() {
        return !summaryValues.isEmpty();
    }

    public SummaryCell addSummaryValue(final SummaryValue summaryValue) {
        summaryValues.add(summaryValue);
        return this;
    }

    public SummaryCell addSummaryValues(final Collection<SummaryValue> summaryValues) {
        if (summaryValues != null && !summaryValues.isEmpty()) {
            this.summaryValues.addAll(summaryValues);
        }

        return this;
    }

    public List<SummaryValue> getSummaryValues() {
        return unmodifiableSummaryValues;
    }

    public List<Integer> getRowChildCellIndices() {
        return rowChildCellIndices;
    }

    public void setRowChildCellIndices(List<Integer> rowChildCellIndices) {
        this.rowChildCellIndices = rowChildCellIndices;
    }

    public List<Integer> getColChildCellIndices() {
        return colChildCellIndices;
    }

    public void setColChildCellIndices(List<Integer> colChildCellIndices) {
        this.colChildCellIndices = colChildCellIndices;
    }

    public int getRowChildrenColIndex() {
        return rowChildrenColIndex;
    }

    public void setRowChildrenColIndex(int rowChildrenColIndex) {
        this.rowChildrenColIndex = rowChildrenColIndex;
    }

    public int getColChildrenRowIndex() {
        return colChildrenRowIndex;
    }

    public void setColChildrenRowIndex(int colChildrenRowIndex) {
        this.colChildrenRowIndex = colChildrenRowIndex;
    }

}
