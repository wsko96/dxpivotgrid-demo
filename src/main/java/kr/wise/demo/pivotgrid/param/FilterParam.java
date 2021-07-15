package kr.wise.demo.pivotgrid.param;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
}
