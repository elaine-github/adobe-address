package com.adobe.util;

import com.adobe.model.Address;
import com.adobe.model.AddressRecord;
import com.adobe.model.GoogleVerifiedAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;

public class GoogleAddressVerifier {
    private static final Logger logger = Logger.getLogger(GoogleAddressVerifier.class);

    public static GoogleVerifiedAddress verify(Address address){
        try {
            String response = GoogleGeoClient.geocode(address.getAddress(), address.getCity(),
                    address.getState(), address.getPostalCode(), address.getCountry());

            if (response == null) return null;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);
            /** Use the first item in the response result list if it has multiple ones. */
            JsonNode result = jsonNode.get("results").get(0);

            GoogleVerifiedAddress googleVerifiedAddress = null;
            if (result != null) {
                googleVerifiedAddress = new GoogleVerifiedAddress();
                googleVerifiedAddress.setGoogleVerifiedAddress(result.get("formatted_address").asText());
                googleVerifiedAddress.setLatitude(result.get("geometry").get("location").get("lat").asText());
                googleVerifiedAddress.setLongitude(result.get("geometry").get("location").get("lng").asText());
            }

            return googleVerifiedAddress;
        } catch (Exception e) {
            logger.error("Failed to call Google Maps API! " + e);
            throw new RuntimeException("Failed to call Google Maps API! ", e);
        }
    }
}
