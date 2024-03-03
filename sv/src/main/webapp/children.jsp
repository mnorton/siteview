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

	<!-- Content is in the right column.  -->
	<div id="content" style="padding:20px; border-style: solid; border-right-width: 1px">
		<%= framework.getChildrenContent()  %>
	</div>


<!--
	<table>
		<thead>
			<tr>
				<th>Child Pages</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>Child Page 1</td>
			</tr>
			<tr>
				<td>Child Page 2</td>
			</tr>
			<tr>
				<td>Child Page 3</td>
			</tr>
			<tr>
				<td>Child Page 4</td>
			</tr>
			<tr>
				<td>Child Page 5</td>
			</tr>
		</tbody>
	</table>
-->

</body>
</html>