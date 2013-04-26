<%@ page import="java.io.*" %>
<%@ page import="com.betfair.site.model.PropertiesReader" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sql_rt" uri="http://java.sun.com/jstl/sql_rt" %>
<%--
  ~ Copyright (c) 2012 The Sporting Exchange Limited
  ~
  ~ All rights reserved.
  ~
  ~ Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
  ~
  ~ 1.	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  ~ 2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
  ~ 3.	Neither the names of The Sporting Exchange Limited, Betfair Limited nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
  ~
  ~ THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  --%>

<html>
<head>
    <%!
        public String getDescription(File file) throws FileNotFoundException, IOException {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String text = "";
            String line;

            while((line = br.readLine()) != null){
                text = text + line;
            }
            in.close();


            String[] desc = text.split("description=");
            return desc[1];
        }
    %>
    <title>Mocks Web Interface</title>
    <link rel="stylesheet" type="text/css" href="resources/reset.css" />
    <link rel="stylesheet" type="text/css" href="resources/main.css" />
</head>
<body>
<div id="container">
    <div id="header">
        <h1>
            Mocks Viewer
            <c:out value="${request.getContextPath()}"/>
        </h1>
    </div>

    <div id="mocks">
        <table border="1">
            <tr>
                <th class=tableheader>UUID</th>
                <th class=tableheader>Description</th>
                <th class=tableheader></th>
                <th class=tableheader></th>
            </tr>
            <%



                PropertiesReader props = new PropertiesReader();
                File path = new File(props.getPath());
                String canonicalPath = path.getCanonicalPath();

                if(!canonicalPath.startsWith(props.getPath())){
                    throw new IOException();
                }
                File folder = new File(canonicalPath);
                File[] listOfUUIDs = folder.listFiles();
                for(int i=0; i<listOfUUIDs.length; i++){
                    //There might be files. Ignore these, only look at the directories
                    if(listOfUUIDs[i].isDirectory()){
                        File uuidPath = new File(props.getPath() + File.separator + listOfUUIDs[i].getName());

                        String uuidCanonicalPath = uuidPath.getCanonicalPath();

                        if(!uuidCanonicalPath.startsWith(props.getPath())){
                            throw new IOException();
                        }
                        File uuid = new File(uuidCanonicalPath);
                        File[] listOfMocks = uuid.listFiles();
                        for(int x=0; x<listOfMocks.length; x++){
                            //Looking for the file with the metadata in. This file begins with a .
                            if(listOfMocks[x].getName().startsWith(".")){
                                request.getSession().setAttribute(Integer.toString(x), listOfUUIDs[i].getName());
                                %>
                                <tr>
                                    <%= "<td class=uuidcolumn><a href='/Mock?uuid=" + listOfUUIDs[i].getName() + "'>" + listOfUUIDs[i].getName() + "</a></td>" %>
                                    <%= "<td class=descriptioncolumn>" + getDescription(listOfMocks[x]) + "</td>"%>
                                    <%= "<td class=downloadcolumn><a href='/DownloadAll?uuid=" + listOfUUIDs[i].getName() + "'>Download</a></td>"%>
                                    <%= "<td class=deletecolumn><a href='/Delete?uuid=" + listOfUUIDs[i].getName() + "'>Delete</a></td>"%>
                                </tr>
                                <%
                            }
                        }
                    }
                }
            %>
        </table>
    </div>
</div>
</body>
</html>