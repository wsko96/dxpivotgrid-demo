package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataAggregation {

    private List<DataGroup> data;
    private List<DataGroup> unmodifiableData;
    private Map<String, DataGroup> dataMap;
    private List<BigDecimal> summary;

    public DataAggregation() {
    }

    public DataAggregation addSummaryValue(final int value) {
        return addSummaryValue(new BigDecimal(value));
    }

    public DataAggregation addSummaryValue(final long value) {
        return addSummaryValue(new BigDecimal(value));
    }

    public DataAggregation addSummaryValue(final double value) {
        return addSummaryValue(new BigDecimal(value));
    }

    public DataAggregation addSummaryValue(final String value) {
        return addSummaryValue(new BigDecimal(value));
    }

    public DataAggregation addSummaryValue(final BigDecimal value) {
        if (summary == null) {
            summary = new ArrayList<>();
        }
        summary.add(value);
        return this;
    }

    public List<BigDecimal> getSummary() {
        return summary;
    }

    public DataGroup addGroup(final String key) {
        final DataGroup group = new DataGroup(key);

        if (data == null) {
            data = new LinkedList<>();
            unmodifiableData = Collections.unmodifiableList(data);
            dataMap = new HashMap<>();
        }

        data.add(group);
        dataMap.put(key, group);

        return group;
    }

    public DataGroup getGroup(final String key) {
        return dataMap != null ? dataMap.get(key) : null;
    }

    public List<DataGroup> getData() {
        return unmodifiableData;
    }
}
