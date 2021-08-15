package kr.wise.demo.pivotgrid.param;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SummaryParam)) {
            return false;
        }

        final SummaryParam that = (SummaryParam) o;

        if (!StringUtils.equals(selector, that.selector)) {
            return false;
        }

        if (!StringUtils.equals(summaryType, that.summaryType)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(selector).append(summaryType).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("selector", selector)
                .append("summaryType", summaryType).toString();
    }
}
