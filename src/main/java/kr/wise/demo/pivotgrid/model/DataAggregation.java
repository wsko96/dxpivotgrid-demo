package kr.wise.demo.pivotgrid.model;

import java.util.List;

/**
 * 그룹별 총 써머리 데이터 집합.
 * <P>
 * 이는 최상위 컨테이너로서, 하위 그룹들을 포함한다. 이 클래스의 인스턴스는 결과 총 써머리 데이터 집합으로 사용된다.
 * <P>
 * 이 모델은 DevExpress Pivot Grid가 요구하는 JSON 모델과 동일한 방식이다.
 * <P>
 * 참고자료: <a href=
 * "https://js.devexpress.com/Documentation/18_2/Guide/Widgets/PivotGrid/Use_CustomStore/">https://js.devexpress.com/Documentation/18_2/Guide/Widgets/PivotGrid/Use_CustomStore/</a>
 */
public class DataAggregation extends AbstractSummaryContainer<DataAggregation> {

    private int offset;
    private int limit;
    private int rowCount;
    private final Paging paging = new Paging();

    public DataAggregation() {
    }

    public List<DataGroup> getData() {
        return super.getChildDataGroups();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public Paging getPaging() {
        return paging;
    }

}
