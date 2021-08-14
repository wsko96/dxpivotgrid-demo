package kr.wise.demo.pivotgrid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;

public class ParamUtilsTest {

    private static Logger log = LoggerFactory.getLogger(ParamUtilsTest.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSimpleFilterParamParsing() throws Exception {
        ArrayNode filterNode = (ArrayNode) objectMapper
                .readTree("[[\"date.Year\",\"=\",\"2013\"]]");
        FilterParam rootFilter = ParamUtils.toFilterParam(filterNode);
        assertNotNull(rootFilter);
        assertNull(rootFilter.getOperator());
        assertNull(rootFilter.getSelector());
        assertNull(rootFilter.getComparingValue());
        assertTrue(rootFilter.hasChild());
        assertEquals(1, rootFilter.getChildCount());

        FilterParam firstChild = rootFilter.getFirstChild();
        assertNotNull(firstChild);
        assertEquals("=", firstChild.getOperator());
        assertEquals("date.Year", firstChild.getSelector());
        assertEquals("2013", firstChild.getComparingValue());
    }

    @Test
    public void testCompositeFilterParamParsing() throws Exception {
        ArrayNode filterNode = (ArrayNode) objectMapper.readTree(
                "[[[\"date.Year\",\"=\",\"2013\"]],\"and\",[[\"region\",\"=\",\"Africa\"]]]");
        FilterParam rootFilter = ParamUtils.toFilterParam(filterNode);
        assertNotNull(rootFilter);
        assertEquals("and", rootFilter.getOperator());
        assertNull(rootFilter.getSelector());
        assertNull(rootFilter.getComparingValue());
        assertTrue(rootFilter.hasChild());
        assertEquals(2, rootFilter.getChildCount());

        FilterParam childFilter1 = rootFilter.getChildren().get(0);
        FilterParam childFilter2 = rootFilter.getChildren().get(1);
        assertNotNull(childFilter1);
        assertNotNull(childFilter2);

        assertEquals("=", childFilter1.getOperator());
        assertEquals("date.Year", childFilter1.getSelector());
        assertEquals("2013", childFilter1.getComparingValue());

        assertEquals("=", childFilter2.getOperator());
        assertEquals("region", childFilter2.getSelector());
        assertEquals("Africa", childFilter2.getComparingValue());
    }

    @Test
    public void testMoreComplexFilterParamParsing() throws Exception {
        ArrayNode filterNode = (ArrayNode) objectMapper.readTree(
                "[[[\"date.Year\",\"=\",\"2013\"]],\"and\",[[\"region\",\"=\",\"Africa\"],\"or\",[\"region\",\"=\",\"Australia\"]]]");
        FilterParam rootFilter = ParamUtils.toFilterParam(filterNode);
        assertNotNull(rootFilter);
        assertEquals("and", rootFilter.getOperator());
        assertNull(rootFilter.getSelector());
        assertNull(rootFilter.getComparingValue());
        assertTrue(rootFilter.hasChild());
        assertEquals(2, rootFilter.getChildCount());

        FilterParam childFilter1 = rootFilter.getChildren().get(0);
        FilterParam childFilter2 = rootFilter.getChildren().get(1);
        assertNotNull(childFilter1);
        assertNotNull(childFilter2);

        assertEquals("=", childFilter1.getOperator());
        assertEquals("date.Year", childFilter1.getSelector());
        assertEquals("2013", childFilter1.getComparingValue());

        assertEquals("or", childFilter2.getOperator());
        assertNull(childFilter2.getSelector());
        assertNull(childFilter2.getComparingValue());
        assertTrue(childFilter2.hasChild());
        assertEquals(2, childFilter2.getChildCount());

        FilterParam grandchildFilter21 = childFilter2.getChildren().get(0);
        FilterParam grandchildFilter22 = childFilter2.getChildren().get(1);
        assertNotNull(grandchildFilter21);
        assertNotNull(grandchildFilter21);

        assertEquals("=", grandchildFilter21.getOperator());
        assertEquals("region", grandchildFilter21.getSelector());
        assertEquals("Africa", grandchildFilter21.getComparingValue());

        assertEquals("=", grandchildFilter22.getOperator());
        assertEquals("region", grandchildFilter22.getSelector());
        assertEquals("Australia", grandchildFilter22.getComparingValue());
    }

    @Test
    public void testSingleGroupParamParsing() throws Exception {
        ArrayNode groupParamsNode = (ArrayNode) objectMapper.readTree(
                "[{\"selector\":\"date\",\"groupInterval\":\"year\",\"isExpanded\":false}]");
        GroupParam[] groupParams = ParamUtils.toGroupParams(objectMapper, groupParamsNode);
        assertEquals(1, groupParams.length);

        GroupParam groupParam = groupParams[0];
        assertEquals("date", groupParam.getSelector());
        assertEquals("year", groupParam.getGroupInterval());
        assertFalse(groupParam.getIsExpanded());
    }

    @Test
    public void testDoubleGroupParamParsing() throws Exception {
        ArrayNode groupParamsNode = (ArrayNode) objectMapper.readTree(
                "[{\"selector\":\"region\",\"isExpanded\":false},{\"selector\":\"date\",\"groupInterval\":\"year\",\"isExpanded\":false}]");
        GroupParam[] groupParams = ParamUtils.toGroupParams(objectMapper, groupParamsNode);
        assertEquals(2, groupParams.length);

        GroupParam groupParam = groupParams[0];
        assertEquals("region", groupParam.getSelector());
        assertNull(groupParam.getGroupInterval());
        assertFalse(groupParam.getIsExpanded());

        groupParam = groupParams[1];
        assertEquals("date", groupParam.getSelector());
        assertEquals("year", groupParam.getGroupInterval());
        assertFalse(groupParam.getIsExpanded());
    }

    @Test
    public void testToPagingParams() throws Exception {
        ObjectNode pagingParamNode = (ObjectNode) objectMapper.readTree(
                "{ \"index\": 1, \"size\": 5, \"rowGroups\": [ { \"selector\": \"region\" }, { \"selector\": \"city\" } ] }");
        PagingParam pagingParam = ParamUtils.toPagingParam(objectMapper, pagingParamNode);
        assertEquals(1, pagingParam.getIndex());
        assertEquals(5, pagingParam.getSize());

        List<GroupParam> rowGroupParams = pagingParam.getRowGroupParams();
        assertEquals(2, rowGroupParams.size());

        GroupParam rowGroupParam = rowGroupParams.get(0);
        assertEquals("region", rowGroupParam.getSelector());

        rowGroupParam = rowGroupParams.get(1);
        assertEquals("city", rowGroupParam.getSelector());
    }
}
