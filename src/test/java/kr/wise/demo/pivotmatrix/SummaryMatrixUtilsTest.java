package kr.wise.demo.pivotmatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kr.wise.demo.pivotgrid.model.DataAggregation;
import kr.wise.demo.pivotgrid.model.DataGroup;
import kr.wise.demo.pivotgrid.model.Paging;

public class SummaryMatrixUtilsTest {

    private static Logger log = LoggerFactory.getLogger(SummaryMatrixUtilsTest.class);

    private DataAggregation dataAggregation;

    @BeforeEach
    public void setUp() throws Exception {
        dataAggregation = new DataAggregation();

        DataGroup group = dataAggregation.addChildDataGroup("North America");

        DataGroup childGroup = group.addChildDataGroup("New York");
        DataGroup grandChildGroup = childGroup.addChildDataGroup("2013");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(12030);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(23700);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(18930);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(23610);
        grandChildGroup = childGroup.addChildDataGroup("2014");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(13770);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(14820);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(11460);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(10740);

        childGroup = group.addChildDataGroup("Los Angeles");
        grandChildGroup = childGroup.addChildDataGroup("2013");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(12775);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(11250);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(10950);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(14900);
        grandChildGroup = childGroup.addChildDataGroup("2014");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(20975);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(8975);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(9375);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(9975);

        group = dataAggregation.addChildDataGroup("Europe");

        childGroup = group.addChildDataGroup("London");
        grandChildGroup = childGroup.addChildDataGroup("2013");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(24650);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(5850);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(13525);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(15100);
        grandChildGroup = childGroup.addChildDataGroup("2014");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(10400);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(21175);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(15450);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(18225);

        childGroup = group.addChildDataGroup("Berlin");
        grandChildGroup = childGroup.addChildDataGroup("2013");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(14725);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(16150);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(14825);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(16750);
        grandChildGroup = childGroup.addChildDataGroup("2014");
        grandChildGroup.addChildDataGroup("Q1").addSummaryValue(19500);
        grandChildGroup.addChildDataGroup("Q2").addSummaryValue(16800);
        grandChildGroup.addChildDataGroup("Q3").addSummaryValue(16750);
        grandChildGroup.addChildDataGroup("Q4").addSummaryValue(12625);
    }

    @Test
    public void testWithFullyExpandedDataAggregation() throws Exception {
        SummaryMatrix matrix = SummaryMatrixFactory
                .createSummaryMatrixFromFullyExpandedDataAggregation(dataAggregation, 2);

        assertEquals(7, matrix.getRows());
        assertEquals(11, matrix.getCols());

        assertEquals(
                Arrays.asList(null, "North America", "New York", "Los Angeles", "Europe", "London",
                        "Berlin"),
                Arrays.stream(matrix.getRowFlattendSummaryDimensions()).map(dim -> dim.getKey())
                        .collect(Collectors.toList()));

        assertEquals(
                Arrays.asList(null, "2013", "Q1", "Q2", "Q3", "Q4", "2014", "Q1", "Q2", "Q3", "Q4"),
                Arrays.stream(matrix.getColFlattendSummaryDimensions()).map(dim -> dim.getKey())
                        .collect(Collectors.toList()));

        log.debug("matrix: {}", matrix);

        final SummaryCell[][] cells = matrix.getSummaryCells();

        assertEquals(matrix.getRows(), cells.length);

        for (int i = 0; i < cells.length; i++) {
            assertEquals(matrix.getCols(), cells[i].length);
        }
    }

    @Test
    public void testPaginationWithSummaryMatrix() throws Exception {
        SummaryMatrix matrix = SummaryMatrixFactory
                .createSummaryMatrixFromFullyExpandedDataAggregation(dataAggregation, 2);
        log.debug("matrix: {}", matrix);

        Paging paging = new Paging();
        paging.setOffset(0);
        paging.setLimit(4);

        SummaryMatrix pageMatrix = SummaryMatrixUtils.slicePageSummaryMatrix(matrix, paging, 2);
        log.debug("pageMatrix: {}", pageMatrix);
        log.debug("paging: {}", paging);

        paging = new Paging();
        paging.setOffset(4);
        paging.setLimit(4);

        pageMatrix = SummaryMatrixUtils.slicePageSummaryMatrix(matrix, paging, 2);
        log.debug("pageMatrix: {}", pageMatrix);
        log.debug("paging: {}", paging);
    }

}
