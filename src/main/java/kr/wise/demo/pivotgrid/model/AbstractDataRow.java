package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

abstract public class AbstractDataRow implements DataRow {

    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d+)-(\\d+)-(\\d+).*$");

    private static final String[] QUARTER_LABELS = { "Q1", "Q2", "Q3", "Q4" };

    @Override
    abstract public String getStringValue(final String columnName);

    @Override
    public String getStringValue(final String columnName, final String dateInterval) {
        final String value = getStringValue(columnName);

        if (StringUtils.isEmpty(dateInterval)) {
            return value;
        }

        if (StringUtils.isBlank(value)) {
            return null;
        }

        final Matcher matcher = DATE_PATTERN.matcher(value);

        if (matcher.matches()) {
            if (StringUtils.equalsIgnoreCase("year", dateInterval)) {
                return matcher.group(1);
            }
            else if (StringUtils.equalsIgnoreCase("quarter", dateInterval)) {
                final int quarterIndex = (NumberUtils.toInt(matcher.group(2)) - 1) / 3;
                return QUARTER_LABELS[quarterIndex];
            }
            else if (StringUtils.equalsIgnoreCase("month", dateInterval)) {
                return matcher.group(2);
            }
            else if (StringUtils.equalsIgnoreCase("day", dateInterval)) {
                return matcher.group(3);
            }
        }

        return null;
    }

    @Override
    public Date getDateValue(final String columnName) {
        final String value = getStringValue(columnName);

        try {
            if (StringUtils.isNotEmpty(value)) {
                return DateUtils.parseDate(value, "yyyy-MM-dd");
            }
        }
        catch (ParseException e) {
        }

        return null;
    }

    @Override
    public BigDecimal getBigDecimalValue(final String columnName) {
        return new BigDecimal(getStringValue(columnName));
    }
}
