package kr.wise.demo.pivotgrid.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import kr.wise.demo.pivotgrid.model.DataRow;

public class ObjectNodeDataRow implements DataRow {

    private static final Pattern DATE_PATTERN = Pattern.compile("^(\\d+)-(\\d+)-(\\d+).*$");

    private final ObjectNode dataNode;

    public ObjectNodeDataRow(final ObjectNode dataNode) {
        this.dataNode = dataNode;
    }

    public String getString(final String columnName) {
        return dataNode.get(columnName).asText();
    }

    public String getString(final String columnName, final String dateInterval) {
        if (StringUtils.isEmpty(dateInterval)) {
            return getString(columnName);
        }

        final String value = dataNode.get(columnName).asText();

        if (StringUtils.isBlank(value)) {
            return null;
        }

        final Matcher matcher = DATE_PATTERN.matcher(value);

        if (matcher.matches()) {
            if ("year".equals(dateInterval)) {
                return matcher.group(1);
            }
            else if ("month".equals(dateInterval)) {
                return matcher.group(2);
            }
            else if ("day".equals(dateInterval)) {
                return matcher.group(3);
            }
        }

        return null;
    }

    public Date getDate(final String columnName) {
        final String value = getString(columnName);

        try {
            if (StringUtils.isNotEmpty(value)) {
                return DateUtils.parseDate(value, "yyyy-MM-dd");
            }
        }
        catch (ParseException e) {
        }

        return null;
    }

    public BigDecimal getBigDecimal(final String columnName) {
        return new BigDecimal(getString(columnName));
    }

}
