package kr.wise.demo.pivotgrid.service;

import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;

import kr.wise.demo.pivotgrid.model.DataFrame;
import kr.wise.demo.pivotgrid.model.DataRow;
import kr.wise.demo.pivotgrid.repository.CSVDataSet;

public class CSVDataSetDataFrame implements DataFrame {

    private final CSVDataSet csvDataSet;
    private final String[] columnNames;

    public CSVDataSetDataFrame(final CSVDataSet csvDataSet) {
        this.csvDataSet = csvDataSet;
        this.columnNames = csvDataSet.getHeaders().toArray(new String[0]);
    }

    public String[] getColumnNames() {
        return columnNames.clone();
    }

    public Iterator<DataRow> iterator() {
        return new DataRowIterator(csvDataSet.getRecords().iterator());
    }

    private class DataRowIterator implements Iterator<DataRow> {

        private final Iterator<CSVRecord> recordIt;

        private DataRowIterator(final Iterator<CSVRecord> recordIt) {
            this.recordIt = recordIt;
        }

        @Override
        public boolean hasNext() {
            return recordIt.hasNext();
        }

        @Override
        public DataRow next() {
            return new CSVRecordDataRow(recordIt.next());
        }
    }
}
