package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 써머리 데이터를 포함하는 객체의 추상화 인터페이스.
 * @param <T>
 */
public interface SummaryContainer<T> {

    public T addSummaryValue(final int value);

    public T addSummaryValue(final long value);

    public T addSummaryValue(final double value);

    public T addSummaryValue(final String value);

    public T addSummaryValue(final BigDecimal value);

    public List<BigDecimal> getSummary();

    public int getRowCount();

    public int incrementRowCount();

}
