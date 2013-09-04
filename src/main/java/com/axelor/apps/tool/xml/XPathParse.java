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
package com.axelor.apps.tool.xml;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XPathParse {

	private static final Logger LOG = LoggerFactory.getLogger(XPathParse.class);
	
	private Document doc;

	protected Document getDoc() {
		return doc;
	}

	protected void setDoc(Document doc) {
		this.doc = doc;
	}

	public XPathParse(String xml) {
		
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder;
		
		try {
			builder = domFactory.newDocumentBuilder();
			this.doc = builder.parse(xml);
			
		} catch (Exception e) {
			
			LOG.error(e.getMessage());
		}
	}

	/**
	 * public static TreeMap<String,String> parse(String xml, ArrayList<String>
	 * xpeList) throws ParserConfigurationException, SAXException, IOException,
	 * XPathExpressionException {
	 *
	 * Ref: http://www.ibm.com/developerworks/library/x-javaxpathapi/index.html
	 * HashMap replaced by TreeMap since it is sorted
	 * Exceptions catched here since no way to catch them in aml
	 * 
	 */
	public Map<String, String> parse(List<String> xpeList) {
		
		Map<String, String> dict = new TreeMap<String, String>();

		try {

			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			for (String xpe : xpeList) {
				
				XPathExpression expr = xpath.compile(xpe); // /text()

				Object result = expr.evaluate(this.doc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				
				if (nodes.getLength() == 1) {
					
					dict.put(xpe, nodes.item(0).getNodeValue());
					
				} else {
					
					for (int i = 0; i < nodes.getLength(); i++) {
						
						dict.put(i + "__" + xpe, nodes.item(i).getNodeValue());
						
					}

				}
			}
		} catch (Exception e) {
			LOG.error("some pb occurred during xml scan");
		}

		return dict;
	}
}