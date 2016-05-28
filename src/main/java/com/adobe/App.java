package com.adobe;

import com.adobe.service.AddressVerifyService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class);

    public static void main1(String[] args) throws Exception {
        // create Options object
        Options options = new Options();
        options.addOption("h", false, "Show help.");
        options.addOption("url", true, "Google sheet url.");
        options.addOption("sheet", true, "Google sheet name.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption("url")) {
            // Run it as a backend service to accept a google sheet url and verify its addresses
            String url = cmd.getOptionValue("url");
            String sheet = cmd.getOptionValue("sheet", null);

            try {
                AddressVerifyService.verfiyGoogleSheet(url, sheet);
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }

        /** Print out the help information to the console */
        System.out.println("" +
                "usage: -h\n" +
                "       -url google_sheet_url [-sheet sheet]\n");
        System.exit(1);
    }

}
