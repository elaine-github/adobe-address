package com.adobe.servlets;

import com.adobe.constant.Constants;
import com.adobe.dao.IAddressDao;
import com.adobe.dao.LocalSheetAddressDao;
import com.adobe.model.AddressRecord;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@WebServlet(
        name = "ResultServlet",
        urlPatterns = {"/result.html"}
)
public class ResultServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        String filePath = request.getParameter("fileToShow");
        IAddressDao dao = new LocalSheetAddressDao(filePath);
        List<AddressRecord> addressRecords = new ArrayList<>();
        Iterator<AddressRecord> iter = dao.iterator();
        while (iter.hasNext()) addressRecords.add(iter.next());

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        response.getWriter().println(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n" +
                        "   \"http://www.w3.org/TR/html4/strict.dtd\">\n" +
                        "<HTML>\n" +
                        "   <HEAD>\n" +
                        "      <TITLE>Address Verify</TITLE>\n" +
                        "   </HEAD>\n" +
                        "   <BODY>\n");

        /** Write table header */
        response.getWriter().println("<table style=\"width:100%\">\n" +
                "  <tr>\n" +
                "   <td>" + Constants.ADDRESS_FIELD_NAME + "</td>\n" +
                "   <td>" + Constants.CITY_FIELD_NAME + "</td>\n" +
                "   <td>" + Constants.STATE_FIELD_NAME + "</td> \n" +
                "   <td>" + Constants.ZIPCODE_FIELD_NAME + "</td>\n" +
                "   <td>" + Constants.COUNTRY_FIELD_NAME + "</td>\n" +
                "   <td>" + Constants.VERIFIED_ADDRESS_FIELD_NAME + "</td> \n" +
                "   <td>" + Constants.LATITUDE_FIELD_NAME + "</td>\n" +
                "   <td>" + Constants.LATITUDE_FIELD_NAME + "</td> \n" +
                "  </tr>\n");

        /** Write data */

        for (AddressRecord ad : addressRecords) {
            StringBuilder sb = new StringBuilder();
            sb.append("<tr>\n");
            if (ad.getRawAddress() != null)
                sb.append(
                    "<td>" + ad.getRawAddress().getAddress() + "</td>\n" +
                    "<td>" + ad.getRawAddress().getCity() + "</td> \n" +
                    "<td>" + ad.getRawAddress().getState() + "</td> \n" +
                    "<td>" + ad.getRawAddress().getPostalCode() + "</td> \n" +
                    "<td>" + ad.getRawAddress().getCountry() + "</td> \n");
            if (ad.getGoogleVerifiedAddress() != null)
                sb.append(
                    "<td>" + ad.getGoogleVerifiedAddress().getGoogleVerifiedAddress().replaceAll("^\"|\"$", "") + "</td> \n" +
                    "<td>" + ad.getGoogleVerifiedAddress().getLatitude() + "</td> \n" +
                    "<td>" + ad.getGoogleVerifiedAddress().getLongitude() + "</td> \n");
            sb.append("</tr>\n");
            response.getWriter().println(sb.toString());
        }

        response.getWriter().println(
                "       </table>" +
                "   </BODY>\n" +
                "</HTML>");
        response.flushBuffer();
    }
}
