/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps;

import java.io.File;

import com.axelor.app.AppSettings;

public class ReportSettings {
	
	public static String FORMAT_PDF = "pdf";
	public static String FORMAT_XLS = "xls";
	public static String FORMAT_DOC = "doc";
	public static String FORMAT_HTML = "html";
	
	private static String BIRT_PATH = "birt";

	private String url = "";
	
	public ReportSettings(String rptdesign, String format)  {
		
		this.addAxelorReportPath(rptdesign)
		.addDataBaseConnection()
		.addAttachmentPath()
		.addParam("__format", format);
		
	}
	
	public ReportSettings(String rptdesign)  {
		
		this.addAxelorReportPath(rptdesign)
		.addDataBaseConnection()
		.addAttachmentPath()
		.addParam("__format", FORMAT_PDF);
		
	}
	
	public String getUrl()  {
		
		return this.url;
		
	}	
	
	private ReportSettings addAxelorReportPath(String rptdesign)  {
		
		AppSettings appsSettings = AppSettings.get();
		
		String defaultUrl = appsSettings.getBaseURL();
		defaultUrl = defaultUrl.substring(0, defaultUrl.lastIndexOf('/'));
		defaultUrl += "/" + BIRT_PATH ;
		
		String appsUrl = appsSettings.get("axelor.report.engine", defaultUrl);
		
		String resourcePath = appsSettings.get("axelor.report.resource.path", "report");
		resourcePath = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";
		
		this.url +=  appsUrl + "/frameset?__report=" + resourcePath + rptdesign;
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
	
	public ReportSettings addAttachmentPath(){
		
		String attachmentPath = AppSettings.get().getPath("file.upload.dir","");
		if(attachmentPath == null){
			return this;
		}
		
		attachmentPath = attachmentPath.endsWith(File.separator) ? attachmentPath : attachmentPath+File.separator;
		
		return this.addParam("AttachmentPath",attachmentPath);
		
	}
}

