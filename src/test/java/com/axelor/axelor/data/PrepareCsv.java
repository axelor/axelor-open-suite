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
package com.axelor.axelor.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.axelor.apps.tool.file.CsvTool;
import com.google.inject.Inject;

public class PrepareCsv {
	
	@Inject
	CsvTool cTool;

	@Test
	public void prepareCsv(){
		String xmlDir = System.getProperty("xmlDir");
		String csvDir = System.getProperty("csvDir");
		List<String> ignoreType = Arrays.asList("many-to-one","one-to-one","many-to-many","one-to-many");
		try{
			if(xmlDir != null && csvDir != null){
				File xDir = new File(xmlDir);
				File cDir = new File(csvDir);
				List<String[]> blankData = new ArrayList<String[]>();
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				if(xDir.isDirectory() && cDir.isDirectory()){
					for(File xf : xDir.listFiles()){
						List<String> fieldList = new ArrayList<String>();
						Document doc = dBuilder.parse(xf);
						NodeList nList = doc.getElementsByTagName("entity");
						if(nList != null){
							NodeList fields = nList.item(0).getChildNodes();
							Integer count = 0;
							String csvFileName = xf.getName().replace(".xml", ".csv");
							while(count < fields.getLength()){
								Node field = fields.item(count);
								NamedNodeMap attrs = field.getAttributes();
								String type = field.getNodeName();
								if(attrs != null && attrs.getNamedItem("name") != null && !ignoreType.contains(type))
									fieldList.add(attrs.getNamedItem("name").getNodeValue());
								count++;
							}
							cTool.csvWriter(csvDir, csvFileName, ';',StringUtils.join(fieldList,",").split(","), blankData);
							System.out.println("INFO# CSV file prepared: "+csvFileName);
						}
					}
					
				}
				else 
					System.out.println("ERROR: XML and CSV paths must be directory");
			}
			else
				System.out.println("ERROR: Please input XML and CSV directory path");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
