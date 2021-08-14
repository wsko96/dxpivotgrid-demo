package kr.wise.demo.pivotgrid.param;

public class SummaryParam {

    private String selector;
    private String summaryType;

    public SummaryParam() {

    }

    public SummaryParam(final String selector, final String summaryType) {
        this.selector = selector;
        this.summaryType = summaryType;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(String summaryType) {
        this.summaryType = summaryType;
    }

}
