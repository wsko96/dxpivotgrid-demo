package kr.wise.demo.pivotgrid.model;

import java.util.Iterator;

/**
 * 원본 데이터셋 접근을 위한 인터페이스.
 */
public interface DataFrame {

    /**
     * 데이터프레임의 컬럼 이름들을 배열로 반환.
     * @return 데이터프레임의 컬럼 이름들
     */
    public String[] getColumnNames();

    /**
     * 첫 행부터 차례로 읽어나가기 위한 <code>Iterator</code>를 반환.
     * <P>
     * 참고로, {@link DataAggregator}를 생성하여, 호출자에게 총 결과 써머리 집합으로 서비스한다.
     * @return 첫 행부터 차례로 읽어나가기 위한 <code>Iterator</code>
     */
    public Iterator<DataRow> iterator();

}
