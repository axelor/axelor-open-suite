package com.axelor.apps;

import java.io.InputStream;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;

import com.axelor.db.JPA;

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
			properties.put("axelor.jdbc.password", emf.getProperties().get("javax.persistence.jdbc.password"));
			
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