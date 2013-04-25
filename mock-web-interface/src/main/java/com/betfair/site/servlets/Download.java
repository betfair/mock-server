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

import com.betfair.site.model.DataRetriever;
import com.betfair.site.model.FileData;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 06/12/12
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class Download extends HttpServlet {
    final private String sessionFilePath = "/var/lib/bf-mock-server/sessions/";
    final private String uuidRegex = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
    final private String uuidFileRegex = "\\.[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
    final private String filenameRegex = "[0-9]{1,5}";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        req.getSession().setAttribute("mockID", req.getParameter("uuid"));
        req.getSession().setAttribute("description", req.getParameter("description"));
        DataRetriever dataRetriever = new DataRetriever();
        File[] listOfFiles = dataRetriever.getFiles(req.getParameter("uuid"));
        getData(listOfFiles, req, res);
        req.getSession().setAttribute("files", listOfFiles);
        downloadFile(req.getParameter("filename"), req.getParameter("uuid"), req, res);
    }



    public void getData(File[] listOfFiles, HttpServletRequest req, HttpServletResponse res) throws FileNotFoundException, IOException {
        DataRetriever dataRetriever = new DataRetriever();
        List<FileData> fileDataList = new ArrayList<FileData>();

        for(int i=0; i<listOfFiles.length; i++){
            if(listOfFiles[i].getName().startsWith(".")){
                continue;
            } else {
                String filename = listOfFiles[i].getName();
                String creationDate = dataRetriever.getCreationDate(listOfFiles[i]);
                String lastEdited = dataRetriever.getLastEdited(listOfFiles[i]);
                String lastUsed = dataRetriever.getLastUsed(listOfFiles[i]);
                String recorded = dataRetriever.getRecorded(listOfFiles[i]);
                String fileContent = dataRetriever.getFileContent(listOfFiles[i]);
                FileData fileData = new FileData(filename, creationDate, lastEdited, lastUsed, recorded, fileContent);
                fileDataList.add(fileData);
            }
        }
        req.getSession().setAttribute("fileData", fileDataList);
    }


   private void downloadFile(String filename, String uuid, HttpServletRequest req, HttpServletResponse res) throws FileNotFoundException, IOException{
       if(!uuid.matches(uuidRegex) || (!filename.matches(filenameRegex) || !filename.matches(uuidFileRegex))){
          throw new FileNotFoundException("Invalid Folder/File name");
       }

       String path = sessionFilePath + uuid + "/" + filename;
        //String path = "C:/etc/mock-server/sessions/" + uuid + "/" + filename;


        File filePath = new File(path);
        String canonicalPath = filePath.getCanonicalPath();
        if(!canonicalPath.startsWith(sessionFilePath)){
            throw new IOException();
        }
        File file = new File(canonicalPath);

        int length = 0;
        ServletOutputStream outStream = res.getOutputStream();
        ServletContext context = getServletConfig().getServletContext();
        String mimeType = context.getMimeType(path);

        //Sets response content type
        if(mimeType == null){
            mimeType = "application/octet-stream";
        }
        res.setContentType(mimeType);
        res.setContentLength((int)file.length());
        //String filename2 = (new File(path)).getName();

        //Sets HTTP header
        res.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        byte[] byteBuffer = new byte[4096];
        DataInputStream in = new DataInputStream(new FileInputStream(file));

        //reads the file's bytes and writes them to the response stream
        while ((in != null) && ((length = in.read(byteBuffer)) != -1)){
            outStream.write(byteBuffer,0,length);
        }

        in.close();
        outStream.close();
    }
}
