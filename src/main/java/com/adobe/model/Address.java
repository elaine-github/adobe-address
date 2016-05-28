package com.adobe.model;

import com.adobe.constant.Constants;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class Address {
    private static final Logger logger = Logger.getLogger(Address.class);

    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public Address(List<String> row, Map<String, Integer> fieldsMapping) {
        this.address = getValueFromList(row, fieldsMapping, Constants.ADDRESS_FIELD_NAME);
        this.city = getValueFromList(row, fieldsMapping, Constants.CITY_FIELD_NAME);
        this.state = getValueFromList(row, fieldsMapping, Constants.STATE_FIELD_NAME);
        this.postalCode = getValueFromList(row, fieldsMapping, Constants.ZIPCODE_FIELD_NAME);
        this.country = getValueFromList(row, fieldsMapping, Constants.COUNTRY_FIELD_NAME);
    }

    private String getValueFromList(List<String> list, Map<String, Integer> fieldsMapping, String fieldName) {
        return fieldsMapping.get(fieldName) >= list.size() ?
                "" : list.get(fieldsMapping.get(fieldName));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
