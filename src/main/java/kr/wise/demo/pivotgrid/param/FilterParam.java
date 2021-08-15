package kr.wise.demo.pivotgrid.param;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class FilterParam {

    private final String operator;
    private String selector;
    private String comparingValue;

    private List<FilterParam> children;

    public FilterParam() {
        this(null);
    }

    public FilterParam(final String operator) {
        this(operator, null, null);
    }

    public FilterParam(final String operator, final String selector, String comparingValue) {
        this.operator = operator;
        this.selector = selector;
        this.comparingValue = comparingValue;
    }

    public String getOperator() {
        return operator;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getComparingValue() {
        return comparingValue;
    }

    public void setComparingValue(String comparingValue) {
        this.comparingValue = comparingValue;
    }

    public FilterParam addChild(final String operator, final String selector,
            final String comparingValue) {
        final FilterParam child = new FilterParam(operator, selector, comparingValue);
        if (children == null) {
            children = new LinkedList<>();
        }
        children.add(child);
        return child;
    }

    public List<FilterParam> getChildren() {
        return (children != null) ? Collections.unmodifiableList(children)
                : Collections.emptyList();
    }

    public FilterParam getFirstChild() {
        return children != null && !children.isEmpty() ? children.get(0) : null;
    }

    public int getChildCount() {
        return children != null ? children.size() : 0;
    }

    public boolean hasChild() {
        return children != null && !children.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FilterParam)) {
            return false;
        }

        final FilterParam that = (FilterParam) o;

        if (!StringUtils.equals(operator, that.operator)) {
            return false;
        }

        if (!StringUtils.equals(selector, that.selector)) {
            return false;
        }

        if (!StringUtils.equals(comparingValue, that.comparingValue)) {
            return false;
        }

        final int childCount = children != null ? children.size() : 0;
        final int thatChildCount = that.children != null ? that.children.size() : 0;

        if (childCount != thatChildCount) {
            return false;
        }

        if (!Objects.equals(children, that.children)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(operator).append(selector).append(comparingValue)
                .append(children).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("operator", operator).append("selector", selector)
                .append("comparingValue", comparingValue).append("children", children).toString();
    }
}
