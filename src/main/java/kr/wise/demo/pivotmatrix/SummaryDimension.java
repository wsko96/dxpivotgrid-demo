package kr.wise.demo.pivotmatrix;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SummaryDimension {

    public static final String PATH_DELIMITER = "~|_";

    private String key;

    private List<SummaryDimension> children;
    private List<SummaryDimension> unmodifiableChildren;
    private Map<String, SummaryDimension> childMapByKey;

    private SummaryDimension parent;
    private int depth;

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

    public void setKey(String key) {
        this.key = key;
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
        child.depth = depth + 1;
        child.path = path + PATH_DELIMITER + child.getKey();

        return child;
    }

    public List<SummaryDimension> getChildren() {
        return unmodifiableChildren;
    }

    public void setChildren(List<SummaryDimension> children) {
        if (this.children == null) {
            this.children = new LinkedList<>();
            unmodifiableChildren = Collections.unmodifiableList(this.children);
            childMapByKey = new HashMap<>();
        }

        if (children != null) {
            for (SummaryDimension child : children) {
                this.children.add(child);
                childMapByKey.put(child.getKey(), child);
            }
        }
    }

    @JsonIgnore
    public SummaryDimension getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
