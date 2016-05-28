package com.adobe.dao;

import com.adobe.constant.Constants;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractGoogleSheetAddressDao extends AbstractSheetAddressDao {
    private static final Logger logger = Logger.getLogger(AbstractGoogleSheetAddressDao.class);
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/sheets.googleapis.com-adobe-elaine.json");
    private static final String GOOGLE_SHEET_API_CLIENT_SECRET = "/client_secret.json";
    private static final String GOOGLE_SHEET_ACCESS_TYPE = "offline";
    private static final String GOOGLE_SHEET_AUTH_ROLE = "user";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    /** Global instance of the scopes required by this application.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at .credentials/sheets.googleapis.com-adobe-elaine.json
     */
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

    protected static final String DEFAULT_VALUE_INPUT_OPTION = "RAW";
    protected static final String GOOGLE_SHEET_UPDATE_DIMENSION = "ROWS";
    protected static final String VALUE_RANGE_CELL_SEP = ":";
    protected static final String VALUE_RANGE_SHEET_CELL_SEP = "!";

    private static HttpTransport HTTP_TRANSPORT;
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    protected Sheets mSheetService;
    protected String mSource;
    protected String mSpreadsheetId;
    protected String mSheet = "sheet1";

    /**
     * Initialize the resources that are needed by using Google Sheet API
     */
    protected void init() {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);

            mSheetService = getSheetsService();
            mSpreadsheetId = getSpreadSheetId();
        } catch (Exception e) {
            logger.error("Unable to access the google file!", e);
            throw new RuntimeException("Unable to access the google file!", e);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    protected Credential authorize() throws IOException {
        InputStream in = GoogleSheetAddressDao.class.getResourceAsStream(GOOGLE_SHEET_API_CLIENT_SECRET);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType(GOOGLE_SHEET_ACCESS_TYPE)
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize(GOOGLE_SHEET_AUTH_ROLE);
        logger.info("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Sheets API client service.
     * @return an authorized Sheets API client service
     * @throws IOException
     */
    protected Sheets getSheetsService() throws IOException {
        Credential credential = authorize();
        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(Constants.APPLICATION_NAME)
                .build();
    }

    /**
     * Parse the spread sheet id from input url.
     * @return spread sheet id
     */
    private String getSpreadSheetId()  {
        try {
            URL googleSheetUrl = new URL(mSource);
            String path = googleSheetUrl.getPath();
            return path.split("/")[3];
        } catch (MalformedURLException e) {
            logger.error("Malformed url:" + mSource, e);
            throw new IllegalArgumentException("Malformed url:" + mSource, e);
        } catch (IndexOutOfBoundsException e) {
            logger.error("Can't find Google spreadsheet id in URL:" + mSource, e);
            throw new IllegalArgumentException("Can't find Google spreadsheet id in URL:" + mSource, e);
        }
    }

    /**
     * Build batch update contents.
     * @param dimension The dimension the update should apply to
     * @param range The cells the update should apply to
     * @param data List of data which needs to be updated to the sheet
     * @return List of batch update contents
     */
    protected List<ValueRange> buildBatchUpdateContents(
            String dimension, String range, List<List<Object>> data) {
        ValueRange content = new ValueRange();
        content.setMajorDimension(dimension);
        content.setRange(range);
        content.setValues(data);
        List<ValueRange> contents = new ArrayList<>();
        contents.add(content);

        return contents;
    }

    /**
     * Execute google sheet batch update for a single column
     * @param columnIndex The column which the update should apply to
     * @param startRow The start row that the update should apply to
     * @param endRow The end row that the update should apply to
     * @param data List of data which needs to be updated to the sheet
     */
    protected void executeBatchUpdate(
            String columnIndex, int startRow, int endRow, List<List<Object>> data) {
        String valueRange = mSheet + VALUE_RANGE_SHEET_CELL_SEP +
                columnIndex + startRow + VALUE_RANGE_CELL_SEP + columnIndex + endRow;
        List<ValueRange> contents = buildBatchUpdateContents(GOOGLE_SHEET_UPDATE_DIMENSION, valueRange, data);
        BatchUpdateValuesRequest request = buildBatchUpdateRequest(DEFAULT_VALUE_INPUT_OPTION, contents);

        try {
            BatchUpdateValuesResponse response = mSheetService.spreadsheets()
                    .values()
                    .batchUpdate(mSpreadsheetId, request)
                    .execute();
            if (logger.isDebugEnabled()) {
                logger.debug("Total Update Value Rows : " + response.getTotalUpdatedRows());
            }
        } catch (IOException e) {
            logger.error("IOException when updating the google file : " + mSource, e);
            throw new RuntimeException("IOException when updating the google file : " + mSource, e);
        }
    }

    /**
     * Build batch update request.
     * @param valueInputOption Value input option
     * @param contents List of batch update contents
     * @return The batch update request
     */
    protected BatchUpdateValuesRequest buildBatchUpdateRequest(
            String valueInputOption, List<ValueRange> contents) {
        BatchUpdateValuesRequest request = new BatchUpdateValuesRequest();
        request.setValueInputOption(valueInputOption);
        request.setData(contents);
        return request;
    }
}
