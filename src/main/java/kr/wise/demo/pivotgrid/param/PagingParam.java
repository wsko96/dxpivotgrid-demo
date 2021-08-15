package kr.wise.demo.pivotgrid.param;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PagingParam {

    private int offset;
    private int limit;
    private List<GroupParam> rowGroupParams;

    public PagingParam() {
    }

    public PagingParam(final int offset, final int limit, final List<GroupParam> rowGroupParams) {
        this.offset = offset;
        this.limit = limit;
        this.rowGroupParams = rowGroupParams;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PagingParam)) {
            return false;
        }

        final PagingParam that = (PagingParam) o;

        if (offset != that.offset) {
            return false;
        }

        if (limit != that.limit) {
            return false;
        }

        final int rowGroupParamCount = rowGroupParams != null ? rowGroupParams.size() : 0;
        final int thatRowGroupParamCount = that.rowGroupParams != null ? that.rowGroupParams.size() : 0;

        if (rowGroupParamCount != thatRowGroupParamCount) {
            return false;
        }

        if (!Objects.equals(rowGroupParams, that.rowGroupParams)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(offset).append(limit).append(rowGroupParams)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("offset", offset).append("limit", limit)
                .append("rowGroupParams", rowGroupParams).toString();
    }
}
