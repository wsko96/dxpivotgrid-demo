package kr.wise.demo.pivotgrid.param;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PagingParam {

    private int index;
    private int size;
    private List<GroupParam> rowGroupParams;

    public PagingParam() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void addRowGroupParam(final GroupParam rowGroupParam) {
        if (rowGroupParams == null) {
            rowGroupParams = new LinkedList<>();
        }

        rowGroupParams.add(rowGroupParam);
    }

    public List<GroupParam> getRowGroupParams() {
        return rowGroupParams != null ? Collections.unmodifiableList(rowGroupParams)
                : Collections.emptyList();
    }
}
