package com.adobe.service;

import com.adobe.constant.Constants;
import com.adobe.dao.GoogleSheetAddressDao;
import com.adobe.dao.IAddressDao;
import com.adobe.dao.LocalSheetAddressDao;
import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import com.adobe.util.GoogleAddressVerifier;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AddressVerifyService {
    private static final Logger logger = Logger.getLogger(AddressVerifyService.class);

    private static void verify(IAddressDao dao){
        Iterator<AddressRecord> iter = dao.iterator();

        List<AddressRecord> addressRecords = new ArrayList<>();
        int count = 0, id = 0;

        while (iter.hasNext()) {
            AddressRecord addressRecord = iter.next();
            Address rawAddress = addressRecord.getRawAddress();

            /** Skip incomplete record */
            if (!"".equals(rawAddress.getAddress()) && !"".equals(rawAddress.getCity()) &&
                    !"".equals(rawAddress.getState()) && !"".equals(rawAddress.getPostalCode()) &&
                    !"".equals(rawAddress.getCountry())) {
                GoogleVerifiedAddress googleVerifiedAddress = GoogleAddressVerifier.verify(rawAddress);
                addressRecord.setGoogleVerifiedAddress(googleVerifiedAddress);
            }

            if (addressRecord != null) {
                if ((id != 0 && id != addressRecord.getId() - 1) || count == Constants.BATCH_SIZE) {
                    dao.batchUpdate(addressRecords);
                    addressRecords.clear();
                    count = 0;
                    id = 0;
                }

                id = addressRecord.getId();
                addressRecords.add(addressRecord);
                count++;
            }
        }
        dao.batchUpdate(addressRecords);
        dao.commit();
    }

    public static void verifyLoalSheet(String path) {
        IAddressDao dao = new LocalSheetAddressDao(path);
        verify(dao);
    }

    public static void verfiyGoogleSheet(String url, String sheetId) {
        IAddressDao dao = new GoogleSheetAddressDao(url, sheetId);
        verify(dao);
    }
}
