package kr.wise.demo.pivotmatrix;

import java.util.List;

import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.SummaryParam;

public interface SummaryMatrix {

    public SummaryDimension[] getRowFlattendSummaryDimensions();

    public SummaryDimension[] getColFlattendSummaryDimensions();

    public int getRows();

    public int getCols();

    public SummaryCell[][] getSummaryCells();

    public SummaryDimension getRowSummaryDimension();

    public SummaryDimension getColSummaryDimension();

    public int getRowIndexByDimensionPath(final String path);

    public int getColIndexByDimensionPath(final String path);

    public SummaryMatrix sliceRows(final List<Integer> pageRowIndices);

    public List<GroupParam> getRowGroupParams();

    public List<GroupParam> getColGroupParams();

    public List<SummaryParam> getSummaryParams();

}
