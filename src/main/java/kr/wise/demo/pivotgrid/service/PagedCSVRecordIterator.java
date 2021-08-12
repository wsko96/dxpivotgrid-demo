package kr.wise.demo.pivotgrid.service;

import java.util.Iterator;

import org.apache.commons.csv.CSVRecord;

public class PagedCSVRecordIterator implements Iterator<CSVRecord> {

    private final Iterator<CSVRecord> recordIt;
    private final int endIndex;
    private int currentIndex;

    public PagedCSVRecordIterator(Iterator<CSVRecord> recordIt, final int offset, final int limit) {
        this.recordIt = recordIt;
        this.endIndex = limit > 0 ? offset + limit : 0;

        for (int i = 0; i < offset; i++) {
            if (this.recordIt.hasNext()) {
                this.recordIt.next();
                ++currentIndex;
            }
        }
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
}

