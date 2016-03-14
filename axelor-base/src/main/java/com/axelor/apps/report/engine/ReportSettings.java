/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.beust.jcommander.internal.Maps;

public class ReportSettings {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static String FORMAT_PDF = "pdf";
	public static String FORMAT_XLS = "xls";
	public static String FORMAT_DOC = "doc";
	public static String FORMAT_HTML = "html";
	
	protected Map<String, Object> params = Maps.newHashMap();
	
	protected String format = FORMAT_PDF;
	protected String rptdesign;
	protected String outputName;
	protected Model model;
	protected String fileName;
	protected File output;
	
	
	public ReportSettings(String rptdesign, String outputName)  {
		
		this.rptdesign = rptdesign;
		this.computeOutputName(outputName);
		addDataBaseConnection();
		addAttachmentPath();
		
	}
	

	public ReportSettings generate() throws AxelorException  {
		
		this.computeFileName();

        
        return this;
        
	}
	
	public String getFileLink()  {
		
		if(output == null)  {  return null;  }
		
		String fileLink = String.format("ws/files/report/%s?name=%s", output.getName(), fileName);
		
		logger.debug("URL : {}", fileLink);
		
		return fileLink;
		
	}
	
	public File getFile()  {
		
		return output;
		
	}
	
	protected void attach() throws FileNotFoundException, IOException  {
		
		if (model != null && model.getId() != null && output != null) {
			try (InputStream is = new FileInputStream(output)) {
				Beans.get(MetaFiles.class).attach(is, fileName, model);
			}
		}
		
	}
	
	
	protected void computeOutputName(String outputName)  {
		
		this.outputName = outputName
							.replace("${date}", new DateTime().toString("yyyyMMdd"))
							.replace("${time}", new DateTime().toString("HHmmss"));
		
	}
	
	protected void computeFileName()  {
		
		this.fileName = String.format("%s.%s", outputName, format);
		
	}
	
	public ReportSettings addFormat(String format)  {
		
		if(format != null)  {
			this.format = format;
		}
		
		return this;
		
	}
	
	public ReportSettings addModel(Model model)  {
		
		this.model = model;
		
		return this;
		
	}

	public ReportSettings addParam(String param, Object value)  {
		
		this.params.put(param, value);
		
		return this;
		
	}
	
	protected ReportSettings addDataBaseConnection()  {
		
		AppSettings appSettings = AppSettings.get();
		
		return this.addParam("DefaultDriver", appSettings.get("db.default.driver"))
		.addParam("DBName", appSettings.get("db.default.url"))
		.addParam("UserName", appSettings.get("db.default.user"))
		.addParam("Password", appSettings.get("db.default.password"));
		
	}
	
	private ReportSettings addAttachmentPath(){
		
		String attachmentPath = AppSettings.get().getPath("file.upload.dir","");
		if(attachmentPath == null){
			return this;
		}
		
		attachmentPath = attachmentPath.endsWith(File.separator) ? attachmentPath : attachmentPath+File.separator;
		
		return this.addParam("AttachmentPath",attachmentPath);
		
	}
	
	public static boolean useIntegratedEngine()  {
		
		AppSettings appsSettings = AppSettings.get();
		
		String useIntegratedEngine = appsSettings.get("axelor.report.use.embedded.engine", "true");
		
		if(useIntegratedEngine.equals("true"))  {  return true;  }
		return false;
		
	}

}

