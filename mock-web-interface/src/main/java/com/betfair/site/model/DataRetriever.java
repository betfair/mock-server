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

package com.betfair.site.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 06/12/12
 * Time: 09:51
 * To change this template use File | Settings | File Templates.
 */
public class DataRetriever {

    final private String uuidRegex = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
    PropertiesReader props;

    public DataRetriever() throws IOException{
        props = new PropertiesReader();
    }
    /**
     *
     * Returns the mock description, from the meta data file
     * @param uuid
     * @return
     * @throws java.io.FileNotFoundException
     */
    public String getDescription(String uuid) throws FileNotFoundException, IOException {
        if(!uuid.matches(uuidRegex)){
           return "";
        }
        File path = new File(props.getPath() + File.separator + uuid);

        String canonicalPath = path.getCanonicalPath();
        File folder = new File(canonicalPath);
        if(!canonicalPath.startsWith(props.getPath())){
            throw new IOException();
        }


        for(File file : folder.listFiles()){
            if(file.getName().startsWith(".")){
                FileInputStream fstream = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String text = "";
                String line;

                try{
                    while((line = br.readLine()) != null){
                        text = text + line;
                    }
                    in.close();
                } catch (IOException e){
                    in.close();
                    e.printStackTrace();
                }

                String[] desc = text.split("description=");
                return desc[1];
            }
        }
        return "";
    }




    public File[] getFiles(String uuid) throws IOException {
        if(!uuid.matches(uuidRegex)){
           return null;
        }
        File path = new File(props.getPath() + File.separator + uuid);

        String canonicalPath = path.getCanonicalPath();
        System.out.println("Canonical is : " + canonicalPath);
        if(!canonicalPath.startsWith(props.getPath())){
            throw new IOException();
        }

        File folder = new File(canonicalPath);
        File[] listOfFiles = folder.listFiles();
        return listOfFiles;
    }


    public String getCreationDate(File file) throws FileNotFoundException, IOException {
        HashMap allData = fileReader(file);
        String date = allData.get("creationDate").toString();
        return date;
    }

    public String getLastEdited(File file) throws FileNotFoundException, IOException {
        HashMap allData = fileReader(file);
        String date = allData.get("lastEdited").toString();
        return date;
    }

    public String getLastUsed(File file) throws FileNotFoundException, IOException {
        HashMap allData = fileReader(file);
        String date = allData.get("lastUsed").toString();
        return date;
    }

    public String getRecorded(File file) throws FileNotFoundException, IOException {
        HashMap allData = fileReader(file);
        String recorded = allData.get("recorded").toString();
        return recorded;
    }


    public String getFileContent(File file) throws FileNotFoundException, IOException {
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String text = "";
        String line;

        boolean firstLine = true;
        while((line = br.readLine()) != null){
            if(!(line.startsWith("REQUEST"))){
                if(firstLine){
                    continue;
                }
            }
            if(firstLine){
                text = line;
                firstLine = false;
            } else {
                text = text + "\n" + line;
            }
        }
        in.close();

        String newText = text.replaceAll("\"", "\\\"");
        return newText;
    }


    public HashMap fileReader(File file) throws FileNotFoundException, IOException {
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String text = "";
        String line;

        HashMap allData = new HashMap();

        int linesRead = 0;
        while((line = br.readLine()) != null && linesRead<5){
            if(line.contains("=")){
                String[] data = line.split("=");
                if(data.length==1){
                    allData.put(data[0], "Mock not used yet");
                } else {
                    allData.put(data[0], data[1]);
                }
            }
            linesRead++;
        }
        in.close();
        return allData;
    }

}
