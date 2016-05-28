package com.adobe.servlets;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "ErrorServlet",
        urlPatterns = {"/error.html"}
)
public class ErrorServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ErrorServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String error_message = (String)request.getAttribute("error_message");

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


        response.getWriter().println("<p>Whoops! Something went wrong :(</p>" +
                "<p><Strong>" + error_message + "</strong></p>" +
                "   </BODY>\n" +
                "</HTML>");
    }
}
