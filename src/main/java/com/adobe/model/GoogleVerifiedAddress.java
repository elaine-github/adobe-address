package com.adobe.model;

import com.adobe.constant.Constants;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class GoogleVerifiedAddress {
    private static final Logger logger = Logger.getLogger(GoogleVerifiedAddress.class);

    private String googleVerifiedAddress;
    private String latitude;
    private String longitude;

    public GoogleVerifiedAddress() {}

    public GoogleVerifiedAddress (String googleVerifiedAddress, String latitude, String longitude) {
        this.googleVerifiedAddress = googleVerifiedAddress == null ? "" : googleVerifiedAddress;
        this.latitude = latitude == null ? "" : latitude;
        this.longitude = longitude == null ? "" : longitude;
    }

    private static String getValueFromList(List<String> list, Map<String, Integer> fieldsMapping, String fieldName) {
        return fieldsMapping.get(fieldName) >= list.size() ?
                null : list.get(fieldsMapping.get(fieldName));
    }

    public static GoogleVerifiedAddress buildGoogleVerifiedAddressObject(
            List<String> data, Map<String, Integer> fieldsMapping) {
        String verifiedAddressStr = getValueFromList(data, fieldsMapping, Constants.VERIFIED_ADDRESS_FIELD_NAME);
        String latitudeStr = getValueFromList(data, fieldsMapping, Constants.LATITUDE_FIELD_NAME);
        String longitudeStr = getValueFromList(data, fieldsMapping, Constants.LONGITUDE_FIELD_NAME);

        if (verifiedAddressStr != null || latitudeStr != null || longitudeStr != null) {
            return new GoogleVerifiedAddress(
                    verifiedAddressStr, latitudeStr, longitudeStr);
        } else {
            return null;
        }
    }

    public String getGoogleVerifiedAddress() {
        return googleVerifiedAddress;
    }

    public void setGoogleVerifiedAddress(String googleVerifiedAddress) {
        this.googleVerifiedAddress = googleVerifiedAddress;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
