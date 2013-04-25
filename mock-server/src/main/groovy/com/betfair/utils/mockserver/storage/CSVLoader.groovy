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



package com.betfair.utils.mockserver.storage

import com.betfair.utils.mockserver.playback.RecordedRequest
import org.springframework.beans.factory.annotation.Value

/**
 * Created with IntelliJ IDEA.
 * User: wambeekm
 * Date: 03/12/12
 * Time: 14:13
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
class CSVLoader extends Loader{
    private String fileName = BASE_DIR + File.separator + "sessionCall_"
    @Value('${csvSeparator}')private final String DEFAULT_SEPARATOR
    private static CSVLoader instance

    public CSVLoader(){

    }

    @Override
    public List<RecordedRequest> load(String uuid){
        File sessionFile = new File(fileName + uuid + ".csv")
        if(!sessionFile.exists()){
            throw new FileNotFoundException("Cannot find file:\n\tid= $uuid\n\tlocation= $fileName${uuid}.csv")
        }

        return parse(sessionFile)
    }

    /**
     * Parses the session file and loads the service interactions as RecordedObjects
     * @param file The Session file
     * @return A list of RecordedObjects
     */
    private List<RecordedRequest> parse(File file){
        List<RecordedRequest> interactions = new ArrayList<RecordedRequest>()
        FileInputStream fis = new FileInputStream(file)
        def reader = new BufferedReader(new InputStreamReader(fis))

        //read the seperator of the csv file if exists, otherwise use default character specified in instance variables
        String line = reader.readLine()
        String separator
        if(line && (line =~ /sep=./) ){
            separator = line.replace("sep=", "")
        }
        else{
            separator = DEFAULT_SEPARATOR
        }

        //reset reader at beginning of file
        fis.getChannel().position(0);
        reader = new BufferedReader(new InputStreamReader(fis))
        line = reader.readLine()

        //while line is not null save requests and responses as RecordedObjects
        while(line){
            RecordedRequest interaction;
            String[] csv = line.split(separator)
            if (csv[0].toUpperCase() == "REQ"){
                interaction = new RecordedRequest()
                interaction.load(csv)

                //get response for request if exists otherwise setup empty response
                csv = (line = reader.readLine()) ? line.split(separator) : null
                if(csv && csv[0] == "RSP"){
                    interaction.response = csv
                }
                else{//response to request was null, continue loop in case line is another REQ
                    interaction.response = ["RSP","[]"]
                    continue
                }
                interactions.add(interaction)
            }

            line = reader.readLine()
        }

        reader.close()
        return interactions
    }

    //TODO
    @Override
    String findUUID(String recordingName) {
        return null
    }

}
