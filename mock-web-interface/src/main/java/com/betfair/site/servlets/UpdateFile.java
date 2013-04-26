/*
 * Copyright (c) 2012 The Sporting Exchange Limited
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1.	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.	Neither the names of The Sporting Exchange Limited, Betfair Limited nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.betfair.site.servlets;

import com.betfair.site.model.PropertiesReader;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 06/12/12
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class UpdateFile extends HttpServlet {

    final private String uuidRegex = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
    final private String uuidFileRegex = "\\.[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
    final private String filenameRegex = "[0-9]{1,5}";

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PropertiesReader props = new PropertiesReader();

        //The data to be written to the file
        String data = (String)req.getParameter("data");
        //The filename
        String filename = (String)req.getParameter("filename2");
        //The UUID
        String uuid = (String)req.getParameter("uuid");
        //The path of the file being editted
        if(!uuid.matches(uuidRegex) || (!filename.matches(filenameRegex) || !filename.matches(uuidFileRegex))){
           throw new FileNotFoundException("Invalid Folder/File name");
        }
        String path = props.getPath() + File.separator + uuid + File.separator + filename;
        //String path = "C:/etc/mock-server/sessions/" + uuid + "/" + filename;

        String creationDate = (String)req.getParameter("creationdate");
        String lastEditted = (String)req.getParameter("lasteditted");
        String lastUsed = (String)req.getParameter("lastused");
        String recorded = (String)req.getParameter("recorded");

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = new Date();
        //The current date/time, for the last editted
        String currentTime = dateFormat.format(date);

        File filePath = new File(path);
        String canonicalPath = filePath.getCanonicalPath();
        if(!canonicalPath.startsWith(props.getPath())){
            throw new IOException();
        }
        File file = new File(canonicalPath);

        PrintWriter writer = new PrintWriter(file);
        writer.println("creationDate=" + creationDate);
        writer.println("lastEdited=" + currentTime);

        if(lastUsed.equals("Mock not used yet")){
            writer.println("lastUsed=");
        } else {
            writer.println("lastUsed=" + lastUsed);
        }

        writer.println("lastUsed+" + lastUsed);
        writer.println("recorded=MANUAL");
        writer.println("");
        writer.print(data);
        writer.close();
        res.sendRedirect("Mock?uuid="+uuid);
    }

}
