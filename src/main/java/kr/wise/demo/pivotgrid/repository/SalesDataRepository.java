package kr.wise.demo.pivotgrid.repository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SalesDataRepository {

    private static Logger log = LoggerFactory.getLogger(SalesDataRepository.class);

    private Resource salesCsvFile = new ClassPathResource("kr/wise/demo/pivotgrid/repository/sales.csv");

    public CSVDataSet findAll() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        CSVParser csvParser = null;

        try {
            is = salesCsvFile.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            csvParser = new CSVParser(br, CSVFormat.EXCEL.withHeader());

            final List<String> headers = csvParser.getHeaderNames();
            final List<CSVRecord> records = csvParser.getRecords();
            return new CSVDataSet(headers, records);
        }
        catch (Exception e) {
            log.error("Failed to load data array from the csv.", e);
        }
        finally {
            IOUtils.closeQuietly(csvParser, br, isr, is);
        }

        return null;
    }
}
