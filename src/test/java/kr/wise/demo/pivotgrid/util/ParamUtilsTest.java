package kr.wise.demo.pivotgrid.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;

import kr.wise.demo.pivotgrid.param.FilterParam;

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
        ArrayNode filterNode = (ArrayNode) JacksonUtils.getObjectMapper()
                .readTree("[[[\"date.Year\",\"=\",\"2013\"]], \"and\", [[\"region\",\"=\",\"Africa\"]]]");
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
}
