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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MyFtp {
	
	private static final Logger LOG = LoggerFactory.getLogger(MyFtp.class);

	private MyFtp(){
		
	}
	
	public static void getDataFiles(String server, String username, String password, String folder, String destinationFolder, Calendar start, Calendar end) {
		
		try {
			
			// Connect and logon to FTP Server
			FTPClient ftp = new FTPClient();
			ftp.connect(server);
			ftp.login(username, password);

			// List the files in the directory
			ftp.changeWorkingDirectory(folder);
			FTPFile[] files = ftp.listFiles();
			
			for (int i = 0; i < files.length; i++) {
				
				Date fileDate = files[i].getTimestamp().getTime();
				if (fileDate.compareTo(start.getTime()) >= 0 && fileDate.compareTo(end.getTime()) <= 0) {
					
					// Download a file from the FTP Server
					File file = new File(destinationFolder + File.separator + files[i].getName());
					FileOutputStream fos = new FileOutputStream(file);
					ftp.retrieveFile(files[i].getName(), fos);
					fos.close();
					file.setLastModified(fileDate.getTime());
				}
			}

			// Logout from the FTP Server and disconnect
			ftp.logout();
			ftp.disconnect();

		} catch (Exception e) {

			LOG.error(e.getMessage());
			
		}
	}
	

}
