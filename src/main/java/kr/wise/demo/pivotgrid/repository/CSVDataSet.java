package kr.wise.demo.pivotgrid.repository;

import java.util.Collections;
import java.util.List;

import org.apache.commons.csv.CSVRecord;

public class CSVDataSet {

    private final List<String> headers;
    private final List<CSVRecord> records;

    public CSVDataSet(final List<String> headers, final List<CSVRecord> records) {
        this.headers = headers;
        this.records = records;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<CSVRecord> getRecords() {
        return (records != null) ? Collections.unmodifiableList(records) : Collections.emptyList();
    }

    public int getRecordCount() {
        return records != null ? records.size() : 0;
    }
}
