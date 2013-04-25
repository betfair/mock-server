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
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 06/12/12
 * Time: 15:47
 * To change this template use File | Settings | File Templates.
 */
public class Delete extends HttpServlet {
    final private String filePath = "/var/lib/bf-mock-server/sessions/";
    final private String uuidRegex = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        deleteUUID(req, res);
    }


    public void deleteUUID(HttpServletRequest req, HttpServletResponse res) throws IOException{
        String uuid = req.getParameter("uuid");
        if(!uuid.matches(uuidRegex)){
            res.sendRedirect("index.jsp");
        }

        //String path = "C:/etc/mock-server/sessions/" + uuid;
        File file = new File(filePath + uuid);
        String canonicalPath = file.getCanonicalPath();
        if(!canonicalPath.startsWith(filePath)){
            throw new IOException();
        }

        File dir = new File(canonicalPath);
        if(dir.listFiles().length>0){
            for(File f : dir.listFiles()){
                f.delete();
            }
        }
        dir.delete();
        res.sendRedirect("index.jsp");

    }
}
