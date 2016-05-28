package com.adobe.dao;

import com.adobe.constant.Constants;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.util.*;

public class LocalSheetAddressDaoTest {

    private Map<String, Integer> mFieldMapping = new HashMap<>();

    private final String[] mData1 = new String[] {"Fake Address1", "Fake City1",
            "Fake State1", "Fake Postal_Code1", "Fake Country1"};

    private final String[] mData2 = new String[] {"Fake Address2", "Fake City2",
            "Fake State2", "Fake Postal_Code2", "Fake Country2"};

    private final String mDirPath = "src/test/resources/";

    private final String mFileName = mDirPath + "test.csv";

    private final String mEmptyFileName = mDirPath + "empty.csv";

    private AddressRecord mAddressRecord1, mAddressRecord2;

    @Before
    public void beforeTest() {
        mFieldMapping.put(Constants.ADDRESS_FIELD_NAME, 0);
        mFieldMapping.put(Constants.CITY_FIELD_NAME, 1);
        mFieldMapping.put(Constants.STATE_FIELD_NAME, 2);
        mFieldMapping.put(Constants.ZIPCODE_FIELD_NAME, 3);
        mFieldMapping.put(Constants.COUNTRY_FIELD_NAME, 4);
        mFieldMapping.put(Constants.VERIFIED_ADDRESS_FIELD_NAME, 5);
        mFieldMapping.put(Constants.LATITUDE_FIELD_NAME, 6);
        mFieldMapping.put(Constants.LONGITUDE_FIELD_NAME, 7);

        Address address1 = new Address(Arrays.asList(mData1), mFieldMapping);
        mAddressRecord1 = new AddressRecord(1, address1, null);

        Address address2 = new Address(Arrays.asList(mData2), mFieldMapping);
        mAddressRecord2 = new AddressRecord(2, address2, null);

        generateEmptyFile(mEmptyFileName);
    }

    private void generateEmptyFile(String fileName) {
        File file = new File(fileName);
        try {
            if (!file.exists() || file.delete())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Rule
    public ExpectedException mExpectedEx = ExpectedException.none();

    @Test
    public void testReadFileNotFound() throws Exception {
        mExpectedEx.expect(RuntimeException.class);
        mExpectedEx.expectMessage("Unable to read the file");
        String fileName = mDirPath + "fake.csv";
        LocalSheetAddressDao dao = new LocalSheetAddressDao(fileName);
    }

    @Test
    public void testReadEmptyFile() throws Exception {
        mExpectedEx.expect(RuntimeException.class);
        mExpectedEx.expectMessage("empty");
        LocalSheetAddressDao dao = new LocalSheetAddressDao(mEmptyFileName);
        Iterator<AddressRecord> iter = dao.iterator();
    }

    @Test
    public void testRead() throws Exception {
        generateTestCSV(mFileName);
        LocalSheetAddressDao dao = new LocalSheetAddressDao(mFileName);
        Iterator<AddressRecord> iter = dao.iterator();
        Assert.assertTrue(iter.hasNext());

        AddressRecord addressRecord = iter.next();
        Assert.assertNotNull(addressRecord);
        Assert.assertNull(addressRecord.getGoogleVerifiedAddress());

        Address address = addressRecord.getRawAddress();
        Assert.assertEquals(address.getAddress(), mData1[0]);
        Assert.assertEquals(address.getCity(), mData1[1]);
        Assert.assertEquals(address.getState(), mData1[2]);
        Assert.assertEquals(address.getPostalCode(), mData1[3]);
        Assert.assertEquals(address.getCountry(), mData1[4]);

        Assert.assertFalse(iter.hasNext());
    }

    private void generateTestCSV(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists() && file.delete())
                file.createNewFile();

            FileWriter writer = new FileWriter(fileName);
            StringBuilder sb = new StringBuilder();

            sb.append(Constants.ADDRESS_FIELD_NAME + ",");
            sb.append(Constants.CITY_FIELD_NAME + ",");
            sb.append(Constants.STATE_FIELD_NAME + ",");
            sb.append(Constants.ZIPCODE_FIELD_NAME + ",");
            sb.append(Constants.COUNTRY_FIELD_NAME + ",");
            sb.append(Constants.VERIFIED_ADDRESS_FIELD_NAME + ",");
            sb.append(Constants.LATITUDE_FIELD_NAME + ",");
            sb.append(Constants.LONGITUDE_FIELD_NAME + "\n");

            for (String d : mData1) {
                sb.append(d + Constants.CSV_COLUMNS_SEP);
            }
            sb.append('\n');

            writer.append(sb.toString());
            writer.flush();
            writer.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private BufferedReader getBufferedReader(String fileName){
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            return br;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Unable to find file : " + fileName, e);
        }
    }

    @Test
    public void testBatchUpdate() throws Exception {
        generateTestCSV(mFileName);
        LocalSheetAddressDao dao = new LocalSheetAddressDao(mFileName);
        dao.batchUpdate(Arrays.asList(mAddressRecord1, mAddressRecord2));
        dao.commit();

        BufferedReader br = getBufferedReader(mFileName);
        Assert.assertNotNull(br.readLine());

        String line  = br.readLine();
        Assert.assertNotNull(line);
        Assert.assertTrue(line.startsWith(mData1[0]));

        line  = br.readLine();
        Assert.assertNotNull(line);
        Assert.assertTrue(line.startsWith(mData2[0]));

        Assert.assertNull(br.readLine());
    }

    /**
     * The original file should be not changed if the updated data is null
     * @throws Exception : IOException when reading from buffer
     */
    @Test
    public void testBatchUpdateNull() throws Exception {
        generateTestCSV(mFileName);
        LocalSheetAddressDao dao = new LocalSheetAddressDao(mFileName);
        dao.batchUpdate(null);
        dao.commit();

        BufferedReader br = getBufferedReader(mFileName);
        Assert.assertNotNull(br.readLine());
        Assert.assertNotNull(br.readLine());
    }
}