package kr.wise.demo.pivotmatrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @JsonProperty(value = "vs")
    public List<SummaryValue> getSummaryValues() {
        return unmodifiableSummaryValues;
    }

    @JsonIgnore
    public List<Integer> getRowChildCellIndices() {
        return rowChildCellIndices;
    }

    public void setRowChildCellIndices(List<Integer> rowChildCellIndices) {
        this.rowChildCellIndices = rowChildCellIndices;
    }

    @JsonIgnore
    public List<Integer> getColChildCellIndices() {
        return colChildCellIndices;
    }

    public void setColChildCellIndices(List<Integer> colChildCellIndices) {
        this.colChildCellIndices = colChildCellIndices;
    }

    @JsonIgnore
    public int getRowChildrenColIndex() {
        return rowChildrenColIndex;
    }

    public void setRowChildrenColIndex(int rowChildrenColIndex) {
        this.rowChildrenColIndex = rowChildrenColIndex;
    }

    @JsonIgnore
    public int getColChildrenRowIndex() {
        return colChildrenRowIndex;
    }

    public void setColChildrenRowIndex(int colChildrenRowIndex) {
        this.colChildrenRowIndex = colChildrenRowIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SummaryCell)) {
            return false;
        }

        final SummaryCell that = (SummaryCell) o;

        if (!Objects.equals(summaryValues, that.summaryValues)) {
            return false;
        }

        if (!Objects.equals(rowChildCellIndices, that.rowChildCellIndices)) {
            return false;
        }

        if (rowChildrenColIndex != that.rowChildrenColIndex) {
            return false;
        }

        if (!Objects.equals(colChildCellIndices, that.colChildCellIndices)) {
            return false;
        }

        if (colChildrenRowIndex != that.colChildrenRowIndex) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(summaryValues).append(rowChildCellIndices)
                .append(rowChildrenColIndex).append(colChildCellIndices).append(colChildrenRowIndex)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("summaryValues", summaryValues).toString();
    }
}
