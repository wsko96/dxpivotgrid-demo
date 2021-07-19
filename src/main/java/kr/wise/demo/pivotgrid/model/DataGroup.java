package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link DataAggregator}에 포함되는 데이터 그룹과 하위 아이템 데이터 그룹.
 */
public class DataGroup implements SummaryContainer<DataGroup> {

    private final String key;
    private List<BigDecimal> summary;
    private List<DataGroup> items;
    private List<DataGroup> unmodifiableItems;
    private Map<String, DataGroup> itemsMap;
    private int rowCount;

    public DataGroup() {
        this(null);
    }

    public DataGroup(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public DataGroup addSummaryValue(final int value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataGroup addSummaryValue(final long value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataGroup addSummaryValue(final double value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataGroup addSummaryValue(final String value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataGroup addSummaryValue(final BigDecimal value) {
        if (summary == null) {
            summary = new ArrayList<>();
        }
        summary.add(value);
        return this;
    }

    @Override
    public List<BigDecimal> getSummary() {
        return summary;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int incrementRowCount() {
        return ++rowCount;
    }

    public DataGroup addItem(final String key) {
        final DataGroup item = new DataGroup(key);

        if (items == null) {
            items = new LinkedList<>();
            unmodifiableItems = Collections.unmodifiableList(items);
            itemsMap = new HashMap<>();
        }

        items.add(item);
        itemsMap.put(key, item);
        return item;
    }

    public DataGroup getItem(final String key) {
        return itemsMap != null ? itemsMap.get(key) : null;
    }

    public List<DataGroup> getItems() {
        return unmodifiableItems;
    }
}
