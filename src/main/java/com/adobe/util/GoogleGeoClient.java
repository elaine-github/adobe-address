package com.adobe.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class GoogleGeoClient {
    private static final Logger logger = Logger.getLogger(GoogleGeoClient.class);

    private static final String PROTOCOL = "https";
    private static final String HOST = "maps.googleapis.com";
    private static final String GOOGLE_GEO_API_KEY = "AIzaSyDF4kBQ4Z704QXQcUH3AHiKCXnlktB7Yvw";
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GEOCODE_REQUEST_METHOD = "GET";

    /**
     * Use Google Maps API to verify an address
     * @param address
     * @param city
     * @param state
     * @param postalCode
     * @param country
     * @return Response
     * @throws Exception
     */
    public static String geocode(String address, String city, String state, String postalCode, String country)
            throws Exception {
        String completeAddress = address + ", " + city + ", " + state + " " + postalCode + ", " + country;
        URL url = new URL(PROTOCOL, HOST,
                "/maps/api/geocode/json?address=" + URLEncoder.encode(completeAddress, "UTF-8")
                        + "&key=" + GOOGLE_GEO_API_KEY);
        logger.debug(url.toString());

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod(GEOCODE_REQUEST_METHOD);
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        if (responseCode != 200) {
            logger.info("response code to call url.toString() is " + responseCode);
            return null;
        }

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}
