<html>
<head>
	<%@page import="com.nolaria.sv.PageIdFramework"%>
	<%
	   PageIdFramework framework = new PageIdFramework(request);
	   
	   //String name = "UNKNOWN-NAME";
	   String title = "UNKNOWN-TITLE";
	   if (framework.page != null)
	       title = framework.page.getTitle();
	%>
	<link rel="stylesheet" href="http://localhost:8080/nolaria/green.css">
	<title><%= title %></title>
	<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
	<meta http-equiv="Pragma" content="no-cache" />
	<meta http-equiv="Expires" content="0" />
	<meta name="title" content="<%= title %>" />
	

</head>
<body>

<div id="banner" style="width: 100%; height: 180px; border-style: solid; border-width: 1px; padding: 20px; background-color:#709d3e">
	<%= framework.getBanner()  %>
</div>

<div id="middle" style="width: 100%; display: grid; grid-template-columns: 30% 70%">
	<!-- Navigation is in the left column.  -->
	<div id="navigation" style="padding:20px; border-style: solid; border-right-width: 1px; border-left-width: 1px">
		<%= framework.getNavigation() %>
	</div>

	<!-- Content is in the right column.  -->
	<div id="content" style="padding:20px; border-style: solid; border-right-width: 1px">
		<%= framework.getContent()  %>
	</div>
	
</div>

<div id="footer" style="height: 40px; width: 100%; border-style: solid; border-width: 1px; padding:20px; background-color:#709d3e">
	<span style="text-align: center"> © 2021 Mark J. Norton, All Rights Reserved<br></span>
</div>

</body>
</html>