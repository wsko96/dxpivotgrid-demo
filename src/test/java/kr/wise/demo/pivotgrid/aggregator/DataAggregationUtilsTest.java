package kr.wise.demo.pivotgrid.aggregator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kr.wise.demo.pivotgrid.model.AbstractSummaryContainer;
import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.param.GroupParam;
import kr.wise.demo.pivotgrid.param.PagingParam;

public class DataAggregationUtilsTest {

    private DataAggregation dataAggregation;

    @BeforeEach
    public void setUp() throws Exception {
        dataAggregation = new DataAggregation();

        DataGroup group = dataAggregation.addChildDataGroup("North America");
        group.addChildDataGroup("New York").addSummaryValue(1).addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup("Los Angeles").addSummaryValue(4).addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup("Denver").addSummaryValue(7).addSummaryValue(8).addSummaryValue(9);

        group = dataAggregation.addChildDataGroup("South America");
        group.addChildDataGroup("Rio de Janeiro").addSummaryValue(1).addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup("Buenos Aires").addSummaryValue(4).addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup("Asuncion").addSummaryValue(7).addSummaryValue(8)
                .addSummaryValue(9);

        group = dataAggregation.addChildDataGroup("Europe");
        group.addChildDataGroup("London").addSummaryValue(1).addSummaryValue(2).addSummaryValue(3);
        group.addChildDataGroup("Berlin").addSummaryValue(4).addSummaryValue(5).addSummaryValue(6);
        group.addChildDataGroup("Madrid").addSummaryValue(7).addSummaryValue(8).addSummaryValue(9);
    }

    @Test
    public void testFillSummaryContainersToFlatList() throws Exception {
        List<AbstractSummaryContainer<?>> list = new LinkedList<>();
        DataAggregationUtils.fillSummaryContainersToFlatList(list, dataAggregation, 2, true);
        assertEquals(13, list.size());
        assertSame(dataAggregation, list.get(0));
        Object[] keys = list.stream().map((s) -> s.getKey()).toArray();
        assertArrayEquals(new Object[] { null, "North America", "New York", "Los Angeles", "Denver",
                "South America", "Rio de Janeiro", "Buenos Aires", "Asuncion", "Europe", "London",
                "Berlin", "Madrid" }, keys);

        list = new LinkedList<>();
        DataAggregationUtils.fillSummaryContainersToFlatList(list, dataAggregation, 1, true);
        assertEquals(4, list.size());
        assertSame(dataAggregation, list.get(0));
        keys = list.stream().map((s) -> s.getKey()).toArray();
        assertArrayEquals(new Object[] { null, "North America", "South America", "Europe" }, keys);
    }

    @Test
    public void testPaging() throws Exception {
        {
            List<DataGroup> groups = dataAggregation.getChildDataGroups();
            assertEquals(3, groups.size());

            Object[] groupKeys = groups.stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "North America", "South America", "Europe" },
                    groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("North America").getChildDataGroups()
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "New York", "Los Angeles", "Denver" }, groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("South America").getChildDataGroups()
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "Rio de Janeiro", "Buenos Aires", "Asuncion" },
                    groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("Europe").getChildDataGroups().stream()
                    .map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "London", "Berlin", "Madrid" }, groupKeys);
        }

        final PagingParam pagingParam = new PagingParam();
        pagingParam.setLimit(7);

        pagingParam.addRowGroupParam(new GroupParam("region", null, true));
        pagingParam.addRowGroupParam(new GroupParam("city", null, false));

        {
            pagingParam.setOffset(0);

            DataAggregationUtils.markPaginatedSummaryContainersVisible(dataAggregation, pagingParam);

            assertEquals(0, dataAggregation.getPaging().getOffset());
            assertEquals(7, dataAggregation.getPaging().getLimit());
            assertEquals(7, dataAggregation.getPaging().getCount());
            assertEquals(17, dataAggregation.getPaging().getTotal());

            List<DataGroup> groups = dataAggregation.getChildDataGroups(true);
            assertEquals(2, groups.size());

            Object[] groupKeys = groups.stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "North America", "South America" }, groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("North America").getChildDataGroups(true)
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "New York", "Los Angeles", "Denver" }, groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("South America").getChildDataGroups(true)
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "Rio de Janeiro" }, groupKeys);
        }

        {
            pagingParam.setOffset(7);

            DataAggregationUtils.resetContainersVisibility(dataAggregation, false);
            DataAggregationUtils.markPaginatedSummaryContainersVisible(dataAggregation, pagingParam);

            assertEquals(7, dataAggregation.getPaging().getOffset());
            assertEquals(7, dataAggregation.getPaging().getLimit());
            assertEquals(7, dataAggregation.getPaging().getCount());
            assertEquals(17, dataAggregation.getPaging().getTotal());

            List<DataGroup> groups = dataAggregation.getChildDataGroups(true);
            assertEquals(2, groups.size());

            Object[] groupKeys = groups.stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "South America", "Europe" }, groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("South America").getChildDataGroups(true)
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "Buenos Aires", "Asuncion" }, groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("Europe").getChildDataGroups(true)
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "London", "Berlin" }, groupKeys);
        }

        {
            pagingParam.setOffset(11);

            DataAggregationUtils.resetContainersVisibility(dataAggregation, false);
            DataAggregationUtils.markPaginatedSummaryContainersVisible(dataAggregation, pagingParam);

            assertEquals(11, dataAggregation.getPaging().getOffset());
            assertEquals(7, dataAggregation.getPaging().getLimit());
            assertEquals(6, dataAggregation.getPaging().getCount());
            assertEquals(17, dataAggregation.getPaging().getTotal());

            List<DataGroup> groups = dataAggregation.getChildDataGroups(true);
            assertEquals(1, groups.size());

            Object[] groupKeys = groups.stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "Europe" }, groupKeys);

            groupKeys = dataAggregation.getChildDataGroup("Europe").getChildDataGroups(true)
                    .stream().map((group) -> group.getKey()).toArray();
            assertArrayEquals(new Object[] { "London", "Berlin", "Madrid" }, groupKeys);
        }
    }
}
