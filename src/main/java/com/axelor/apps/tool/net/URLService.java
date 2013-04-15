package com.axelor.apps.tool.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.tool.file.FileTool;

public final class URLService {
	
	final static int size = 1024;
	
	private static final Logger LOG = LoggerFactory.getLogger(URLService.class);
	
	private URLService(){
		
	}
	
	/**
	 * Test la validité d'une url.
	 * 
	 * @param url
	 * 		L'URL à tester.
	 * 
	 * @return
	 */
	public static String notExist(String url){
		
		try{
			URL fileURL = new URL(url);
			fileURL.openConnection().connect();
			return null;
		}
		catch(java.net.MalformedURLException ex)
		{
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
		
			LOG.error(sw.toString());
			return "Problème de format de l'URL";
		}
		catch(java.io.IOException ex)
		{
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			
			LOG.error(sw.toString());
			return "Ce document n'existe pas";
		}
		
	}
	 
	
	public static void fileUrl(String fAddress, String localFileName, String destinationDir) throws IOException {
		OutputStream outStream = null;
		URLConnection uCon = null;

		InputStream is = null;
		File file = FileTool.create(destinationDir, localFileName);

		URL Url;
		byte[] buf;
		int ByteRead, ByteWritten = 0;
		Url = new URL(fAddress);
		outStream = new BufferedOutputStream(new FileOutputStream(file));
		uCon = Url.openConnection();

		is = uCon.getInputStream();
		buf = new byte[size];
		while ((ByteRead = is.read(buf)) != -1) {
			outStream.write(buf, 0, ByteRead);
			ByteWritten += ByteRead;
		}
		LOG.info("Downloaded Successfully.");
		LOG.debug("File name:\"" + localFileName
				+ "\"\nNo ofbytes :" + ByteWritten);
	
		if(is != null)  {
			is.close();
		}
		if(outStream != null)  {
			outStream.close();
		}
		
	}

	public static void fileDownload(String fAddress, String destinationDir, String fileName) throws IOException {

		int slashIndex = fAddress.lastIndexOf('/');
		int periodIndex = fAddress.lastIndexOf('.');
		LOG.debug("fAddress = {}" , fAddress);
		LOG.debug("destinationDir = {}" , destinationDir);
		LOG.debug("fileName = {}" , fileName);
	//	String fileName = fAddress.substring(slashIndex + 1);

		if (periodIndex >= 1 && slashIndex >= 0
				&& slashIndex < fAddress.length() - 1) {
			fileUrl(fAddress, fileName, destinationDir);
		} else {
			if(LOG.isErrorEnabled())
			LOG.error("path or file name.");
		}
	}
	
}