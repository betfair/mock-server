<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.betfair.site.model.FileData" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
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
    <title>Individual Mock Page</title>
    <script src="resources/jquery.js" type="text/javascript"></script>
    <link rel="stylesheet" type="text/css" href="resources/reset.css"/>
    <link rel="stylesheet" type="text/css" href="resources/main.css"/>
    <link rel="stylesheet" type="text/css" href="resources/mockPage.css"/>
    <script type=text/javascript>

        var currentRow = -1;

        function textChanged(){
            document.getElementById('submitbutton').disabled = false;
        }


        function getFilename(row){
            var cells = row.getElementsByTagName('td');
            var rows = document.getElementsByTagName('tr');

            //Set the elements in the form to the data in the selected row
            for (var i = 0; i < cells.length; i++) {
                document.getElementById('filename').innerHTML = cells[0].innerHTML;
                document.getElementById('filename2').value = cells[0].innerHTML;
                document.getElementById('creationdate').value = cells[1].innerHTML;
                document.getElementById('lasteditted').value = cells[2].innerHTML;
                document.getElementById('lastused').value = cells[3].innerHTML;
                document.getElementById('recorded').value = cells[4].innerHTML;
            }

            //This ensures the currently selected row is unselected
            for (var x=0; x < rows.length; x++){
                rows[x].className = "showData";
            }

            //Adds the class, so it is highlighted
            row.className = row.className + " selectedRow";

            //Adds the file contents to the textarea
            document.getElementById("data").value = row.getAttribute("data-attr");

            currentRow = row.id;
            document.getElementById("submitbutton").disabled = true;
        }

        $(document).keydown(function(e){

            if($(document.activeElement).attr("name") == "data"){
                if(e.keyCode == 8 || e.keyCode == 46){
                    document.getElementById("submitbutton").disabled = false;
                }
            }

            if($(document.activeElement).attr("name") != "data"){

                var numOfRows = document.getElementById('filestable').getElementsByTagName('tr').length -1;
                var rowChanged = false;

                if(currentRow != -1){

                    var row = document.getElementById(currentRow);
                    if(currentRow != 0){
                        if (e.keyCode == 38) {
                            //Up pressed
                            rowChanged = true;
                            row.className = "showData";
                            var prevRow = document.getElementById(currentRow-1);
                            prevRow.className = prevRow.className + " selectedRow";
                            currentRow = currentRow-1;
                        }
                    }

                    if(currentRow != numOfRows -1){
                        if (e.keyCode == 40){
                            //Down pressed
                            rowChanged = true;
                            row.className = "showData";
                            var nextRow = document.getElementById(currentRow*1+1);
                            nextRow.className = nextRow.className + " selectedRow";
                            currentRow = currentRow*1+1;
                        }
                    }

                    if(rowChanged == true){
                        var selectedRow = document.getElementById(currentRow)
                        var cells = selectedRow.getElementsByTagName('td');
                        //Set the elements in the form to the data in the selected row
                        for (var i = 0; i < cells.length; i++) {
                            document.getElementById('filename').innerHTML = cells[0].innerHTML;
                            document.getElementById('filename2').value = cells[0].innerHTML;
                            document.getElementById('creationdate').value = cells[1].innerHTML;
                            document.getElementById('lasteditted').value = cells[2].innerHTML;
                            document.getElementById('lastused').value = cells[3].innerHTML;
                            document.getElementById('recorded').value = cells[4].innerHTML;
                        }
                        //Adds the file contents to the textarea
                        document.getElementById("data").value = row.getAttribute("data-attr");
                        document.getElementById("submitbutton").disabled = false;
                    }
                }
            }
        })


    </script>

    <%
        List<FileData> list = (ArrayList<FileData>)request.getSession().getAttribute("fileData");
        application.setAttribute("list", list);
    %>
</head>
<body>
    <%
        request.getSession().setAttribute("refresh", "true");
    %>
<div id="container">
    <div id="header">
        <h1>
            Mock:
            <c:out value=' ${request.getSession().getAttribute("mockID")}'/>
        </h1>
    </div>
    <div id=innerContainer>
        <div id=description>
            <h2>Description: </h2><br><c:out value='${ request.getSession().getAttribute("description")}'/>
        </div>
        <div id=files>
            <table id=filestable border=1>
                <tr>
                    <th class=tableheader>Filename</th>
                    <th class=tableheader>Creation Date</th>
                    <th class=tableheader>Last Edited</th>
                    <th class=tableheader>Last Used</th>
                    <th class=tableheader>Recorded</th>
                    <th class=tableheader></th>
                </tr>
                <%
                    for(int i=0; i<list.size(); i++){

                %>

                    <%= "<tr id='"+i+"' onclick='getFilename(this)' class=showData data-attr='" + list.get(i).getFileContent() + "'>"%>

                    <%= "<td class=filenamecolumn>"+list.get(i).getFilename()+"</td>"%>
                    <%= "<td class=creationdatecolumn>"+list.get(i).getCreationDate()+"</td>"%>
                    <%= "<td class=lasteditedcolumn>"+list.get(i).getLastEdited()+"</td>"%>
                    <%= "<td class=lastusedcolumn>"+list.get(i).getLastUsed()+"</td>"%>
                    <%= "<td class=recordedcolumn>"+list.get(i).getRecorded()+"</td>"%>

                <c:out value="<td class=downloadcolumn><a href='/Download?uuid=${request.getSession().getAttribute('mockID')} &filename=${ list.get(i).getFilename() }'>Download</a></td>"/>
                <%= "</tr>"%>
                <%
                    }
                %>
            </table>
        </div>
    </div>
    <div id=fileContent>
        <form action=UpdateFile method=POST>
            <span name=filename id=filename ></span><br />
            <textarea name=data id="data" rows=15 cols=80 onkeypress="textChanged()" >Click on a row in the column to view the file contents here</textarea><br />
            <input type=hidden id=filename2 name=filename2 />
            <c:out value= "<input type='hidden' id='uuid' name='uuid' value='${request.getSession().getAttribute('mockID')}' />"/>
            <input type=hidden id=creationdate name=creationdate />
            <input type=hidden id=lasteditted name=lasteditted />
            <input type=hidden id=lastused name=lastused />
            <input type=hidden id=recorded name=recorded />
            <input type=submit value="Save changes" disabled=true name=submitbutton id=submitbutton />
        </form>
    </div>
</div>
</body>
</html>