package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import kr.wise.demo.pivotmatrix.SummaryDimension;

abstract public class AbstractSummaryContainer<T> implements SummaryContainer<T> {

    private final String key;

    private List<BigDecimal> summary;
    private int rowCount;

    private AbstractSummaryContainer<?> parent;
    private int depth;

    private String childDataGroupKey;

    private List<DataGroup> childDataGroups;
    private List<DataGroup> unmodifiableChildDataGroups;
    private Map<String, DataGroup> childDataGroupsMap;

    private boolean visible;

    private Map<String, Object> attributes;

    private String path = "";

    public AbstractSummaryContainer() {
        this(null);
    }

    public AbstractSummaryContainer(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

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

    protected void setSummary(final List<BigDecimal> summary) {
        this.summary = summary;
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

    protected void setRowCount(final int rowCount) {
        this.rowCount = rowCount;
    }

    @JsonIgnore
    public AbstractSummaryContainer<?> getParent() {
        return parent;
    }

    @JsonIgnore
    public int getDepth() {
        return depth;
    }

    protected void setDepth(int depth) {
        this.depth = depth;
    }

    protected void setParent(AbstractSummaryContainer<?> parent) {
        this.parent = parent;
    }

    public String getChildDataGroupKey() {
        return childDataGroupKey;
    }

    public void setChildDataGroupKey(String childDataGroupKey) {
        this.childDataGroupKey = childDataGroupKey;
    }

    public void sortChildDataGroups(final Comparator<DataGroup> comparator) {
        if (childDataGroups != null) {
            Collections.sort(childDataGroups, comparator);
        }
    }

    public DataGroup addChildDataGroup(final String key) {
        final DataGroup group = new DataGroup(key);
        addChildDataGroup(group);
        return group;
    }

    public void addChildDataGroup(final DataGroup group) {
        if (childDataGroups == null) {
            childDataGroups = new LinkedList<>();
            unmodifiableChildDataGroups = Collections.unmodifiableList(childDataGroups);
            childDataGroupsMap = new HashMap<>();
        }

        childDataGroups.add(group);
        childDataGroupsMap.put(group.getKey(), group);
        group.setDepth(depth + 1);
        group.setParent(this);
        group.setPath(path + SummaryDimension.PATH_DELIMITER + group.getKey());
    }

    public DataGroup getChildDataGroup(final String key) {
        return childDataGroupsMap != null ? childDataGroupsMap.get(key) : null;
    }

    @JsonIgnore
    public List<DataGroup> getChildDataGroups() {
        return getChildDataGroups(false);
    }

    @JsonIgnore
    public List<DataGroup> getChildDataGroups(final boolean visibleOnly) {
       if (!visibleOnly || unmodifiableChildDataGroups == null) {
            return unmodifiableChildDataGroups;
        }

        return unmodifiableChildDataGroups.stream().filter((dataGroup) -> dataGroup.isVisible())
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Object getAttribute(final String attrName) {
        return attributes != null ? attributes.get(attrName) : null;
    }

    public void setAttribute(final String attrName, final Object attrValue) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(attrName, attrValue);
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
