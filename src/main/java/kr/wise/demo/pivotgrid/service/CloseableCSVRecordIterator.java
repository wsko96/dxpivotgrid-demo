package kr.wise.demo.pivotgrid.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import kr.wise.demo.pivotgrid.repository.CSVDataReader;

@JsonSerialize(using = ClosingCSVRecordIteratorSerializer.class)
public class CloseableCSVRecordIterator implements Iterator<CSVRecord>, Closeable {

    private final CSVDataReader csvDataReader;
    private final String[] headers;
    private final Iterator<CSVRecord> recordIt;
    private final int endIndex;
    private int currentIndex;

    public CloseableCSVRecordIterator(final CSVDataReader csvDataReader, final int offset, final int limit) {
        this.csvDataReader = csvDataReader;
        this.headers = this.csvDataReader.getHeaders().toArray(new String[0]);
        this.recordIt = csvDataReader.iterator();
        this.endIndex = limit > 0 ? offset + limit : 0;

        for (int i = 0; i < offset; i++) {
            if (this.recordIt.hasNext()) {
                this.recordIt.next();
                ++currentIndex;
            }
        }
    }

    public String[] getHeaders() {
        return headers;
    }

    @Override
    public boolean hasNext() {
        if (endIndex > 0 && currentIndex >= endIndex) {
            return false;
        }

        return recordIt.hasNext();
    }

    @Override
    public CSVRecord next() {
        final CSVRecord record = recordIt.next();
        ++currentIndex;
        return record;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(csvDataReader);
    }
}

