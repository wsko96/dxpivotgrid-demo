package kr.wise.demo.pivotgrid.repository;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.wise.demo.pivotgrid.model.SaleData;

@Service
public class SalesDataRepository {

    private static final int EXPECTED_MIN_DATA_SIZE = 1000000;

    private List<SaleData> saleDataList;

    private Resource salesJsonFile = new ClassPathResource("kr/wise/demo/pivotgrid/repository/sales.json");

    public SalesDataRepository() {
        final List<SaleData> list = new LinkedList<>();

        try (InputStream input = this.salesJsonFile.getInputStream()) {
            final ObjectMapper objectMapper = new ObjectMapper();
            final ArrayNode root = (ArrayNode) objectMapper.readTree(input);
            final int size = root.size();
            final List<ObjectNode> saleNodes = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                saleNodes.add((ObjectNode) root.get(i));
            }

            final int loopCount = 1 + EXPECTED_MIN_DATA_SIZE / size;

            for (int i = 0; i < loopCount; i++) {
                for (ObjectNode saleNode : saleNodes) {
                    final SaleData saleData = objectMapper.convertValue(saleNode, SaleData.class);
                    saleData.setId(saleData.getId() + 10000);
                    list.add(saleData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.saleDataList = Collections.unmodifiableList(list);
    }
    
    public List<SaleData> findAll() {
        return saleDataList;
    }
}
