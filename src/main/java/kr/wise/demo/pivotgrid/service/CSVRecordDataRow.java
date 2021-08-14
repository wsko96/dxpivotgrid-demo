package kr.wise.demo.pivotgrid.service;

import org.apache.commons.csv.CSVRecord;

import kr.wise.demo.pivotgrid.model.AbstractDataRow;

public class CSVRecordDataRow extends AbstractDataRow {

    private final CSVRecord record;

    public CSVRecordDataRow(final CSVRecord record) {
        this.record = record;
    }

    public String getStringValue(final String columnName) {
        return record.get(columnName);
    }
}
