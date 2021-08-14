package kr.wise.demo.pivotgrid.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataAggregationTest {

    private DataAggregation dataAggregation;

    @BeforeEach
    public void setUp() throws Exception {
        dataAggregation = new DataAggregation();

        DataGroup group = dataAggregation.addChildDataGroup("North America");
        group.addChildDataGroup("New York")
                .addSummaryValue(1)
                .addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup("Los Angeles")
                .addSummaryValue(4)
                .addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup("Denver")
                .addSummaryValue(7)
                .addSummaryValue(8)
                .addSummaryValue(9);

        group = dataAggregation.addChildDataGroup("South America");
        group.addChildDataGroup("Rio de Janeiro")
                .addSummaryValue(1)
                .addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup("Buenos Aires")
                .addSummaryValue(4)
                .addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup("Asuncion")
                .addSummaryValue(7)
                .addSummaryValue(8)
                .addSummaryValue(9);

        group = dataAggregation.addChildDataGroup("Europe");
        group.addChildDataGroup("London")
                .addSummaryValue(1)
                .addSummaryValue(2)
                .addSummaryValue(3);
        group.addChildDataGroup("Berlin")
                .addSummaryValue(4)
                .addSummaryValue(5)
                .addSummaryValue(6);
        group.addChildDataGroup("Madrid")
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
