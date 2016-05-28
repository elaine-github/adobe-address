package com.adobe.model;

public class AddressRecord {
    private int id;
    private Address rawAddress;
    private GoogleVerifiedAddress googleVerifiedAddress;

    public AddressRecord(int id, Address address, GoogleVerifiedAddress googleVerifiedAddress) {
        this.id = id;
        this.rawAddress = address;
        this.googleVerifiedAddress = googleVerifiedAddress;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public Address getRawAddress() {
        return rawAddress;
    }

    public void setRawAddress(Address rawAddress) {
        this.rawAddress = rawAddress;
    }

    public GoogleVerifiedAddress getGoogleVerifiedAddress() {
        return googleVerifiedAddress;
    }

    public void setGoogleVerifiedAddress(GoogleVerifiedAddress googleVerifiedAddress) {
        this.googleVerifiedAddress = googleVerifiedAddress;
    }
}
