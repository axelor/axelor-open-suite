<%--

    Axelor Business Solutions

    Copyright (C) 2012-2015 Axelor (<http://axelor.com>).

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

<%@ page import="com.axelor.i18n.I18n" %>
<%@ page import="com.axelor.app.AppSettings" %>

<%
AppSettings settings = AppSettings.get();

String appName = settings.get("application.name", "My App");
String appLogo = settings.get("application.logo", "img/axelor-logo.png");
String appHome = settings.get("application.home", "");
%>

<header class="header">
	<div class="navbar navbar-inverse navbar-fixed-top">
		<div class="navbar-inner">
			<div class="container-fluid">
				<% if (appLogo == null || "".equals(appLogo)) { %>
				<a class="brand" href="<%=appHome%>"><%=appName%></a>
				<% } else { %>
				<a class="brand-logo" href="<%=appHome%>"> <img src="<%=appLogo%>"></a>
				<% } %>
			</div>
		</div>
	</div>
</header>
