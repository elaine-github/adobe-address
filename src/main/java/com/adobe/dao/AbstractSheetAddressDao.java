package com.adobe.dao;

import com.adobe.constant.Constants;

import java.util.Arrays;
import java.util.Map;

public abstract class AbstractSheetAddressDao implements IAddressDao {
    /** Key - column name; value = column number starting from 0 */
    protected Map<String, Integer> mSheetFieldsMapping;

    protected void checkFieldMapping() {
        String[] fields = new String[]{Constants.ADDRESS_FIELD_NAME, Constants.CITY_FIELD_NAME,
                Constants.STATE_FIELD_NAME, Constants.ZIPCODE_FIELD_NAME, Constants.COUNTRY_FIELD_NAME,
                Constants.VERIFIED_ADDRESS_FIELD_NAME, Constants.LATITUDE_FIELD_NAME,
                Constants.LONGITUDE_FIELD_NAME};

        for (String field : fields) {
            if (!mSheetFieldsMapping.containsKey(field)) {
                throw new RuntimeException("Unable to get the header. The file must be csv file, and " +
                        "the field names should be " + Arrays.toString(fields));
            }
        }
    }
}
