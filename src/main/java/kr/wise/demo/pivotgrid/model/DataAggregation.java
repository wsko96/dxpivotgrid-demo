package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 그룹별 총 써머리 데이터 집합.
 * <P>
 * 이는 최상위 컨테이너로서, 하위 그룹들을 포함한다. 이 클래스의 인스턴스는 결과 총 써머리 데이터 집합으로 사용된다.
 * <P>
 * 이 모델은 DevExpress Pivot Grid가 요구하는 JSON 모델과 동일한 방식이다.
 * <P>
 * 참고자료: <a href="https://js.devexpress.com/Documentation/18_2/Guide/Widgets/PivotGrid/Use_CustomStore/">https://js.devexpress.com/Documentation/18_2/Guide/Widgets/PivotGrid/Use_CustomStore/</a>
 */
public class DataAggregation implements SummaryContainer<DataAggregation> {

    private List<DataGroup> data;
    private List<DataGroup> unmodifiableData;
    private Map<String, DataGroup> dataMap;
    private List<BigDecimal> summary;
    private int rowCount;

    public DataAggregation() {
    }

    @Override
    public DataAggregation addSummaryValue(final int value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataAggregation addSummaryValue(final long value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataAggregation addSummaryValue(final double value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataAggregation addSummaryValue(final String value) {
        return addSummaryValue(new BigDecimal(value));
    }

    @Override
    public DataAggregation addSummaryValue(final BigDecimal value) {
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
