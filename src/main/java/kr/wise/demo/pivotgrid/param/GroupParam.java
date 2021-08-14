package kr.wise.demo.pivotgrid.param;

public class GroupParam {

    private String selector;
    private String groupInterval;
    private boolean isExpanded;

    public GroupParam() {
    }

    public GroupParam(final String selector, final String groupInterval, final boolean isExpanded) {
        this.selector = selector;
        this.groupInterval = groupInterval;
        this.isExpanded = isExpanded;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getGroupInterval() {
        return groupInterval;
    }

    public void setGroupInterval(String groupInterval) {
        this.groupInterval = groupInterval;
    }

    public boolean getIsExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

}
