package com.adobe.dao;

import com.adobe.model.AddressRecord;

import java.util.Iterator;
import java.util.List;

public interface IAddressDao {
    /**
     * Read one page of data into memory.
     * @return Iterator
     */
    Iterator<AddressRecord> iterator();

    /**
     * Batch update operation for efficiency
     * @param addressRecords List of data that needs to be updated.
     */
    void batchUpdate(List<AddressRecord> addressRecords);

    /** Commit updates */
    void commit();
}
