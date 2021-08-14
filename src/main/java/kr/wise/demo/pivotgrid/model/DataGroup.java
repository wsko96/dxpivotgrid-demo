package kr.wise.demo.pivotgrid.model;

import java.util.List;

import kr.wise.demo.pivotgrid.aggregator.DataAggregator;
import kr.wise.demo.pivotgrid.param.GroupParam;

/**
 * {@link DataAggregator}에 포함되는 데이터 그룹과 하위 아이템 데이터 그룹.
 */
public class DataGroup extends AbstractSummaryContainer<DataGroup> {

    private final GroupParam groupParam;
    private final String key;

    public DataGroup() {
        this(null, null);
    }

    public DataGroup(final GroupParam groupParam, final String key) {
        super();
        this.groupParam = groupParam;
        this.key = key;
    }

    public GroupParam getGroupParam() {
        return groupParam;
    }

    public String getKey() {
        return key;
    }

    public List<DataGroup> getItems() {
        return super.getChildDataGroups();
    }

    public DataGroup cloneWithoutChildDataGroups() {
        final DataGroup clone = new DataGroup(groupParam, key);
        clone.setSummary(getSummary());
        clone.setRowCount(getRowCount());
        return clone;
    }
}
