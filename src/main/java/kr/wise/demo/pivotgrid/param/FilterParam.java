package kr.wise.demo.pivotgrid.param;

public class FilterParam {

    private final String selector;
    private final String operator;
    private final String comparingValue;

    public FilterParam(final String selector, final String operator, String comparingValue) {
        this.selector = selector;
        this.operator = operator;
        this.comparingValue = comparingValue;
    }

    public String getSelector() {
        return selector;
    }

    public String getOperator() {
        return operator;
    }

    public String getComparingValue() {
        return comparingValue;
    }
}
