<%--

    Axelor Business Solutions

    Copyright (C) 2012-2014 Axelor (<http://axelor.com>).

    This program is free software: you can redistribute it and/or  modify
    it under the terms of the GNU Affero General Public License, version 3,
    as published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" session="true"%>
<%@ page import="com.axelor.i18n.I18n"%>
<%@ page import="com.axelor.app.AppSettings"%>
<%@ page import="com.axelor.web.internal.AppInfo"%>
<%@ page contentType="text/html; charset=UTF-8"%>
<%
	AppSettings settings = AppSettings.get();

	String appVersion = settings.get("application.version", null);
	String appDesc = settings.get("application.description", null);
	String appTheme = settings.get("application.theme", null);

	String appTitle = settings.get("application.name", "My App");
	if (appDesc != null) {
		appTitle = appTitle + " :: " + appDesc;
	}
	String loginTitle = I18n.get("Please sign in");
	String loginRemember = I18n.get("Remember me");
	String loginSubmit = I18n.get("Log in");

	String loginUserName = I18n.get("User name");
	String loginPassword = I18n.get("Password");

	String loginHeader = "login-header.jsp";
	if (pageContext.getServletContext().getResource(loginHeader) == null) {
		loginHeader = null;
	}
%>
<!DOCTYPE html>
<html>

<head>
	<meta charset="utf-8">
	<title><%=appTitle%></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
	<meta name="description" content="<%=appDesc%>">
	<meta name="author" content="{{app.author}}">
	<%
		if (appTheme != null) {
	%>
	<link href="css/<%=appTheme%>/theme.css" rel="stylesheet">
	<%
		}
	%>
	<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
	<!--[if lt IE 9]>
			    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
			  <![endif]-->
	
	<!-- Le fav and touch icons -->
	<link rel="shortcut icon" href="ico/favicon.ico">
	
    <style type="text/css">
      body { padding-top: 60px; }
    </style>
	<link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet">
	<link href="lib/font-awesome/css/font-awesome.css" rel="stylesheet">
	<link href="css/view.form.panels.css" rel="stylesheet">
	<link href="css/colors.css" rel="stylesheet">
	<link href="css/login.css" rel="stylesheet">
	<script type="text/javascript" src="lib/jquery.ui/js/jquery.js"></script>
	<script type="text/javascript" src="lib/bootstrap/js/bootstrap.js"></script>

<body>

	<%
		if (loginHeader != null) {
	%>
	<jsp:include page="<%=loginHeader%>" />
	<%
		}
	%>

	<div class="container-fluid">
		<div class="row-fluid">
			<div class="panel login-panel span4 offset4">
				<div class="panel-header panel-default">
					<span class="panel-title"><%=loginTitle%></span>
				</div>
				<div class="panel-body">
					<form id="login-form" action="" method="POST">
						<label for="usernameId"><%=loginUserName%></label> 
						<input type="text" class="input-block-level" id="usernameId" name="username"> 
						<label for="passwordId"><%=loginPassword%></label>
						<input type="password" class="input-block-level" id="passwordId" name="password">
						<label class="checkbox">
							<input type="checkbox" value="rememberMe" name="rememberMe"><%=loginRemember%>
						</label>
						<button class="btn btn-primary" type="submit"><%=loginSubmit%></button>
					</form>
				</div>
			</div>
		</div>
	</div>

	<footer class="container-fluid">
		<p class="credit small">Version <%=appVersion%></p>
		<p class="credit small">
			&copy; 2014 <a href="http://www.axelor.com">Axelor</a>. All Rights
			Reserved.
		</p>
	</footer>
</body>
</html>
