package com.axelor.apps.base.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.App;
import com.axelor.common.FileUtils;
import com.axelor.data.csv.CSVImporter;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaScanner;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class AppServiceImpl implements AppService {
	
	private final Logger log = LoggerFactory.getLogger(AppService.class);
	
	private static final String DIR_DEMO = "demo";
	
	private static final String DIR_INIT = "data-init";
	
	private static final String DIR_INIT_INPUT = "app";
	
	@Override
	public String importDataDemo(App app) {
		String modules = app.getModules();
		String type = app.getTypeSelect();
		String lang = AppSettings.get().get("application.locale");
		log.debug("App type: {}, App lang: {}", type, lang);
		if (lang == null) {
			return I18n.get("No application language set. Please set 'application.locale' property.");
		}
		
		for (String module : modules.split(",")) {
			File tmp = extract(module, DIR_DEMO);
			if (tmp == null) {
				continue;
			}
			try {
				File config = FileUtils.getFile(tmp, DIR_DEMO,  type + "-config.xml");
				if (config != null && config.exists()) {
					File data = FileUtils.getFile(config.getParentFile(), lang);
					CSVImporter importer = new CSVImporter(config.getAbsolutePath(), data.getAbsolutePath(), null);
					importer.run();
				}
			} finally {
				clean(tmp);
			}
		}
		
		app.setDemoDataLoaded(true);
		
		return I18n.get("Demo data loaded sucessefully");
	}
	
	@Override
	public void importDataInit(App app) {
		
		log.debug("Data init...");
		String modules = app.getModules();
		String type = app.getTypeSelect();
		String lang = AppSettings.get().get("application.locale");
		log.debug("App type: {}, App lang: {}", type, lang);
		if (lang == null) {
			return;
		}
		
		for (String module : modules.split(",")) {
			File tmp = extract(module, DIR_INIT);
			if (tmp == null) {
				continue;
			}
			try {
				File config = FileUtils.getFile(tmp, DIR_INIT, DIR_INIT_INPUT, type + "-config.xml");
				log.debug("Config path: {}", config.getAbsolutePath());
				if (config != null && config.exists()) {
					File data = FileUtils.getFile(config.getParentFile(), lang);
					log.debug("data path: {}", config.getAbsolutePath(), data.getAbsolutePath());
					CSVImporter importer = new CSVImporter(config.getAbsolutePath(), data.getAbsolutePath(), null);
					importer.run();
				}
			} finally {
				clean(tmp);
			}
		}
		
	}
	

	private File extract(String module, String dirName) {

		final List<URL> files = MetaScanner.findAll(module, dirName, "(.+?)");

		if (files.isEmpty()) {
			return null;
		}

		final File tmp = Files.createTempDir();

		for (URL file : files) {
			String name = file.toString();
			name = name.substring(name.lastIndexOf(dirName));
			try {
				copy(file.openStream(), tmp, name);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return tmp;
	}
	
	private void copy(InputStream in, File toDir, String name) throws IOException {
		File dst = FileUtils.getFile(toDir, name);
		Files.createParentDirs(dst);
		FileOutputStream out = new FileOutputStream(dst);
		try {
			ByteStreams.copy(in, out);
		} finally {
			out.close();
		}
	}

	private void clean(File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				clean(child);
			}
			file.delete();
		} else if (file.exists()) {
			file.delete();
		}
	}
}
