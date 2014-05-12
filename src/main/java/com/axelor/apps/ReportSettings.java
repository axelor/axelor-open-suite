/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps;

import com.axelor.app.AppSettings;

public class ReportSettings {
	
	public static String FORMAT_PDF = "pdf";
	public static String FORMAT_XLS = "xls";
	public static String FORMAT_DOC = "doc";
	
	private static String BIRT_PATH = "birt";

	public static String REPORT_SALES_ORDER = "SalesOrder.rptdesign";
	public static String REPORT_STOCK_MOVE = "StockMove.rptdesign";

	private String url = "";
	
	public ReportSettings(String rptdesign, String format)  {
		
		this.addAxelorReportPath(rptdesign)
		.addDataBaseConnection()
		.addParam("__format", format);
		
	}
	
	public ReportSettings(String rptdesign)  {
		
		this.addAxelorReportPath(rptdesign)
		.addDataBaseConnection()
		.addParam("__format", FORMAT_PDF);
		
	}
	
	public String getUrl()  {
		
		return this.url;
		
	}	
	
	private ReportSettings addAxelorReportPath(String rptdesign)  {
		
		String appsUrl = AppSettings.get().getBaseURL();
		appsUrl = appsUrl.substring(0, appsUrl.lastIndexOf('/'));
		
		this.url +=  appsUrl + "/" + BIRT_PATH + "/frameset?__report=report/" + rptdesign;
		return this;
		
	}	
	
	private ReportSettings addDataBaseConnection()  {
		
		AppSettings appSettings = AppSettings.get();
		
		return this.addParam("DBName", appSettings.get("db.default.url"))
		.addParam("UserName", appSettings.get("db.default.user"))
		.addParam("Password", appSettings.get("db.default.password"));
		
	}	

	public ReportSettings addParam(String param, String value)  {
		
		this.url +=  "&" + param + "=" + value;
		return this;
		
	}
}

