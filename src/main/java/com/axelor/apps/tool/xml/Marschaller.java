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
package com.axelor.apps.tool.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.axelor.apps.tool.file.FileTool;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public final class Marschaller {
	
	private Marschaller(){
		
	}
	
	public static void marschalOutputStream(Object jaxbElement, String context) throws JAXBException{
		
		JAXBContext jaxbContext = JAXBContext.newInstance(context);
		marschalOutputStream(jaxbElement, jaxbContext);
		
	}
	
	public static void marschalOutputStream(Object jaxbElement, JAXBContext jaxbContext) throws JAXBException{
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(jaxbElement, System.out);
		
	}
	
	public static void marschal(Object jaxbElement, String context, StringWriter writer) throws JAXBException{
		
		JAXBContext jaxbContext = JAXBContext.newInstance(context);
		marschal(jaxbElement, jaxbContext, writer);
		
	}
	
	public static void marschal(Object jaxbElement, JAXBContext jaxbContext, StringWriter sw) throws JAXBException{
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		sw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?> \n");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CustomNamespacePrefixMapper());
		marshaller.marshal(jaxbElement,sw);
		
	}
	
	public static void marschalFile(Object jaxbElement, String context, String destinationFolder, String fileName) throws JAXBException, IOException{
		
		JAXBContext jaxbContext = JAXBContext.newInstance(context);
		marschalFile(jaxbElement, jaxbContext, destinationFolder, fileName);
		
	}
	
	public static void marschalFile(Object jaxbElement, JAXBContext jaxbContext, String destinationFolder, String fileName) throws JAXBException, IOException{
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(jaxbElement, FileTool.create(destinationFolder, fileName));
		
	}
	
	public static Object unmarschalFile(String context, String data) throws JAXBException{
		
		JAXBContext jc = JAXBContext.newInstance(context);
		
		return unmarschalFile(jc,data);
		
	}
	
	public static Object unmarschalFile(JAXBContext jaxbContext, String data) throws JAXBException{
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StringReader reader = new StringReader(data);
		
		return unmarshaller.unmarshal(reader);
		
	}
	
	/**
	 * Coerces the JAXB marshaller to declare the "xsi" and "xsd" namespaces at the root element
	 * instead of putting them inline on each element that uses one of the namespaces.
	 */
	private static class CustomNamespacePrefixMapper extends NamespacePrefixMapper {

	    @Override
	    public String getPreferredPrefix(String namespaceUri, String suggestion, 
	                    boolean requirePrefix) {
	        return suggestion;
	    }

	    @Override
	    public String[] getPreDeclaredNamespaceUris2() {
	        return new String[]{
	                "xsi",
	                "http://www.w3.org/2001/XMLSchema-instance",
	                "xsd",
	                "http://www.w3.org/2001/XMLSchema"
	        };

	    }
	}
	
}
