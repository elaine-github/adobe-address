package com.adobe.dao;

import com.adobe.constant.Constants;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import com.google.api.services.sheets.v4.model.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Simple Google Sheet Dao
 * The google file must be
 *    1) small enough to be read into memory all at once;
 *    2) not containing any empty lines;
 *    3) the updated fields (google verified address, latitude and longitude) must be in columns G to I.
 * The updated file will be messed up otherwise because
 * Google Sheet API requires consecutive rows and columns in a single update/batchUpdate.
 */
public class SimpleGoogleSheetAddressDao extends AbstractGoogleSheetAddressDao {
    private static final Logger logger = Logger.getLogger(SimpleGoogleSheetAddressDao.class);
    private static final String UPDATE_RANGE = "G:I";
    private int mStartIndex = 2;
    private List<AddressRecord> mAddressRecords;

    public SimpleGoogleSheetAddressDao(String source, String sheet) {
        this.mSource = source;
        if (sheet != null && !"".equals(sheet)) this.mSheet = sheet;
        init();
    }

    @Override
    public Iterator<AddressRecord> iterator() {
        if (mAddressRecords == null) {
            try {
                ValueRange response = mSheetService.spreadsheets().values()
                        .get(mSpreadsheetId, mSheet)
                        .execute();

                List<List<Object>> values = response.getValues();
                if (values == null || values.size() == 0) {
                    logger.info("No data is found.");
                    return null;
                }

                List<Object> fields = values.remove(0);
                mSheetFieldsMapping = new HashMap<>();
                for (int i=0; i<fields.size(); i++) {
                    mSheetFieldsMapping.put((String) fields.get(i), i);
                }
                checkFieldMapping();

                mAddressRecords = new ArrayList<>();
                for (List row : values) {
                    if (row.size() != 0) {
                        Address address = new Address(row, mSheetFieldsMapping);
                        mAddressRecords.add(new AddressRecord(mStartIndex, address, null));
                    }
                    mStartIndex++;
                }
            } catch (IOException e) {
                logger.error("IOException when reading the google file : " + mSource, e);
                throw new RuntimeException("IOException when reading the google file : " + mSource, e);
            }
        }

        return mAddressRecords.iterator();
    }

    /**
     * Build the updated field name row.
     * @return List of the field names
     */
    private List<Object> buildFieldNameRow() {
        List<Object> fields = new ArrayList<>();
        fields.add(Constants.VERIFIED_ADDRESS_FIELD_NAME);
        fields.add(Constants.LATITUDE_FIELD_NAME);
        fields.add(Constants.LONGITUDE_FIELD_NAME);
        return fields;
    }

    @Override
    public void batchUpdate(List<AddressRecord> addressRecords) {
        if (addressRecords == null || addressRecords.size() == 0) return;

        List<List<Object>> data = new ArrayList<>();
        /** Since the update range is starting from the first row, rewrite the field name row. */
        data.add(buildFieldNameRow());

        for (AddressRecord addressRecord : addressRecords) {
            GoogleVerifiedAddress googleVerifiedAddress = addressRecord.getGoogleVerifiedAddress();

            List<Object> record = new ArrayList<>();
            if (googleVerifiedAddress != null) {
                record.add(googleVerifiedAddress.getGoogleVerifiedAddress());
                record.add(googleVerifiedAddress.getLatitude());
                record.add(googleVerifiedAddress.getLongitude());
            }
            /** If the Google Maps API return null, put empty list into data to skip that record. */
            data.add(record);
        }

        List<ValueRange> contents = buildBatchUpdateContents(
                GOOGLE_SHEET_UPDATE_DIMENSION, mSheet + VALUE_RANGE_SHEET_CELL_SEP + UPDATE_RANGE, data);
        BatchUpdateValuesRequest request = buildBatchUpdateRequest(DEFAULT_VALUE_INPUT_OPTION, contents);

        try {
            BatchUpdateValuesResponse response = mSheetService.spreadsheets()
                    .values()
                    .batchUpdate(mSpreadsheetId, request)
                    .execute();
        } catch (IOException e) {
            logger.error("IOException when updating the google file : " + mSource, e);
            throw new RuntimeException("IOException when updating the google file : " + mSource, e);
        }
    }

    @Override
    /* No resources to be committed. */
    public void commit() {}
}
