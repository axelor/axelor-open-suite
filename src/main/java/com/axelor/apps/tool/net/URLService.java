/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
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
 * Software distributed under the License is distributed on an “AS IS”
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.tool.net;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.tool.file.FileTool;
import com.google.common.base.Strings;

public final class URLService {
	
	final static int size = 1024;
	
	private static final Logger LOG = LoggerFactory.getLogger(URLService.class);
	
	/**
	 * Test la validité d'une url.
	 * 
	 * @param url
	 * 		L'URL à tester.
	 * 
	 * @return
	 */
	public static String notExist(String url) {

		if(Strings.isNullOrEmpty(url)) {
			return "Can not opening the connection to a empty URL.";
		}

		try {
			URL fileURL = new URL(url);
			fileURL.openConnection().connect();
			return null;
		}
		catch(java.net.MalformedURLException ex) {
			ex.printStackTrace();
			return "Url " + url + " is malformed.";
		}
		catch(java.io.IOException ex) {
			ex.printStackTrace();
			return "An error occurs while opening the connection. Please verify the following URL : " + url;
		}
		
	}
	 
	
	public static void fileUrl(String fAddress, String localFileName, String destinationDir) throws IOException {
		int ByteRead, ByteWritten = 0;
		byte[] buf = new byte[size];
		
		URL Url = new URL(fAddress);
		File file = FileTool.create(destinationDir, localFileName);
		OutputStream outStream = new BufferedOutputStream(new FileOutputStream(file));
		URLConnection uCon = Url.openConnection();
		InputStream is = uCon.getInputStream();
		
		while ((ByteRead = is.read(buf)) != -1) {
			outStream.write(buf, 0, ByteRead);
			ByteWritten += ByteRead;
		}
		
		LOG.info("Downloaded Successfully.");
		LOG.debug("No of bytes :" + ByteWritten);
	
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

		if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fAddress.length() - 1) {
			LOG.debug("Downloading file {} from {} to {}", fileName, fAddress, destinationDir);
			fileUrl(fAddress, fileName, destinationDir);
		} 
		else {
			LOG.error("Destination path or filename is not well formatted.");
		}
	}
	
}