package com.adobe.dao;

import com.adobe.constant.Constants;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Handle large files.
 * Read part of the file into memory, update the change into another temporary file,
 * and use commit to finally overwrite the input file with all the updates.
 */
public class LocalSheetAddressDao extends AbstractLocalSheetAddressDao{
    private static final Logger logger = Logger.getLogger(LocalSheetAddressDao.class);

    /** The temporary output file that the data is written into. */
    private String mOutput;

    public LocalSheetAddressDao(String source) {
        this.mSource = source;
        this.mOutput = mSource + ".tmp";

        try {
            parseFileFields();
        } catch (IOException e) {
            logger.error("Unable to parse the file : " + e);
            throw new RuntimeException("Unable to parse the file! ", e);
        }
    }

    private String getFileHeader(BufferedReader br) {
        try {
            String line = br.readLine();
            if (line == null || "".equals(line)) {
                return null;
            }
            return line;
        } catch (IOException e) {
            logger.error("Unable to read the file : " + e);
            throw new RuntimeException("Unable to read the file!", e);
        }
    }
    /**
     * Read the first row of the input file, parse it and initiate mSheetFieldsMapping.
     * @return Array of field names
     * @throws IOException
     */
    private String[] parseFileFields() throws IOException {
        BufferedReader br = getBufferedReader();
        String line  = getFileHeader(br);
        if (line == null ) {
            throw new RuntimeException("The file is empty!");
        }

        String[] fields = line.split(Constants.CSV_PARSING_REG);
        mSheetFieldsMapping = new HashMap<>();
        for (int i=0; i<fields.length; i++) {
            mSheetFieldsMapping.put((String) fields[i], i);
        }
        br.close();

        checkFieldMapping();
        return fields;
    }

    @Override
    public Iterator<AddressRecord> iterator(){
        final BufferedReader br = getBufferedReader();

        /** Skip the first row - fieldname */
        getFileHeader(br);

        return new Iterator<AddressRecord>() {
            Iterator<AddressRecord> iter = null;
            List<AddressRecord> mAddressRecords;
            /** Use line number as the record id.
             * The data starts from line 2 because the first row is fieldnames
             */
            int mStartIndex = 2;

            @Override
            public boolean hasNext() {
                if (iter != null && iter.hasNext()) return true;

                String line = null;
                int count = 0;
                try {
                    mAddressRecords = new ArrayList<>();

                    while (count < Constants.BATCH_SIZE && (line = br.readLine()) != null) {
                        count++;
                        String[] data = line.split(Constants.CSV_PARSING_REG);
                        Address address = new Address(Arrays.asList(data), mSheetFieldsMapping);

                        String verifiedAddressStr = null, latitudeStr = null, longitudeStr = null;
                        if (mSheetFieldsMapping.get(Constants.VERIFIED_ADDRESS_FIELD_NAME) < data.length)
                            verifiedAddressStr = data[mSheetFieldsMapping.get(Constants.VERIFIED_ADDRESS_FIELD_NAME)]
                                    .replaceAll("^\"|\"$", "");
                        if (mSheetFieldsMapping.get(Constants.LATITUDE_FIELD_NAME) < data.length)
                            latitudeStr = data[mSheetFieldsMapping.get(Constants.LATITUDE_FIELD_NAME)];
                        if (mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME) < data.length)
                            longitudeStr = data[mSheetFieldsMapping.get(Constants.LONGITUDE_FIELD_NAME)];

                        if (verifiedAddressStr != null || latitudeStr != null || longitudeStr != null) {
                            GoogleVerifiedAddress gVAddress = new GoogleVerifiedAddress(
                                    verifiedAddressStr, latitudeStr, longitudeStr);
                            mAddressRecords.add(new AddressRecord(mStartIndex++, address, gVAddress));
                        } else {
                            mAddressRecords.add(new AddressRecord(mStartIndex++, address, null));
                        }
                    }

                    if (mAddressRecords.size() == 0) {
                        /** Reach the end of the file */
                        try {
                            br.close();
                        } catch (IOException closeException) {
                            logger.error("Unable to close the file : " + closeException);
                        }
                        return false;
                    }

                    iter = mAddressRecords.iterator();
                    return iter.hasNext();
                } catch (IOException e) {
                    logger.error("Unable to read the file : " + e);
                    try {
                        br.close();
                    } catch (IOException closeException) {
                        logger.error("Unable to close the file : " + closeException);
                    }
                    commit();
                    throw new RuntimeException("Unable to read the file! ", e);
                }
            }

            @Override
            public AddressRecord next() {
                return iter.next();
            }

            @Override
            public void remove() {}
        };
    }

    @Override
    public void batchUpdate(List<AddressRecord> addressRecords){
        if (addressRecords == null || addressRecords.size() == 0) return;

        StringBuilder sb = new StringBuilder();
        for (AddressRecord addressRecord : addressRecords) {
            writeAddressRecordToSB(sb, addressRecord);
        }

        try {
            if (mWriter == null) initOutputFile(mOutput);
            mWriter.append(sb.toString());
            mWriter.flush();
        } catch (IOException e) {
            logger.error("Unable to write data to the file : " + mOutput + "! " + e);
        }
    }

    /**Close the sources and overwrite the input file */
    @Override
    public void commit() {
        close();
        File fileToDelete = new File(mSource);
        File fileToRename = new File(mOutput);
        if (fileToRename.exists() && fileToDelete.delete()) fileToRename.renameTo(fileToDelete);
    }
}
