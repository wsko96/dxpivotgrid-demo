package kr.wise.demo.pivotgrid.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import kr.wise.demo.pivotgrid.param.GroupParam;

public class DataAggregationTest {

    private DataAggregation dataAggregation;

    @BeforeEach
    public void setUp() throws Exception {
        dataAggregation = new DataAggregation();

        GroupParam regionGroupParam = new GroupParam("region", null, true);
        GroupParam cityGroupParam = new GroupParam("city", null, false);

        DataGroup group = dataAggregation.addChildDataGroup(regionGroupParam, "North America");
        group.addChildDataGroup(cityGroupParam, "New York")
                .addSummaryValue(1)
                .addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup(cityGroupParam, "Los Angeles")
                .addSummaryValue(4)
                .addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup(cityGroupParam, "Denver")
                .addSummaryValue(7)
                .addSummaryValue(8)
                .addSummaryValue(9);

        group = dataAggregation.addChildDataGroup(regionGroupParam, "South America");
        group.addChildDataGroup(cityGroupParam, "Rio de Janeiro")
                .addSummaryValue(1)
                .addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup(cityGroupParam, "Buenos Aires")
                .addSummaryValue(4)
                .addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup(cityGroupParam, "Asuncion")
                .addSummaryValue(7)
                .addSummaryValue(8)
                .addSummaryValue(9);

        group = dataAggregation.addChildDataGroup(regionGroupParam, "Europe");
        group.addChildDataGroup(cityGroupParam, "London")
                .addSummaryValue(1)
                .addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup(cityGroupParam, "Berlin")
                .addSummaryValue(4)
                .addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup(cityGroupParam, "Madrid")
                .addSummaryValue(7)
                .addSummaryValue(8)
                .addSummaryValue(9);
    }

    @Test
    public void testGroupSorting() throws Exception {
        List<DataGroup> groups = dataAggregation.getChildDataGroups();
        assertEquals(3, groups.size());

        Object[] groupKeys = groups.stream().map((group) -> group.getKey()).toArray();
        assertArrayEquals(new Object[] { "North America", "South America", "Europe" }, groupKeys);

        // alphabetical sorting on group keys...
        Comparator<DataGroup> groupComparator = new Comparator<DataGroup>() {
            @Override
            public int compare(DataGroup group1, DataGroup group2) {
                return group1.getKey().compareTo(group2.getKey());
            }
        };
        dataAggregation.sortChildDataGroups(groupComparator);

        groupKeys = groups.stream().map((groupItem) -> groupItem.getKey()).toArray();
        assertArrayEquals(new Object[] { "Europe", "North America", "South America" }, groupKeys);

        DataGroup group = dataAggregation.getChildDataGroup("Europe");
        groupKeys = group.getChildDataGroups().stream().map((groupItem) -> groupItem.getKey()).toArray();
        assertArrayEquals(new Object[] { "London", "Berlin", "Madrid" }, groupKeys);

        groupComparator = new Comparator<DataGroup>() {
            @Override
            public int compare(DataGroup group1, DataGroup group2) {
                return group2.getKey().compareTo(group1.getKey());
            }
        };
        group.sortChildDataGroups(groupComparator);
        groupKeys = group.getChildDataGroups().stream().map((groupItem) -> groupItem.getKey()).toArray();
        assertArrayEquals(new Object[] { "Madrid", "London", "Berlin" }, groupKeys);
    }
}
