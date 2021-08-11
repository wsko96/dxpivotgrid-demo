package kr.wise.demo.pivotgrid.repository;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.wise.demo.pivotgrid.util.JacksonUtils;

@Service
public class SalesDataRepository {

    private static Logger log = LoggerFactory.getLogger(SalesDataRepository.class);

    private final int EXPECTED_DATA_SIZE = 1000000;
    private Resource originalSalesJsonFile = new ClassPathResource("kr/wise/demo/pivotgrid/repository/sales.json");
    private File salesCsvFile;

    @PostConstruct
    public void init() throws Exception {
        initializeTestCSVDataFile();
    }

    public CSVDataReader findAll() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        CSVParser csvParser = null;

        CSVDataReader csvDataReader = null;

        try {
            is = new FileInputStream(salesCsvFile);
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            csvParser = new CSVParser(br, CSVFormat.EXCEL.withHeader());
            csvDataReader = new CSVDataReader(csvParser, true);
        }
        catch (Exception e) {
            IOUtils.closeQuietly(csvParser, br, isr, is);
            log.error("Failed to load data array from the csv.", e);
        }

        return csvDataReader;
    }

    private void initializeTestCSVDataFile() throws Exception {
        final File tempDir = new File("target");
        if (!tempDir.isDirectory()) {
            tempDir.mkdirs();
        }
        salesCsvFile = new File(tempDir, "sales.csv");

        final String[] headers = { "id", "region", "country", "city", "amount", "date" };

        InputStream is = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        CSVPrinter csvPrinter = null;

        try {
            is = originalSalesJsonFile.getInputStream();
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(salesCsvFile);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL.builder().setHeader(headers).build());

            final ArrayNode sourceDataArray = (ArrayNode) JacksonUtils.getObjectMapper()
                    .readTree(bis);
            final int originalSize = sourceDataArray.size();

            for (int i = 0; i < originalSize; i++) {
                printDateNodeToCSVRecord(csvPrinter, (ObjectNode) sourceDataArray.get(i), 0);
            }

            int size = originalSize;
            int newIdDelta = 10000;
            while (size < EXPECTED_DATA_SIZE) {
                for (int i = 0; i < originalSize; i++) {
                    printDateNodeToCSVRecord(csvPrinter, (ObjectNode) sourceDataArray.get(i),
                            newIdDelta++);
                    if (++size >= EXPECTED_DATA_SIZE) {
                        break;
                    }
                }
            }
        }
        finally {
            IOUtils.closeQuietly(csvPrinter, bw, osw, fos, bis, is);
        }
    }

    private static void printDateNodeToCSVRecord(final CSVPrinter csvPrinter, final ObjectNode rowNode, final int newIdDelta) throws IOException {
        final int id = NumberUtils.toInt(rowNode.get("id").asText());
        final String region = rowNode.get("region").asText();
        final String country = rowNode.get("country").asText();
        final String city = rowNode.get("city").asText();
        final BigDecimal amount = new BigDecimal(rowNode.get("amount").asText());
        final String date = rowNode.get("date").asText();
        csvPrinter.printRecord(id + newIdDelta, region, country, city, amount, date);
    }
}
