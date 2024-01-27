<html>
<head>
	<%@page import="com.nolaria.dbedit.DbEditFramework"%>
	<%
		DbEditFramework framework = new DbEditFramework(request);
	   
	   	//String name = "UNKNOWN-NAME";
	   	String title = "BD Record Editor";
	   	//if (framework.page != null)
	    //   title = framework.page.getTitle();
	%>
	<link rel="stylesheet" href="http://localhost:8080/nolaria/green.css">
	<title><%= title %></title>
	
	<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Expires" content="0" />
	<meta name="title" content="<%= title %>" />
</head>
<body>

<div id="header" style="height: 80px; width: 100%; border-style: solid; border-width: 1px; padding:20px; background-color:#709d3e">
	<center>
		<h1>Page Registry Record Editor</h1>
	</center>
</div>


<div id="body" style="width: 100%; padding: 20px">
	<%= framework.getBody()  %>
</div>

<div id="footer" style="height: 40px; width: 100%; border-style: solid; border-width: 1px; padding:20px; background-color:#709d3e">
	<center>
		© 2024 Mark J. Norton, All Rights Reserved<br>
	</center>
</div>

</body>
</html>