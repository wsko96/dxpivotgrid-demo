package kr.wise.demo.pivotmatrix;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SummaryValue {

    private static final BigDecimal ZERO = BigDecimal.valueOf(0);

    private String fieldName;
    private SummaryType summaryType;
    private long count;
    private BigDecimal sum;
    private BigDecimal value;
    private String textValue;
    private Set<BigDecimal> distinctValues;

    public SummaryValue() {
        this(null, null, null);
    }

    public SummaryValue(final String fieldName, final SummaryType summaryType) {
        this(fieldName, summaryType, null);
    }

    public SummaryValue(final String fieldName, final SummaryType summaryType, final BigDecimal value) {
        this.fieldName = fieldName;
        this.summaryType = summaryType;
        this.value = value;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("f")
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    @JsonProperty("t")
    public SummaryType getSummaryType() {
        return summaryType;
    }

    public void setSummaryType(final SummaryType summaryType) {
        this.summaryType = summaryType;
    }

    @JsonProperty("c")
    @JsonInclude(Include.NON_DEFAULT)
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long incrementCount() {
        return ++count;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("s")
    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonProperty("tv")
    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
    	this.textValue = textValue;
    }

    public BigDecimal addSum(final BigDecimal augend) {
        if (sum != null) {
            sum = sum.add(augend);
        } else {
            sum = augend;
        }

        return sum;
    }

    public void addDistinctValue(final BigDecimal distinctValue) {
        if (distinctValues != null) {
            distinctValues = new HashSet<>();
        }

        distinctValues.add(distinctValue);
    }

    @JsonProperty("v")
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @JsonIgnore
    public BigDecimal getRepresentingValue() {
        switch (summaryType) {
        case COUNT:
            return BigDecimal.valueOf(count);
        case COUNTDISTINCT:
            return BigDecimal.valueOf(distinctValues != null ? distinctValues.size() : 0);
        case AVERAGE:
        case AVG:
            if (count == 0) {
                return ZERO;
            }
            return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        default:
            break;
        }

        if (value != null) {
            return value;
        }

        return sum;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SummaryValue)) {
            return false;
        }

        final SummaryValue that = (SummaryValue) o;

        if (!StringUtils.equals(fieldName, that.fieldName)) {
            return false;
        }

        if (!Objects.equals(summaryType, that.summaryType)) {
            return false;
        }

        if (count != that.count) {
            return false;
        }

        if (!Objects.equals(sum, that.sum)) {
            return false;
        }

        if (!Objects.equals(value, that.value)) {
            return false;
        }

        if (!Objects.equals(textValue, that.textValue)) {
            return false;
        }

        if (!Objects.equals(distinctValues, that.distinctValues)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fieldName).append(summaryType).append(count).append(sum)
                .append(value).append(textValue).append(distinctValues).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("fieldName", fieldName)
                .append("summaryType", summaryType).append("count", count).append("sum", sum)
                .append("value", value).append("textValue", textValue)
                .append("distinctValues", distinctValues).toString();
    }
}
