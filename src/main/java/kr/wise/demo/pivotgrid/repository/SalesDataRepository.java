package kr.wise.demo.pivotgrid.repository;

import java.io.InputStream;

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

    private static final int EXPECTED_MIN_DATA_SIZE = 1000000;

    private ArrayNode dataArray;

    private Resource salesJsonFile = new ClassPathResource(
            "kr/wise/demo/pivotgrid/repository/sales.json");

    public SalesDataRepository() {
        this.dataArray = JacksonUtils.getObjectMapper().createArrayNode();

        try (InputStream input = this.salesJsonFile.getInputStream()) {
            final ArrayNode sourceDataArray = (ArrayNode) JacksonUtils.getObjectMapper()
                    .readTree(input);
            final int size = sourceDataArray.size();
            final int loopCount = 1 + EXPECTED_MIN_DATA_SIZE / size;
            int baseId = 10000;

            for (int i = 0; i < loopCount; i++) {
                for (int j = 0; j < size; j++) {
                    final ObjectNode dataNode = ((ObjectNode) sourceDataArray.get(j)).deepCopy();
                    final int id = NumberUtils.toInt(dataNode.get("id").asText());
                    dataNode.put("id", id + (baseId++));
                    this.dataArray.add(dataNode);
                }
            }

            log.debug("Test sales data (size: {}) loaded from {}.", this.dataArray.size(), salesJsonFile);
        }
        catch (Exception e) {
            log.error("Failed to load data array from the json.", e);
        }
    }

    public ArrayNode findAll() {
        return dataArray;
    }
}
