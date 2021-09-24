package kr.wise.demo.pivotmatrix.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SummaryCell {

    private final List<SummaryValue> summaryValues = new ArrayList<>();

    private final List<SummaryValue> unmodifiableSummaryValues = Collections
            .unmodifiableList(summaryValues);

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
}
