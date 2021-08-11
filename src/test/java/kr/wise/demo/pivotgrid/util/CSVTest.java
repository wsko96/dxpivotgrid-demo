package kr.wise.demo.pivotgrid.util;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CSVTest {

    private static Logger log = LoggerFactory.getLogger(CSVTest.class);

    private static final int EXPECTED_DATA_SIZE = 1000000;

    @Test
    public void testGenerateTestCsvFromJson() throws Exception {
        final Resource salesJsonFile = new ClassPathResource(
                "kr/wise/demo/pivotgrid/repository/sales.json");
        final String[] headers = { "id", "region", "country", "city", "amount", "date" };

        File file = new File("target/sales.csv");

        InputStream is = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        CSVPrinter csvPrinter = null;

        try {
            is = salesJsonFile.getInputStream();
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(file);
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

    private void printDateNodeToCSVRecord(final CSVPrinter csvPrinter, final ObjectNode rowNode, final int newIdDelta) throws IOException {
        final int id = NumberUtils.toInt(rowNode.get("id").asText());
        final String region = rowNode.get("region").asText();
        final String country = rowNode.get("country").asText();
        final String city = rowNode.get("city").asText();
        final BigDecimal amount = new BigDecimal(rowNode.get("amount").asText());
        final String date = rowNode.get("date").asText();
        csvPrinter.printRecord(id + newIdDelta, region, country, city, amount, date);
    }
}
