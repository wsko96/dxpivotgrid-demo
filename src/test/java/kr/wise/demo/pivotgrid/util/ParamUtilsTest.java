package kr.wise.demo.pivotgrid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.FilterParam;
import kr.wise.demo.pivotgrid.param.GroupParam;

public class ParamUtilsTest {

    private static Logger log = LoggerFactory.getLogger(ParamUtilsTest.class);

    @Test
    public void testSimpleFilterParamParsing() throws Exception {
        ArrayNode filterNode = (ArrayNode) JacksonUtils.getObjectMapper()
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
        ArrayNode filterNode = (ArrayNode) JacksonUtils.getObjectMapper().readTree(
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
    public void testSingleGroupParamParsing() throws Exception {
        ArrayNode groupParamsNode = (ArrayNode) JacksonUtils.getObjectMapper().readTree(
                "[{\"selector\":\"date\",\"groupInterval\":\"year\",\"isExpanded\":false}]");
        GroupParam[] groupParams = ParamUtils.toGroupParams(groupParamsNode);
        assertEquals(1, groupParams.length);

        GroupParam groupParam = groupParams[0];
        assertEquals("date", groupParam.getSelector());
        assertEquals("year", groupParam.getGroupInterval());
        assertFalse(groupParam.getIsExpanded());
    }

    @Test
    public void testDoubleGroupParamParsing() throws Exception {
        ArrayNode groupParamsNode = (ArrayNode) JacksonUtils.getObjectMapper().readTree(
                "[{\"selector\":\"region\",\"isExpanded\":false},{\"selector\":\"date\",\"groupInterval\":\"year\",\"isExpanded\":false}]");
        GroupParam[] groupParams = ParamUtils.toGroupParams(groupParamsNode);
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
}
