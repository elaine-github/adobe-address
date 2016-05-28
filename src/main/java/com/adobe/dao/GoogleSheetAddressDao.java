package com.adobe.dao;

import com.adobe.constant.Constants;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Sophisticated  Google Sheet Dao
 *      The file could be large and contain empty lines.
 *      The fields are not necessary to be consecutive.
 *      The updated columns can be in anywhere.
 */
public class GoogleSheetAddressDao extends AbstractGoogleSheetAddressDao {
    private static final Logger logger = Logger.getLogger(GoogleSheetAddressDao.class);
    /** The first row is the field name */
    private static final String FIELD_NAME_RANGE = "1:1";
    private static final String GOOGLE_SHEET_FIRST_COLUMN = "A";

    /** Column character for the three updated columns */
    private String mVerifiedGoogleColumnIndex;
    private String mLatitudeColumnIndex;
    private String mLongitudeColumnIndex;

    public GoogleSheetAddressDao(String source, String sheet) {
        this.mSource = source;
        if (sheet != null && !"".equals(sheet)) this.mSheet = sheet;
        init();
        parseFields();
    }

    /**
     * Parse field names:
     *      1. put the field name and its column index into mSheetFieldsMapping;
     *      2. et the column number for the updated fields.
     */
    private void parseFields() {
        try {
            ValueRange response = mSheetService.spreadsheets()
                    .values()
                    .get(mSpreadsheetId, mSheet + VALUE_RANGE_SHEET_CELL_SEP + FIELD_NAME_RANGE)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.size() == 0) {
                logger.info("No data is found.");
                return;
            } else {
                List<Object> fields = values.get(0);
                mSheetFieldsMapping = new HashMap<>();
                for (int i=0; i<fields.size(); i++) {
                    String field = (String) fields.get(i);
                    mSheetFieldsMapping.put(field, i);

                    switch (field) {
                        case Constants.VERIFIED_ADDRESS_FIELD_NAME:
                            mVerifiedGoogleColumnIndex = String.valueOf((char)(i+'A'));
                            break;
                        case Constants.LATITUDE_FIELD_NAME:
                            mLatitudeColumnIndex = String.valueOf((char)(i+'A'));
                            break;
                        case Constants.LONGITUDE_FIELD_NAME:
                            mLongitudeColumnIndex = String.valueOf((char)(i+'A'));
                            break;
                    }
                }
                checkFieldMapping();
            }
        } catch (IOException e) {
            logger.error("Unable to access the google file!", e);
            throw new RuntimeException("Unable to access the google file!", e);
        }
    }

    @Override
    public Iterator<AddressRecord> iterator() {
        return new Iterator<AddressRecord>() {
            List<AddressRecord> mAddressRecords = null;
            Iterator<AddressRecord> mIter = null;
            int mStartIndex = 2;

            @Override
            public boolean hasNext() {
                if (mIter != null && mIter.hasNext()) return true;

                String range = mSheet + VALUE_RANGE_SHEET_CELL_SEP +
                        mStartIndex + VALUE_RANGE_CELL_SEP + (mStartIndex + Constants.BATCH_SIZE - 1);
                mStartIndex += Constants.BATCH_SIZE;
                try {
                    mAddressRecords = new ArrayList<>();
                    ValueRange response = mSheetService.spreadsheets().values()
                            .get(mSpreadsheetId, range)
                            .execute();

                    String rangeInResponse = response.getRange();
                    int startIndex = Integer.parseInt(
                            rangeInResponse.substring(rangeInResponse.indexOf(GOOGLE_SHEET_FIRST_COLUMN) + 1,
                                    rangeInResponse.indexOf(VALUE_RANGE_CELL_SEP)));
                    List<List<Object>> values = response.getValues();

                    if (values == null) return false;

                    for (List row : values) {
                        if (row.size() != 0) {
                            Address address = new Address(row, mSheetFieldsMapping);

                            String verifiedAddressStr = null, latitudeStr = null, longitudeStr = null;
                            if (mSheetFieldsMapping.get(Constants.VERIFIED_ADDRESS_FIELD_NAME) < row.size())
                                verifiedAddressStr = (String)row.get(mSheetFieldsMapping.get(Constants.VERIFIED_ADDRESS_FIELD_NAME));
                            if (mSheetFieldsMapping.get(Constants.LATITUDE_FIELD_NAME) < row.size())
                                latitudeStr = (String)row.get(mSheetFieldsMapping.get(Constants.LATITUDE_FIELD_NAME));
                            if (mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME) < row.size())
                                longitudeStr = (String)row.get(mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME));

                            if (verifiedAddressStr != null || latitudeStr != null || longitudeStr != null) {
                                GoogleVerifiedAddress gVAddress = new GoogleVerifiedAddress(
                                        verifiedAddressStr, latitudeStr, longitudeStr);
                                mAddressRecords.add(new AddressRecord(startIndex, address, gVAddress));
                            } else {
                                mAddressRecords.add(new AddressRecord(startIndex, address, null));
                            }
                        }
                        startIndex++;
                    }
                    mIter = mAddressRecords.iterator();
                    return mIter.hasNext();
                } catch (IOException e) {
                    logger.error("IOException when reading the google file : " + mSource, e);
                    throw new RuntimeException("IOException when reading the google file : " + mSource, e);
                }
            }

            @Override
            public AddressRecord next() {
                return mIter.next();
            }

            @Override
            public void remove() {}
        };
    }

    @Override
    public void batchUpdate(List<AddressRecord> addressRecords) {
        if (addressRecords == null || addressRecords.size() == 0) return;

        List<List<Object>> verfiedAddressData = new ArrayList<>();
        List<List<Object>> latitudeData = new ArrayList<>();
        List<List<Object>> longitudeData = new ArrayList<>();
        for (AddressRecord addressRecord : addressRecords) {
            GoogleVerifiedAddress googleVerifiedAddress = addressRecord.getGoogleVerifiedAddress();

            if (googleVerifiedAddress != null) {
                verfiedAddressData.add(Arrays.asList((Object)googleVerifiedAddress.getGoogleVerifiedAddress()));
                latitudeData.add(Arrays.asList((Object) googleVerifiedAddress.getLatitude()));
                longitudeData.add(Arrays.asList((Object) googleVerifiedAddress.getLongitude()));
            } else {
                /** handle the record that has no google verified address */
                verfiedAddressData.add(Arrays.asList(""));
                latitudeData.add(Arrays.asList(""));
                longitudeData.add(Arrays.asList(""));
            }
        }

        int startRow = addressRecords.get(0).getId();
        int endRow = addressRecords.get(addressRecords.size()-1).getId();
        executeBatchUpdate(mVerifiedGoogleColumnIndex, startRow, endRow, verfiedAddressData);
        executeBatchUpdate(mLatitudeColumnIndex, startRow, endRow, latitudeData);
        executeBatchUpdate(mLongitudeColumnIndex, startRow, endRow, longitudeData);
    }

    @Override
    /* No resources to be committed. */
    public void commit() {}
}
