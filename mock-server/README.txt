~~~~~~~~~Readme.txt~~~~~~~~~

The mock-server is designed to send back web-service responses when it recieves specific requests:

To see this in action:
1.  Get the service deployed on a Tomcat Server.
2.  Set up a session using a POST call to /sessions:

	curl -H 'Content-Type:application/json' -d '{"commonWhen":{"any":[{"all":[{"url":{"contains":"/api/"}},{"param":{"name":{"eq":"foo"},"value":{"eq":"1234"}}}]},{"all":[{"url":{"contains":"/www/"}},{"param":{"name":{"eq":"bar"},"value":{"eq":"1234"}}}]}]}}' http://<yourserver>/sessions>resp.html
	
3.  Look in resp.html to see the id of the session you just created.
4.  Set up an expectation in that session using a POST call to /expectations?sessionId=<your id here>

	curl -H "content-type:application/json" -d '{"when":{"url":{"endsWith":"getallnodes"}},"then":{"sequentially":[{"respond":{"code":"200","contentType": "text/html","body":"<html><body>firstCall</body></html>"}},{"respond":{"code":"200","contentType": "text/html","body":"<html><body>middleCall</body></html>"}},{"respond":{"code":"200","contentType": "text/html","body":"<html><body>lastCall</body></html>"}}]}}' http://<yourserver>/expectations?sessionId= ??? >resp.html
	
5.  Now try hitting the server with a request the server will now recognise:

	http://<yourserver>/getallnodes?foo=1234
	http://<yourserver>/getallnodes?bar=1234
	
6.  Check the state of your expectations:

	GET http://<yourserver>/expectations?sessionId=<your_id_here>
	
7.  Finally delete your session:

	curl –X DELETE http://<yourserver>/sessions?sessionId= ??? >resp.html
	
	
	
	


1.  navigate to the mock server pom and run the command:
	mvn -Prpm,rpm-deploy clean deploy -DentityExpansionLimit=200000 -Dmaven.test.failure.ignore=false
	
2.	This can create an RPM so that you can install it on a linux system with the command:
	rpm -i 
	
	
	
To deploy on a Tomcat Server manually:	
1.  get mock-server.war onto the box 
2.  /sbin/service tomcat7 stop
3.  rm -r /usr/share/tomcat7/webapps/ROOT
4.  rm /usr/share/tomcat7/webapps/ROOT.war
5.  mv mock-server.war /usr/share/tomcat7/webapps/ROOT.war
6.  /sbin/service tomcat7 start
	

	