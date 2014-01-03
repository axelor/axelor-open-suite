/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

import com.axelor.db.JPA;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

@Singleton
public class AxelorSettings {

	private Properties properties;

	private static AxelorSettings INSTANCE;

	@Inject
	private AxelorSettings() {
			
		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("axelor.properties");

		if (is == null) {
			throw new RuntimeException(
					"Unable to locate application configuration file.");
		}

		properties = new Properties();
		
		try {
			
			EntityManagerFactory emf = JPA.em().getEntityManagerFactory();
			
			properties.load(is);
			properties.put("axelor.jdbc.url", emf.getProperties().get("javax.persistence.jdbc.url"));
			properties.put("axelor.jdbc.user", emf.getProperties().get("javax.persistence.jdbc.user"));
			InputStream res = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/persistence.xml");
			String text = CharStreams.toString(new InputStreamReader(res, Charsets.UTF_8));
			Pattern pat = Pattern.compile("<property\\s*name=\"javax.persistence.jdbc.password\"\\s*value=\"(.*?)\"\\s*/>", Pattern.CASE_INSENSITIVE);
			Matcher mat = pat.matcher(text);
			while (mat.find()) {
				 properties.put("axelor.jdbc.password", mat.group(1));
			}
			
			String dataSource = String.format("&DBName=%s&UserName=%s&Password=%s",
					properties.get("axelor.jdbc.url"), properties.get("axelor.jdbc.user"), properties.get("axelor.jdbc.password"));
			
			properties.put("axelor.report.engine.datasource",dataSource);
			
			
		} catch (Exception e) {
			throw new RuntimeException("Error reading application configuration.");
		}
	}

	public static AxelorSettings get() {
		if (INSTANCE == null)
			INSTANCE = new AxelorSettings();
		return INSTANCE;
	}

	public String get(String key) {
		return properties.getProperty(key);
	}

	public String get(String key, String defaultValue) {
		String value = properties.getProperty(key, defaultValue);
		if (value == null || "".equals(value.trim()))  {
			return defaultValue;
		}
		return value;
	}
	
	public int getInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(get(key).toString());
		} catch (Exception e){}
		return defaultValue;
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		try {
			return Boolean.parseBoolean(get(key).toString());
		} catch (Exception e){}
		return defaultValue;
	}
	
	public void putAll(Properties properties) {
		this.properties.putAll(properties);
	}
	
	public Properties getProperties() {
		return properties;
	}
}