package kr.wise.demo.pivotgrid.param;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PagingParam {

    private int offset;
    private int limit;
    private List<GroupParam> rowGroupParams;

    public PagingParam() {
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

    public int getRowGroupCount() {
        return rowGroupParams != null ? rowGroupParams.size() : 0;
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
