package com.axelor.apps.tool.xml;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DateToXML {

	private static final Logger LOG = LoggerFactory.getLogger(DateToXML.class);

	public static XMLGregorianCalendar convert(LocalDateTime in) {
		
		XMLGregorianCalendar date = null;
		
		try {
			
			date = DatatypeFactory.newInstance().newXMLGregorianCalendar(in.toString());
			
		} catch (DatatypeConfigurationException e) {
			
			LOG.error(e.getMessage());
			
		}
		
		return date;
	}
}
