/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.web.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.simple.PDFRenderer;

import com.google.inject.servlet.RequestScoped;
import com.lowagie.text.DocumentException;

@RequestScoped
@Path("/htmlToPdf")
public class HtmlToPdf {
	
	@GET
	public File htmlToPdf(
			@QueryParam("html") String html,
			@QueryParam("fileName") String fileName,
			@QueryParam("printPageNo") String printPageNo){
		try {
			InputStream io = this.getClass().getClassLoader().getResourceAsStream("css/studio.css");
			StringWriter writer = new StringWriter();
			IOUtils.copy(io, writer, "utf-8");
			
			String css = writer.toString();
			html = "<div class=\"content\">" + html + "</div>";
			if(printPageNo != null){
				html = "<div class=\"pageno\"> <span id=\"pagenumber\"></span> / <span id=\"pagecount\"></span></div>" +  html;
			}
			html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><style type=\"text/css\">" + css + "</style></head><body>" + html + "</body></html>";
			
			html = formatHtml(html);
			File htmlfile = File.createTempFile(fileName, ".html");
			FileWriterWithEncoding fw = new FileWriterWithEncoding(htmlfile, "utf-8");
			fw.write(html);
			fw.close();
			
			File pdfFile = File.createTempFile(fileName, "");
			PDFRenderer.renderToPDF(htmlfile, pdfFile.getAbsolutePath());
			return pdfFile;
		} catch (IOException | DocumentException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String formatHtml(String html){
		
		Tidy tidy = new Tidy();
		tidy.setXHTML(true);
		tidy.setShowWarnings(false);
		tidy.setTidyMark(false);
		tidy.setDocType("omit");
		tidy.setCharEncoding(Configuration.UTF8);
		ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tidy.parse(in, out);
		
		html = new String(out.toByteArray());
		html = html.replace("&nbsp;"," ");
		
		return html;
	}

}
