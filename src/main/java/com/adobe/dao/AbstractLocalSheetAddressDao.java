package com.adobe.dao;

import com.adobe.constant.Constants;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.StringJoiner;

public abstract class AbstractLocalSheetAddressDao extends AbstractSheetAddressDao {
    private static final Logger logger = Logger.getLogger(AbstractLocalSheetAddressDao.class);
    /** The input file path that needs to be verified. */
    protected String mSource;
    protected FileWriter mWriter;

    protected BufferedReader getBufferedReader(){
        try {
            BufferedReader br = new BufferedReader(new FileReader(mSource));
            return br;
        } catch (FileNotFoundException e) {
            logger.error("Unable to read the file : " + mSource, e);
            throw new IllegalArgumentException("Unable to read the file : " + mSource, e);
        }
    }

    /**
     * Write the field names to the first row of the output file
     * @param fileName Output file name
     * @throws IOException
     */
    protected void initOutputFile (String fileName) throws IOException {
        File outputFile = new File(fileName);
        if (outputFile.exists()) outputFile.delete();

        StringJoiner sj = new StringJoiner(Constants.CSV_COLUMNS_SEP);
        sj.add(Constants.ADDRESS_FIELD_NAME);
        sj.add(Constants.CITY_FIELD_NAME);
        sj.add(Constants.STATE_FIELD_NAME);
        sj.add(Constants.ZIPCODE_FIELD_NAME);
        sj.add(Constants.COUNTRY_FIELD_NAME);
        sj.add(Constants.VERIFIED_ADDRESS_FIELD_NAME);
        sj.add(Constants.LATITUDE_FIELD_NAME);
        sj.add(Constants.LONGITUDE_FIELD_NAME);
        sj.add("\n");
        mWriter = new FileWriter(fileName);
        mWriter.append(sj.toString());
    }

    /**
     * Convert address record into String
     * @param sb The StringBuilder object that the data is written into
     * @param addressRecord The object that needs to be converted
     */
    protected void writeAddressRecordToSB(StringBuilder sb, AddressRecord addressRecord) {
        Address address = addressRecord.getRawAddress();
        sb.append(address.getAddress() + Constants.CSV_COLUMNS_SEP);
        sb.append(address.getCity() + Constants.CSV_COLUMNS_SEP);
        sb.append(address.getState() + Constants.CSV_COLUMNS_SEP);
        sb.append(address.getPostalCode() + Constants.CSV_COLUMNS_SEP);
        sb.append(address.getCountry() + Constants.CSV_COLUMNS_SEP);

        GoogleVerifiedAddress googleVerifiedAddress = addressRecord.getGoogleVerifiedAddress();
        if (googleVerifiedAddress != null) {
            //The google verified address may contain commas, put it in quotation marks to prevent confusion
            sb.append("\"" + googleVerifiedAddress.getGoogleVerifiedAddress() + "\""+ Constants.CSV_COLUMNS_SEP);
            sb.append(googleVerifiedAddress.getLatitude() + Constants.CSV_COLUMNS_SEP);
            sb.append(googleVerifiedAddress.getLongitude() + Constants.CSV_COLUMNS_SEP);
        }
        sb.append('\n');
    }

    /**
     * Close the file writer.
     */
    protected void close() {
        if (mWriter != null) {
            try {
                mWriter.close();
            } catch (IOException e) {
                logger.error("Unable to close the file writer : " + e);
            }
        }
    }
}
