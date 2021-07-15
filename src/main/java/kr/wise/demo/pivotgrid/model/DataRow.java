package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.Date;

public interface DataRow {

    public String getString(final String columnName);

    public String getString(final String columnName, final String dateInterval);

    public Date getDate(final String columnName);

    public BigDecimal getBigDecimal(final String columnName);

}
