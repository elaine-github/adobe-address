package com.adobe.dao;

import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GoogleSheetAddressDaoTest {
    private final String GOOGLE_SHEET_URL =
            "https://docs.google.com/spreadsheets/d/1lI5Ssd0epMRf4R_GZ8xgpB46V1syrDpAiy6oiWTGfeA/edit#gid=0";

    private final String[] FAKE_DATA = new String[] {"Fake Address", "Fake City",
            "Fake State", "Fake Postal_Code", "Fake Country"};

    private final String[] FAKE_GOOGLE_VERIFIED_ADDRESS_DATA = new String[] {
            "Fake Google Verified Address", "1.0", "1.0"};

    @Test
    public void testRead() throws Exception {
        IAddressDao dao = new GoogleSheetAddressDao(GOOGLE_SHEET_URL, null);
        List<AddressRecord> addressRecords = new ArrayList<>();
        addressRecords.add(new AddressRecord(2, null, new GoogleVerifiedAddress()));
        dao.batchUpdate(addressRecords);

        Iterator<AddressRecord> iter = dao.iterator();
        Assert.assertTrue(iter.hasNext());

        AddressRecord addressRecord = iter.next();
        Assert.assertNotNull(addressRecord);

        Address address = addressRecord.getRawAddress();
        Assert.assertEquals(address.getAddress(), FAKE_DATA[0]);
        Assert.assertEquals(address.getCity(), FAKE_DATA[1]);
        Assert.assertEquals(address.getState(), FAKE_DATA[2]);
        Assert.assertEquals(address.getPostalCode(), FAKE_DATA[3]);
        Assert.assertEquals(address.getCountry(), FAKE_DATA[4]);
    }

    @Rule
    public ExpectedException mExpectedEx = ExpectedException.none();

    @Test
    public void testReadNonExistSheet() throws Exception {
        mExpectedEx.expect(RuntimeException.class);
        mExpectedEx.expectMessage("Unable to access the google file");
        IAddressDao dao = new GoogleSheetAddressDao(GOOGLE_SHEET_URL, "fake sheet");
        Iterator<AddressRecord> iter = dao.iterator();
    }

    @Test
    public void testBatchUpdate() throws Exception {
        IAddressDao dao = new GoogleSheetAddressDao(GOOGLE_SHEET_URL, null);

        /** Update the first record's verfied address to empty value */
        List<AddressRecord> addressRecords = new ArrayList<>();
        addressRecords.add(new AddressRecord(2, null, null));
        dao.batchUpdate(addressRecords);

        /** Verify the update with empty value. */
        Iterator<AddressRecord> iter = dao.iterator();
        Assert.assertTrue(iter.hasNext());
        AddressRecord addressRecord = iter.next();
        Assert.assertNotNull(addressRecord);
        Assert.assertNull(addressRecord.getGoogleVerifiedAddress());

        /** Update the verified address with fake value */
        GoogleVerifiedAddress gVAddress = new GoogleVerifiedAddress();
        gVAddress.setGoogleVerifiedAddress(FAKE_GOOGLE_VERIFIED_ADDRESS_DATA[0]);
        gVAddress.setLatitude(FAKE_GOOGLE_VERIFIED_ADDRESS_DATA[1]);
        gVAddress.setLongitude(FAKE_GOOGLE_VERIFIED_ADDRESS_DATA[2]);

        addressRecord.setGoogleVerifiedAddress(gVAddress);
        addressRecords.clear();
        addressRecords.add(addressRecord);
        dao.batchUpdate(addressRecords);

        /** Check the non-empty update. */
        iter = dao.iterator();
        Assert.assertTrue(iter.hasNext());
        addressRecord = iter.next();
        Assert.assertNotNull(addressRecord);
        Assert.assertNotNull(addressRecord.getGoogleVerifiedAddress());
        Assert.assertEquals(addressRecord.getGoogleVerifiedAddress().getGoogleVerifiedAddress(),
                FAKE_GOOGLE_VERIFIED_ADDRESS_DATA[0]);
    }
}