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

/**
 * Created with IntelliJ IDEA.
 * User: BrumfittC
 * Date: 06/12/12
 * Time: 11:54
 * To change this template use File | Settings | File Templates.
 */
public class FileData implements Comparable{

    private String filename;
    private String creationDate;
    private String lastEdited;
    private String lastUsed;
    private String recorded;
    private String fileContent;

    public FileData(String filename, String creationDate, String lastEdited, String lastUsed, String recorded, String fileContent){
        this.filename = filename;
        this.creationDate = creationDate;
        this.lastEdited = lastEdited;
        this.lastUsed = lastUsed;
        this.recorded = recorded;
        this.fileContent = fileContent;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getLastEdited(){
        return lastEdited;
    }

    public void setLastEdited(String lastEdited){
        this.lastEdited = lastEdited;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(String lastUsed) {
        this.lastUsed = lastUsed;
    }

    public String getRecorded() {
        return recorded;
    }

    public void setRecorded(String recorded) {
        this.recorded = recorded;
    }

    public String getFileContent(){
        return fileContent;
    }

    public void setFileContent(String fileContent){
        this.fileContent = fileContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileData)) return false;

        FileData fileData = (FileData) o;

        if (creationDate != null ? !creationDate.equals(fileData.creationDate) : fileData.creationDate != null)
            return false;
        if (fileContent != null ? !fileContent.equals(fileData.fileContent) : fileData.fileContent != null)
            return false;
        if (filename != null ? !filename.equals(fileData.filename) : fileData.filename != null) return false;
        if (lastEdited != null ? !lastEdited.equals(fileData.lastEdited) : fileData.lastEdited != null) return false;
        if (lastUsed != null ? !lastUsed.equals(fileData.lastUsed) : fileData.lastUsed != null) return false;
        if (recorded != null ? !recorded.equals(fileData.recorded) : fileData.recorded != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (lastEdited != null ? lastEdited.hashCode() : 0);
        result = 31 * result + (lastUsed != null ? lastUsed.hashCode() : 0);
        result = 31 * result + (recorded != null ? recorded.hashCode() : 0);
        result = 31 * result + (fileContent != null ? fileContent.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if (this == o) return 0;
        if (!(o instanceof FileData)) return 1;
        FileData fileData = (FileData) o;

        //-1 not a valid filename
        Integer thisName = -1;
        Integer thatName = -1;

        //try to filenames into numbers
        try{
            thisName = Integer.parseInt(filename);
        }catch(NumberFormatException e){}
        try{
            thatName = Integer.parseInt(fileData.filename);
        }catch(NumberFormatException e){}

        //both filenames are numbers, use integer comparison
        if(thisName != -1 && thatName != -1){
            return thisName.compareTo(thatName);
        }
        //this name is string, then order this below
        else if(thisName == -1){
            return -1;
        }
        //other name is string, then order this above
        else if(thatName == -1){
            return 1;
        }
        //if both are not numbers, use string comparison
        else{
            return filename.compareTo(fileData.filename);
        }


    }
}
