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

package com.betfair.utils.jsonClient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines an API for creating, setting up expectations and destroying a session. The APIs takes path to a
 * file that contains JSON parsed by the mock server.  Application
 * should set some parameters that's uniquely be able to identify the session, eg by setting up an X_UUID header
 * with a unique string.
 */
public class MockServerDataSetup {
    public static final String FILE_NAME_PREFIX = "[{{";
    public static final String FILE_NAME_SUFFIX = "}}]";
    DefaultHttpClient httpClient = new DefaultHttpClient();

    static final String MOCK_SERVER_BASE_URL = "";//You should set this from the properties
    static final String SESSION_URL = MOCK_SERVER_BASE_URL + "sessions";
    static final String EXPECTATIONS_URL = MOCK_SERVER_BASE_URL + "expectations?sessionId=";
    static final String UUID_REPLACE_KEYWORD = "[{RANDOM_UUID_STRING}]";

    private static final Logger LOG = Logger.getLogger("MockServerDataSetup");

    /**
     * This method creates a session in the mock server.
     * @param sessionDetailsFilePath Path to the file containing session information
     * @param uuid A random uuid that's uniquely identified across sessions. Use the getRandomString() to generate a
     * random uuid and reuse it across expectations for a test. Replaces occurrence of string [{RANDOM_UUID_STRING}] in
     * the file with random number. If [{RANDOM_UUID_STRING}] is not present, value of uuid is ignored.
     * @return Id of the session created in mock server. This will be used in subsequent calls to set up expectations
     * and to remove the session.
    */
    public String createSession(String sessionDetailsFilePath, String uuid) {
        String strSessionId = null;
        String sessionDetails = readFileContents(sessionDetailsFilePath);
        sessionDetails = addUUIDString(sessionDetails, uuid);
        strSessionId = postAndGetContents(SESSION_URL, sessionDetails);
/*
        To extract the sessionId from the response. Have to change the mock server to return a valid url
        from session post
*/

        String[] split = strSessionId.split("=");
        LOG.log(Level.INFO, "[MockServerDataSetup] Created a session on mock server with id : " + split[1]);
        return split[1];
    }

    /**
     * Sets up expectations for a particular request, various options are available for setting up expectation for eg.,
     * can set up different expectations based on headers, cookies etc. More information can be found in confluence page.
     * "body" section of the expectation can be the contents of the response from mock server, or a path to a file name.
     * If file name is used instead of contents of the response, it has to be within [{{ .. }}] placeholder so that it
     * is replaced when setting up the expectation. A sample body is given below. Since the body uses xml file in this
     * example, the content-type is replaced automatically with application/xml. If a json file is used, it will be
     * replaced with application/json. Please refer to mockserver/basicexpectation/sampleExpectations.json file for a template
     *
     * Example:
            "contentType": "[{{CONTENT_TYPE_PLACEHOLDER}}]",
            "body":"[{{mockserver/basicexpectation/expectationFileContents.xml}}]",
     * The value of contentType has to be in format "[{{CONTENT_TYPE_PLACEHOLDER}}]" to be automatically replaced with
     * application/xml or application/json
     * @param expectationFilePath Path to the file containing expectation
     * @param sessionId ID of the session, if it doesn't exist it can be created by calling the createSession(...)
     * @param uuid A random uuid that's uniquely identified across sessions. Use the getRandomString() to generate a
     * random uuid and reuse it across expectations for a test. Replaces occurrence of string [{RANDOM_UUID_STRING}] in
     * the file with random number. If [{RANDOM_UUID_STRING}] is not present, value of uuid is ignored.
     */
    public void setupExpectations(String expectationFilePath, String sessionId, String uuid) {
        if (sessionId == null) {
            LOG.log(Level.SEVERE, "Invalid session id passed, please obtain a session calling createSession()  ");
        }
        String expectationUrl = EXPECTATIONS_URL + sessionId;
        String expectationFileContents = readFileContents(expectationFilePath);
        expectationFileContents = addUUIDString(expectationFileContents, uuid);
        expectationFileContents = replaceFileNameWithContents(expectationFileContents);
        postAndGetContents(expectationUrl, expectationFileContents);
        LOG.log(Level.INFO, "[MockServerDataSetup] Setup expectation for session : " + sessionId);
    }

    /**
    * Call this method to remove the session created in mock server. Don't forget to call this method to remove the
    * session, else expectations will be stored in the server using up memory even if its not going to be used later
    * @param sessionId ID of the session to be removed from mock server
    */
    public void removeSession(String sessionId) {
        if (sessionId == null) {
            LOG.log(Level.SEVERE, "Invalid session id passed, please obtain a session calling createSession()  ");
        }
        deleteMethod(SESSION_URL + "?sessionId=" + sessionId);
        LOG.log(Level.INFO, "[MockServerDataSetup] Removed session : " + sessionId);
    }

/**
     * Generate a 10 digit random string that can be used to set a unique uuid header in session
     * @return A 10 digit random alphanumeric string
*/

    public String getRandomString() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    String replaceFileNameWithContents(String originalContents) {
        StringBuilder response = new StringBuilder();
        int startPos = originalContents.lastIndexOf(FILE_NAME_PREFIX);
        int endPos = originalContents.lastIndexOf(FILE_NAME_SUFFIX);
        if (startPos == -1 || endPos == -1) {
            return originalContents;
        }
        String fileName = originalContents.substring(startPos + 3, endPos);
        String responseContents = readFileContents(fileName);

        // Replaces file contents into the response by removing the file path from expectation
        response.append(originalContents.substring(0, startPos));
        responseContents = replaceSpecialCharacters(responseContents);
        response.append(responseContents);
        response.append(originalContents.substring(endPos + 3, originalContents.length()));
        return replaceContentTypePlaceHolder(response.toString(), fileName);
    }

    private String replaceContentTypePlaceHolder(String originalContents, String fileName) {
        String contents = null;
        if (fileName.toLowerCase().endsWith("xml")) {
            contents = originalContents.replace("[{{CONTENT_TYPE_PLACEHOLDER}}]", "application/xml");
        } else if (fileName.toLowerCase().endsWith("json")) {
            contents = originalContents.replace("[{{CONTENT_TYPE_PLACEHOLDER}}]", "application/json");
        }
        return contents;
    }

    private String replaceSpecialCharacters(String responseContents) {
        responseContents = responseContents.replaceAll("\r\n", "");
        responseContents = responseContents.replaceAll("\n", "");
        responseContents = responseContents.replaceAll("\t", "");
        responseContents = responseContents.replaceAll("    ", "");
        // Escapes quotes "
        responseContents = responseContents.replaceAll("\\\"", "\\\\\"");
        return responseContents;
    }

    String readFileContents(String sessionDetailsFilePath) {
        String fileContents = null;
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(sessionDetailsFilePath);
            fileContents = IOUtils.toString(is);
        } catch (NullPointerException e) {
            LOG.log(Level.SEVERE, "Please check the path and name of the file " + sessionDetailsFilePath, e);
        }
        catch (IOException e) {
            LOG.log(Level.SEVERE, "An error occurred while reading file contents ", e);
        }
        return fileContents;
    }

    String addUUIDString(String fileContents, String uuid) {
        return fileContents.replace(UUID_REPLACE_KEYWORD, uuid);
    }

    String postAndGetContents(String url, String postMethodBody) {
        String response = "";
        HttpPost httpPost = new HttpPost(url);
        try {
            StringEntity entity = new StringEntity(postMethodBody);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            response = IOUtils.toString(httpResponse.getEntity().getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    void deleteMethod(String url) {
        HttpDelete httpDelete = new HttpDelete(url);
        try {
            httpClient.execute(httpDelete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}