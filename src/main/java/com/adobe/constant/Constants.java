package com.adobe.constant;

public class Constants {
    public static final String APPLICATION_NAME = "Adobe Case Study";

    public static final String WEB_APP_DIR_LOCATION = "webapp/";
    public static final String WEB_PORT = "8080";

    /** Field Name */
    public static final String ADDRESS_FIELD_NAME = "Address";
    public static final String CITY_FIELD_NAME = "City";
    public static final String STATE_FIELD_NAME = "State_Province";
    public static final String ZIPCODE_FIELD_NAME = "Postal_Code";
    public static final String COUNTRY_FIELD_NAME = "Country";
    public static final String VERIFIED_ADDRESS_FIELD_NAME = "Google Verified Address";
    public static final String LATITUDE_FIELD_NAME = "Latitude";
    public static final String LONGITUDE_FIELD_NAME = "Longitude";

    /** Batch size for reading and updating extremely large files */
    public static final int BATCH_SIZE = 100;

    /** When using comma to parse the csv file, ignore those commas in quotation marks */
    public static final String CSV_PARSING_REG = ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    /** Used to write csv files */
    public static final String CSV_COLUMNS_SEP = ",";
}
