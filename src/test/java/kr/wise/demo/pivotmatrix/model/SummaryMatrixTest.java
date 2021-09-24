package kr.wise.demo.pivotmatrix.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class SummaryMatrixTest {

    @Test
    public void testBasic() throws Exception {
        final SummaryDimension dimRow = new SummaryDimension("region");
        SummaryDimension child = dimRow.addChild(new SummaryDimension("North America"));
        child.addChild(new SummaryDimension("New York"));
        child.addChild(new SummaryDimension("Los Angeles"));
        child = dimRow.addChild(new SummaryDimension("Europe"));
        child.addChild(new SummaryDimension("London"));
        child.addChild(new SummaryDimension("Berlin"));

        final SummaryDimension dimCol = new SummaryDimension("date");
        child = dimCol.addChild(new SummaryDimension("2012"));
        child.addChild(new SummaryDimension("Q1"));
        child.addChild(new SummaryDimension("Q2"));
        child.addChild(new SummaryDimension("Q3"));
        child.addChild(new SummaryDimension("Q4"));
        child = dimCol.addChild(new SummaryDimension("2013"));
        child.addChild(new SummaryDimension("Q1"));
        child.addChild(new SummaryDimension("Q2"));
        child.addChild(new SummaryDimension("Q3"));
        child.addChild(new SummaryDimension("Q4"));

        SummaryMatrix matrix = new SummaryMatrix(dimRow, dimCol);

        assertEquals(7, matrix.getRows());
        assertEquals(11, matrix.getCols());

        assertEquals(
                Arrays.asList("region", "North America", "New York", "Los Angeles", "Europe", "London",
                        "Berlin"),
                Arrays.stream(matrix.getRowFlattendSummaryDimensions()).map(dim -> dim.getKey())
                        .collect(Collectors.toList()));

        assertEquals(
                Arrays.asList("date", "2012", "Q1", "Q2", "Q3", "Q4", "2013", "Q1", "Q2", "Q3",
                        "Q4"),
                Arrays.stream(matrix.getColFlattendSummaryDimensions()).map(dim -> dim.getKey())
                        .collect(Collectors.toList()));

        final SummaryCell[][] cells = matrix.getSummaryCells();

        assertEquals(matrix.getRows(), cells.length);

        for (int i = 0; i < cells.length; i++) {
            assertEquals(matrix.getCols(), cells[i].length);
        }
    }
}
