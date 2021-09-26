package kr.wise.demo.pivotgrid.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Paging {

    private int offset;
    private int limit;
    private int count;
    private int total;
    private int distinctTotal;

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getDistinctTotal() {
        return distinctTotal;
    }

    public void setDistinctTotal(int distinctTotal) {
        this.distinctTotal = distinctTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Paging)) {
            return false;
        }

        final Paging that = (Paging) o;

        return offset == that.offset && limit == that.limit && count == that.count
                && total == that.total && distinctTotal == that.distinctTotal;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(offset).append(limit).append(count).append(total)
                .append(distinctTotal).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("offset", offset).append("limit", limit)
                .append("count", count).append("total", total)
                .append("distinctTotal", distinctTotal).toString();
    }
}
