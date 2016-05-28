package com.adobe.servlets;

import javax.servlet.ServletException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "IndexServlet",
        urlPatterns = {"/index.html"}
)
public class IndexServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        String format = request.getParameter("format");
        String note = "Please choose the csv file which contains addresses to be verified.";
        if ("csv".equals(format)) note = "The file must be in <strong> csv </strong> format.";

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


        response.getWriter().println("<p>" + note + "</p>" +
                "       <form action=\"fileupload\" method=\"POST\" enctype=\"multipart/form-data\">\n" +
                "           <input type=\"file\" name=\"file\" file_extension=\"*.csv\">\n" +
                "           <input type=\"submit\">\n" +
                "       </form>\n" +
                "   </BODY>\n" +
                "</HTML>");
    }
}
