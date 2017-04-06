package com.axelor.apps.base.service.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.common.FileUtils;
import com.axelor.data.csv.CSVImporter;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaScanner;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class AppServiceImpl implements AppService {
	
	private final Logger log = LoggerFactory.getLogger(AppService.class);
	
	private static final String DIR_DEMO = "demo";
	
	private static final String DIR_INIT = "data-init";
	
	private static final String DIR_INIT_INPUT = "app";
	
	private static final String APP_TYPE_SELECT = "app.type.select";
	
	@Inject
	private AppRepository appRepo;
	
	
	@Override
	public String importDataDemo(App app) {
		app = appRepo.find(app.getId());
		
		importParentData(app);
		
		String modules = app.getModules();
		String type = app.getTypeSelect();
		String lang = AppSettings.get().get("application.locale");
		log.debug("App type: {}, App lang: {}", type, lang);
		
		if (lang == null) {
			return I18n.get("No application language set. Please set 'application.locale' property.");
		}

		for (String module : modules.split(",")) {
			log.debug("Importing module: {}", module);
			File tmp = extract(module, DIR_DEMO);
			if (tmp == null) {
				log.debug("Demo data not found");
				continue;
			}
			try {
				File config = FileUtils.getFile(tmp, DIR_DEMO,  type + "-config.xml");
				if (config != null && config.exists()) {
					File data = FileUtils.getFile(config.getParentFile(), lang);
					CSVImporter importer = new CSVImporter(config.getAbsolutePath(), data.getAbsolutePath(), null);
					importer.run();
				}
				else {
					log.debug("Config file not found");
				}
			} finally {
				clean(tmp);
			}
		}
		
		app = appRepo.find(app.getId());
		app.setDemoDataLoaded(true);
		
		saveApp(app);
		
		return I18n.get("Demo data loaded sucessefully");
	}
	
	private void importParentData(App app) {
		
		List<App> depends = getDepends(app, true);
		for (App parent : depends) {
			parent = appRepo.find(parent.getId());
			if (!parent.getDemoDataLoaded()) {
				log.debug("Importing demo data for parent app: {}", parent.getName());
				importDataDemo(parent);
			}
		}
		
	}

	@Transactional
	public App saveApp(App app) {
		return appRepo.save(app);
	}
	
	private void importDataInit(App app) {
		
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
		
		app = appRepo.find(app.getId());
		app.setInitDataLoaded(true);
		
		saveApp(app);
		
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

	@Override
	public App getApp(String type) {
		if (type == null) {
			return null;
		}
		return Beans.get(AppRepository.class).all().filter("self.typeSelect = ?1", type).fetchOne();
	}

	@Override
	public boolean isApp(String type) {
		App app = getApp(type);
		if (app == null) {
			return false;
		}
		
		return app.getActive();
	}

	@Override
	public List<App> getDepends(App app, Boolean active) {
		
		String dependsOn = app.getDependsOn();
		if (dependsOn == null) {
			return new ArrayList<App>();
		}
		
		String query = "self.typeSelect in (?1)";
		
		if (active != null) {
			query += " AND self.active = " + active;	
		}
		
		List<App> apps = appRepo.all().filter(query, Arrays.asList(dependsOn.split(","))).fetch();
		log.debug("App: {}, DependsOn: {}, Parent active: {}, Total parent founds: {}", app.getName(), dependsOn, active, apps.size());
		return sortApps(apps);
	}

	@Override
	public List<String> getNames(List<App> apps) {
		
		List<String> names = new ArrayList<String>();
		
		for (App app : apps) {
			names.add(app.getName());
		}
		
		return names;
	}

	@Override
	public List<App> getChildren(App app, Boolean active) {
		
		String type = app.getTypeSelect();
		
		String query = "self.dependsOn = ?1 "
				+ "OR self.dependsOn like ?2 "
				+ "OR self.dependsOn like ?3 "
				+ "OR self.dependsOn like ?4 ";
		
		if (active != null) {
			query = "(" + query + ") AND self.active = " + active;
		}
		List<App> apps = appRepo.all().filter(query, type, type + ",%", "%," + type + ",%",  "%," + type).fetch();
		
		log.debug("Parent app: {}, Total children: {}", app.getName(), apps.size());
		
		return apps;
	}
	
	@Override
	public App installApp(App app, Boolean importDemo) {
		List<App> apps = getDepends(app, false);
		
		for (App parentApp : apps) {
			parentApp = appRepo.find(parentApp.getId());
			installApp(parentApp, importDemo);
		}
		
		app = appRepo.find(app.getId());
		
		if (!app.getInitDataLoaded()) {
			importDataInit(app);
		}
		
		app = appRepo.find(app.getId());
		if (importDemo != null && importDemo && !app.getDemoDataLoaded()) {
			importDataDemo(app);
		}
		
		app = appRepo.find(app.getId());
		
		app.setActive(true);
		
		return saveApp(app);
	}
	
	
	@Override
	public List<App> sortApps(Collection<App> apps) {
		
		List<App> appsList = new ArrayList<App>();
		
		appsList.addAll(apps);
		
		appsList.sort(new Comparator<App>() {

			@Override
			public int compare(App app1, App app2) {
				
				Option option1 = MetaStore.getSelectionItem(APP_TYPE_SELECT, app1.getTypeSelect());
				Option option2 = MetaStore.getSelectionItem(APP_TYPE_SELECT, app2.getTypeSelect());
				
				if (option1 == null || option2 == null) {
					return 0;
				}
				
				Integer order1 = option1.getOrder();
				Integer order2 = option2.getOrder();
				
				if (order1 < order2) {
					return -1;
				}
				if (order1 > order2) {
					return 1;
				}
				
				return 0;
			}
		});
		
		log.debug("Apps sorted: {}", getNames(appsList));
		
		return appsList;
	}

}
