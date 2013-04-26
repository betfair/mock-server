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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 11/12/12
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class DownloadAll extends HttpServlet {

    final private String uuidRegex = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        PropertiesReader props = new PropertiesReader();
        List<String> fileList;
        fileList = new ArrayList<String>();
        String uuid = req.getParameter("uuid");
        if(!uuid.matches(uuidRegex)){
            throw new FileNotFoundException("Invalid path");
        }
        String path = fileList + uuid;
        String zipFile = props.getPath() + File.separator + uuid + ".zip";

        //If file doesnt exist, create the zip file
        File filePath = new File(zipFile);
        String canonicalPath = filePath.getCanonicalPath();
        if(!canonicalPath.startsWith(props.getPath())){
            throw new IOException();
        }
        File f = new File(canonicalPath);

        if(!f.exists()){
            File folder = new File(path);
            generateFileList(folder,fileList);
            zipIt(path, zipFile,fileList);
        }

        //Download the file
        downloadZip(zipFile, req, res);
    }


    private void generateFileList(File node, List<String> fileList){
        //add file only
        if(node.isFile()){
            fileList.add(node.getName());
        }

        if(node.isDirectory()){
            String[] files = node.list();
            for(String filename : files){
                generateFileList(new File(node, filename), fileList);
            }
        }
    }


    private void zipIt(String sourceFolder, String zipFile, List<String> fileList){

        byte[] buffer = new byte[1024];

        try{

            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            System.out.println("Output to Zip : " + zipFile);

            for(String file : fileList){
                System.out.println("File Added : " + file);
                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in =
                        new FileInputStream(sourceFolder + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            zos.close();

            System.out.println("Done");
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }


    public void downloadZip(String path, HttpServletRequest req, HttpServletResponse res) throws IOException {
        File filePath = new File(path);
        String canonicalPath = filePath.getCanonicalPath();
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

        //Sets HTTP header
        res.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

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
