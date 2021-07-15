package kr.wise.demo.pivotgrid.model;

import java.util.Iterator;

public interface DataFrame {

    public String[] getColumnNames();

    public Iterator<DataRow> iterator();

}
