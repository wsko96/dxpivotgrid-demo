package kr.wise.demo.pivotgrid.model;

import kr.wise.demo.pivotgrid.aggregator.DataAggregator;

/**
 * {@link DataAggregator}에 포함되는 데이터 그룹과 하위 아이템 데이터 그룹.
 */
public class DataGroup extends AbstractSummaryContainer<DataGroup> {

    private final String key;

    public DataGroup() {
        this(null);
    }

    public DataGroup(final String key) {
        super();
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public DataGroup cloneWithoutChildDataGroups() {
        final DataGroup clone = new DataGroup(key);
        clone.setSummary(getSummary());
        clone.setRowCount(getRowCount());
        return clone;
    }
}
