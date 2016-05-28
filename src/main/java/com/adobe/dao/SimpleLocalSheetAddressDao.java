package com.adobe.dao;

import com.adobe.constant.Constants;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Handle small files.
 * Read the file into memory all at once, update the change in the memory,
 * and use commit to finally write the change to the file.
 */
public class SimpleLocalSheetAddressDao extends AbstractLocalSheetAddressDao {
    private static final Logger logger = Logger.getLogger(SimpleLocalSheetAddressDao.class);
    private List<AddressRecord> mAddressRecords;
    protected int mStartIndex = 2;

    public SimpleLocalSheetAddressDao(String source) {
        this.mSource = source;
    }

    @Override
    public Iterator<AddressRecord> iterator() {
        if (mAddressRecords == null) {
            try {
                BufferedReader br = getBufferedReader();
                String line = br.readLine();
                if (line == null || "".equals(line)) {
                    logger.info("Empty file : " + mSource);
                    return null;
                }

                String[] fields = line.split(Constants.CSV_PARSING_REG);
                mSheetFieldsMapping = new HashMap<>();
                for (int i = 0; i < fields.length; i++) {
                    mSheetFieldsMapping.put((String) fields[i], i);
                }
                checkFieldMapping();

                mAddressRecords = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(Constants.CSV_PARSING_REG);
                    Address address = new Address(Arrays.asList(data), mSheetFieldsMapping);

                    GoogleVerifiedAddress gVAddress = new GoogleVerifiedAddress();
                    if (mSheetFieldsMapping.get(Constants.VERIFIED_ADDRESS_FIELD_NAME) < data.length)
                        gVAddress.setGoogleVerifiedAddress(
                                data[mSheetFieldsMapping.get(Constants.VERIFIED_ADDRESS_FIELD_NAME)]);

                    if (mSheetFieldsMapping.get(Constants.LATITUDE_FIELD_NAME) < data.length)
                        gVAddress.setLatitude(data[mSheetFieldsMapping.get(Constants.LATITUDE_FIELD_NAME)]);

                    if (mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME) < data.length &&
                            !"".equals(data[mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME)]))
                        gVAddress.setLatitude(data[mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME)]);

                    AddressRecord addressRecord = new AddressRecord(mStartIndex++, address, gVAddress);
                    mAddressRecords.add(addressRecord);
                }
                br.close();
            } catch (IOException e) {
                commit();
                logger.error("Unable to parse the file : " + e);
                throw new RuntimeException("Unable to parse the file.", e);
            }
        }
        return mAddressRecords.iterator();
    }

    @Override
    public void batchUpdate(List<AddressRecord> addressRecords) {
        mAddressRecords.removeAll(mAddressRecords);
        mAddressRecords.addAll(addressRecords);
    }

    /**
     * Write the records in memory into file.
     */
    @Override
    public void commit() {
        try {
            if (mWriter == null) initOutputFile(mSource);

            StringBuilder sb = new StringBuilder();
            for (AddressRecord addressRecord : mAddressRecords) {
                writeAddressRecordToSB(sb, addressRecord);
            }

            mWriter.append(sb.toString());
            mWriter.flush();
        } catch (IOException e) {
            logger.error("Unable to write data to the file : " + mSource + "! " + e);
        }
        close();
    }
}
