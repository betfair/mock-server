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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 05/12/12
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */
public class Mock extends HttpServlet {


    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        DataRetriever dataRetriever = new DataRetriever();

        String uuid = req.getParameter("uuid");
        String description = dataRetriever.getDescription(req.getParameter("uuid"));

        req.getSession().setAttribute("mockID", uuid);
        req.getSession().setAttribute("description", description);

        File[] listOfFiles = dataRetriever.getFiles(uuid);
        req.getSession().setAttribute("files", listOfFiles);

        getData(listOfFiles, req, res);

        res.setContentType("text/html");
        req.getRequestDispatcher("mock.jsp").include(req, res);
    }



    public void getData(File[] listOfFiles, HttpServletRequest req, HttpServletResponse res) throws FileNotFoundException, IOException {
        DataRetriever dataRetriever = new DataRetriever();
        List<FileData> fileDataList = new ArrayList<FileData>();

        for(int i=0; i<listOfFiles.length; i++){
            if(listOfFiles[i].getName().startsWith(".") || listOfFiles[i].getName().endsWith(".zip")){
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
        Collections.sort(fileDataList);
        req.getSession().setAttribute("fileData", fileDataList);

    }




}
