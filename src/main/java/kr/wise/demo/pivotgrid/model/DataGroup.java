package kr.wise.demo.pivotgrid.model;

import kr.wise.demo.pivotgrid.aggregator.DataAggregator;

/**
 * {@link DataAggregator}에 포함되는 데이터 그룹과 하위 아이템 데이터 그룹.
 */
public class DataGroup extends AbstractSummaryContainer<DataGroup> {

    public DataGroup() {
        this(null);
    }

    public DataGroup(final String key) {
        super(key);
    }

}
