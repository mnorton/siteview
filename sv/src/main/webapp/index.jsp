<html>
<header>
	<%@page import="com.nolaria.sv.PageFramework"%>
	<%
	   String ref = request.getParameter("ref");
	   String site = request.getParameter("site");
	   PageFramework framework = new PageFramework(site, ref);
	   
	   String name = framework.getPageName();
	   name = name.substring(0, name.indexOf("."));
	   String title = framework.getPageTitle();
	%>

	<title><%= name %></title>
	<meta name="title" content="<%= name %>" />
	<meta name="name" content="<%= name %>" />
	
	<link rel="stylesheet" href="/norberg3/nolaria.css">
	<!--
	<style>
		body {
			color: black;
			font-family: Arial;
		}
	</style>
	-->

</header>
<body>

<div id="banner" style="width: 100%; height: 160px; border-style: solid; border-width: 1px; padding: 20px; background-color:#709d3e">
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
	<center>
		© 2021 Mark J. Norton, All Rights Reserved<br>
	</center>
</div>

</body>
</html>