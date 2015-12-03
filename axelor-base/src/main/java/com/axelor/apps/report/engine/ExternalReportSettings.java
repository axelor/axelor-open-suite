/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
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
package com.axelor.apps.report.engine;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.birt.core.exception.BirtException;

import com.axelor.app.AppSettings;
import com.axelor.app.internal.AppFilter;
import com.axelor.apps.tool.net.URLService;
import com.axelor.meta.MetaFiles;

public class ExternalReportSettings  extends ReportSettings  {
	
	protected static String BIRT_PATH = "birt";
	protected String url = "";

	public ExternalReportSettings(String rptdesign, String outputName)  {
		
		super(rptdesign, outputName);
		
		this.addAxelorReportPath(rptdesign)
		.addParam("__locale", AppFilter.getLocale().toString());
		
		
	}
	

	@Override
	public ExternalReportSettings generate() throws IOException, BirtException  {
		
		super.generate();
		
		this.getUrl();
		
    	String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist != null){
			
			// manage exception
			return null;
		}
		
		final Path tmpFile = MetaFiles.createTempFile(null, "");
		
		this.output = tmpFile.toFile();
				
		URLService.fileDownload(this.output, url, "", outputName);
		
		this.attach();
		
		return this;
	}
	
	public String getUrl()  {
		
		addParam("__format", format);
		
		for(String param : params.keySet())  {
			
			this.url +=  this.computeParam(param);
			
		}
		
		return this.url;
		
	}	
	
	private String computeParam(String param)  {
		
		return "&" + param + "=" + params.get(param);
		
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
	

	
}

