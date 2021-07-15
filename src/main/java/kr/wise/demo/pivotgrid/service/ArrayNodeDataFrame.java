package kr.wise.demo.pivotgrid.service;

import java.util.Iterator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataRow;

public class ArrayNodeDataFrame implements DataFrame {

    private final ArrayNode dataArray;
    private final String[] columnNames;

    public ArrayNodeDataFrame(final ArrayNode dataArray, final String[] columnNames) {
        this.dataArray = dataArray;
        final int columnCount = columnNames.length;
        this.columnNames = new String[columnCount];
        System.arraycopy(columnNames, 0, this.columnNames, 0, columnCount);
    }

    public String[] getColumnNames() {
        return columnNames.clone();
    }

    public Iterator<DataRow> iterator() {
        return new DataRowIterator();
    }

    private class DataRowIterator implements Iterator<DataRow> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < dataArray.size() - 1;
        }

        @Override
        public DataRow next() {
            return new ObjectNodeDataRow((ObjectNode) dataArray.get(index++));
        }
    }
}
