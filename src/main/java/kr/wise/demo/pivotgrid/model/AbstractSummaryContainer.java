package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract public class AbstractSummaryContainer<T> implements SummaryContainer<T> {

    private List<BigDecimal> summary;
    private int rowCount;

    private List<DataGroup> childDataGroups;
    private List<DataGroup> unmodifiableChildDataGroups;
    private Map<String, DataGroup> childDataGroupsMap;

    @Override
    public T addSummaryValue(final int value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public T addSummaryValue(final long value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public T addSummaryValue(final double value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public T addSummaryValue(final String value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addSummaryValue(final BigDecimal value) {
        if (summary == null) {
            summary = new ArrayList<>();
        }
        summary.add(value);
        return (T) this;
    }

    @Override
    public List<BigDecimal> getSummary() {
        return summary;
    }

    @JsonIgnore
    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int incrementRowCount() {
        return ++rowCount;
    }

    public DataGroup addChildDataGroup(final String key) {
        final DataGroup group = new DataGroup(key);

        if (childDataGroups == null) {
            childDataGroups = new LinkedList<>();
            unmodifiableChildDataGroups = Collections.unmodifiableList(childDataGroups);
            childDataGroupsMap = new HashMap<>();
        }

        childDataGroups.add(group);
        childDataGroupsMap.put(key, group);

        return group;
    }

    public DataGroup getChildDataGroup(final String key) {
        return childDataGroupsMap != null ? childDataGroupsMap.get(key) : null;
    }

    protected List<DataGroup> getChildDataGroups() {
        return unmodifiableChildDataGroups != null ? unmodifiableChildDataGroups : Collections.emptyList();
    }
}
