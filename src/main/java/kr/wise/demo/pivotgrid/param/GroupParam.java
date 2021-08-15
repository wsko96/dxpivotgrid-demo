package kr.wise.demo.pivotgrid.param;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class GroupParam {

    private String selector;
    private String groupInterval;
    private boolean isExpanded;

    private String key;

    public GroupParam() {
    }

    public GroupParam(final String selector, final String groupInterval, final boolean isExpanded) {
        this.selector = selector;
        this.groupInterval = StringUtils.defaultIfBlank(groupInterval, null);
        this.isExpanded = isExpanded;

        this.key = StringUtils.joinWith(":", selector, groupInterval);
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
        this.key = StringUtils.joinWith(":", selector, groupInterval);
    }

    public String getGroupInterval() {
        return groupInterval;
    }

    public void setGroupInterval(String groupInterval) {
        this.groupInterval = groupInterval;
        this.key = StringUtils.joinWith(":", selector, groupInterval);
    }

    public boolean getIsExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GroupParam)) {
            return false;
        }

        final GroupParam that = (GroupParam) o;

        if (!StringUtils.equals(selector, that.selector)) {
            return false;
        }

        if (!StringUtils.equals(groupInterval, that.groupInterval)) {
            return false;
        }

        if (isExpanded != that.isExpanded) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(selector).append(groupInterval).append(isExpanded)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("selector", selector)
                .append("groupInterval", groupInterval).append("isExpanded", isExpanded).toString();
    }
}
