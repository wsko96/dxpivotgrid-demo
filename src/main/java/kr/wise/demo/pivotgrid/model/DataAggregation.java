package kr.wise.demo.pivotgrid.model;

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

    private final Paging paging = new Paging();
    private boolean pagingApplied;

    public DataAggregation() {
        super();
    }

    public Paging getPaging() {
        return paging;
    }

    public DataAggregation cloneWithoutChildDataGroups() {
        final DataAggregation clone = new DataAggregation();
        clone.setSummary(getSummary());
        clone.setRowCount(getRowCount());
        clone.setChildDataGroupParam(getChildDataGroupParam());
        clone.setDepth(getDepth());
        return clone;
    }

    public boolean isPagingApplied() {
        return pagingApplied;
    }

    public void setPagingApplied(boolean pagingApplied) {
        this.pagingApplied = pagingApplied;
    }

}
