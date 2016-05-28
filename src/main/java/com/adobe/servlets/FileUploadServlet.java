package com.adobe.servlets;

import com.adobe.service.AddressVerifyService;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;

@WebServlet("/fileupload")
@MultipartConfig(fileSizeThreshold=1024*1024*2, // 2MB
        maxFileSize=1024*1024*100,              // 100MB
        maxRequestSize=1024*1024*100)           // 100MB
public class FileUploadServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(HttpServlet.class);
    private static final String SAVE_DIR = "uploadFiles";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /** Get absolute path of the web application */
        String appPath = request.getServletContext().getRealPath("");
        /** Construct path of the directory to save uploaded file */
        String savePath = appPath + SAVE_DIR;

        /** Create the save directory if it does not exists */
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists() || !fileSaveDir.isDirectory()) {
            fileSaveDir.mkdir();
        }

        Part part = request.getPart("file");
        String fileName = extractFileName(part);
        String saveFilePath = savePath + File.separator + fileName;

        part.write(saveFilePath);

        try {
            AddressVerifyService.verifyLoalSheet(saveFilePath);
            getServletContext().getRequestDispatcher("/result.html?fileToShow="+saveFilePath)
                    .forward(request, response);
        } catch (RuntimeException e) {
            request.setAttribute("error_message", e.getMessage());
            getServletContext() .getRequestDispatcher("/error.html")
                    .forward(request, response);
        }
    }

    /**
     * Extracts file name from HTTP header content-disposition
     */
    private String extractFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");

        String[] items = contentDisp.split(";");
        for (String s : items) {
            if (s.trim().startsWith("filename")) {
                return s.substring(s.indexOf("=") + 2, s.length()-1);
            }
        }
        return "";
    }

}
