package kr.wise.demo.pivotgrid.repository;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVDataReader implements Closeable {

    private final CSVParser csvParser;
    private final List<String> headers;

    public CSVDataReader(final CSVParser csvParser, final boolean withHeader) {
        this.csvParser = csvParser;
        this.headers = withHeader ? csvParser.getHeaderNames() : null;
    }

    public List<String> getHeaders() {
        return headers != null ? Collections.unmodifiableList(headers) : Collections.emptyList();
    }

    public Iterator<CSVRecord> iterator() {
        return csvParser.iterator();
    }

    @Override
    public void close() throws IOException {
        csvParser.close();
    }
}
