package kr.wise.demo.pivotmatrix.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SummaryDimension {

    public static final String PATH_DELIMITER = "~|_";

    private final String key;

    private List<SummaryDimension> children;
    private List<SummaryDimension> unmodifiableChildren;
    private Map<String, SummaryDimension> childMapByKey;

    private SummaryDimension parent;

    private String path = "";

    public SummaryDimension() {
        this.key = null;
    }

    public SummaryDimension(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public boolean hasChild() {
        return children != null && !children.isEmpty();
    }

    public SummaryDimension getChild(final String key) {
        if (childMapByKey != null) {
            return childMapByKey.get(key);
        }

        return null;
    }

    public SummaryDimension addChild(final SummaryDimension child) {
        if (children == null) {
            children = new LinkedList<>();
            unmodifiableChildren = Collections.unmodifiableList(children);
            childMapByKey = new HashMap<>();
        }

        final boolean added = children.add(child);

        if (!added) {
            throw new IllegalStateException("Child not added!");
        }

        childMapByKey.put(child.getKey(), child);

        child.parent = this;
        child.path = path + PATH_DELIMITER + child.getKey();

        return child;
    }

    public List<SummaryDimension> getChildren() {
        return unmodifiableChildren;
    }

    public SummaryDimension getParent() {
        return parent;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SummaryDimension)) {
            return false;
        }

        final SummaryDimension that = (SummaryDimension) o;

        if (!StringUtils.equals(key, that.key)) {
            return false;
        }

        if (!Objects.equals(children, that.children)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(key).append(children).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", key).append("children", children)
                .toString();
    }
}
