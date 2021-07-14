package kr.wise.demo.pivotgrid.model;

import java.math.BigDecimal;
import java.util.Calendar;

public class SaleData {

    private long id;
    private String region;
    private String country;
    private String city;
    private BigDecimal amount;
    private String date;

    public long getId() {
	return id;
    }

    public void setId(long id) {
	this.id = id;
    }

    public String getRegion() {
	return region;
    }

    public void setRegion(String region) {
	this.region = region;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

    public String getCity() {
	return city;
    }

    public void setCity(String city) {
	this.city = city;
    }

    public BigDecimal getAmount() {
	return amount;
    }

    public void setAmount(BigDecimal amount) {
	this.amount = amount;
    }

    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	this.date = date;
    }

}
