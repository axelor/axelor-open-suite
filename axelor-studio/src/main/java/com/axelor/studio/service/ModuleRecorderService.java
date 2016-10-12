/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.studio.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.common.FileUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModule;
import com.axelor.studio.db.ModuleRecorder;
import com.axelor.studio.db.repo.ModuleRecorderRepository;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.builder.SelectionBuilderService;
import com.axelor.studio.service.builder.TranslationBuilderService;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.axelor.studio.service.wkf.WkfService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class use to build application and restart tomcat server.
 * 
 * @author axelor
 *
 */
public class ModuleRecorderService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private ModuleRecorderRepository moduleRecorderRepo;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private WkfService wkfService;
	
	@Inject
	private ModelBuilderService modelBuilderService;
	
	@Inject
	private ViewBuilderService viewBuilderService;
	
	@Inject
	private SelectionBuilderService selectionBuilderService;
	
	@Inject
	private TranslationBuilderService translationBuilderService;
	
	@Inject
	private CommandService commandService;
	
	public String update(ModuleRecorder recorder) throws AxelorException {
		
		String wkfProcess = wkfService.processWkfs();
		if (wkfProcess != null) {
			return I18n.get(String.format("Error in workflow processing: \n%s", wkfProcess));
		}
		
		if (recorder.getUpdateServer()) {

			modelBuilderService.build();
			
			selectionBuilderService.build();
			
			translationBuilderService.build();
			
			if (!buildApp(recorder)) {
				return I18n.get("Error in build. Please check the log");
			}
		}
		
		String viewLog = buildView(recorder);
		
		if (viewLog != null) {
			updateModuleRecorder(recorder, viewLog, true);
			return I18n.get("Error in view update. Please check the log");
		}
		
		if (recorder.getUpdateServer()) {
			return restartServer(false);
		}
		
		updateModuleRecorder(recorder, null, false);
		
		return I18n.get("Views updated successfuly");
		
	}
	
	public String reset(ModuleRecorder moduleRecorder) throws IOException, AxelorException {
		
		for (MetaModule module : configService.getCustomizedModules()) {
			File moduleDir = configService.getModuleDir(module.getName(), false);
			log.debug("Deleting directory: {}",moduleDir.getPath());
			if (moduleDir.exists()) {
				FileUtils.deleteDirectory(moduleDir);
			}
		}
		
		if (!buildApp(moduleRecorder)) {
			return I18n.get("Error in build. Please check the log");
		}
		
		return restartServer(true);
	}
	
	/**
	 * Method call process to build application.
	 * 
	 * @param moduleRecorder
	 *            ModuleRecorder record containing reference to build directory
	 *            and AxelorHome path..
	 * @return String array with first element as '0' if success and '-1' for
	 *         error. Second element is log from build process.
	 * @throws AxelorException 
	 */
	public boolean buildApp(ModuleRecorder moduleRecorder) throws AxelorException {

		String logText = null;
		boolean build = true;
		
		File sourceDir = getSourceDir();
		String script = "gradlew";
		if (SystemUtils.IS_OS_WINDOWS) {
			script = "gradlew.bat";
		}
		String scriptPath = new File(sourceDir, script).getAbsolutePath();
		log.debug("Script path: {}", scriptPath);
		
		Map<String, String> env = createEnvironment();
		
		String command = scriptPath + " -x test clean build";
		StringBuffer result = new StringBuffer();
		
		int exitStatus = commandService.execute(sourceDir, env, command, result);
		log.debug("Exit status: {}", exitStatus);
		if (exitStatus != 0) {
			build =  false;
			logText = result.toString();
		}
			
		updateModuleRecorder(moduleRecorder, logText, build);
		
		return build;
	}

	private File getSourceDir() throws AxelorException {
		
		AppSettings settings = AppSettings.get();
		String buildDirPath = checkParams("studio.source.dir",
				settings.get("studio.source.dir"), true);
		File buildDir = new File(buildDirPath);
		
		return buildDir;
	}

	private Map<String, String> createEnvironment() {
		
		Map<String, String> env = new HashMap<String, String>();
		log.debug("JAVA HOME: {}", System.getProperty("java.home"));
		env.put("JAVA_HOME", System.getProperty("java.home"));
		log.debug("JAVA Temp dir {}", System.getProperty("java.io.tmpdir"));
		env.put("GRADLE_OPTS", "-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
		String axelorHome = getAxelorHome(env);
		if (axelorHome != null) {
			env.put("AXELOR_HOME", axelorHome);
		}
		
		return env;
	}


	/**
	 * Validate parameters to check if its null or not.
	 * 
	 * @param name
	 *            Name of parameter.
	 * @param param
	 *            Value of parameter.
	 * @param isFile
	 *            Boolean to check if its file.
	 * @return Value of parameter if its not null.
	 * @throws ValidationException
	 *             Throws validation exception if parameter value is null or if
	 *             parameter is file and file not exist.
	 */
	private String checkParams(String name, String param, boolean isFile)
			throws AxelorException {

		if (param == null) {
			throw new AxelorException(I18n.get("Required parameter is empty: ") + name, 1);
		}

		if (isFile) {
			if (!(new File(param)).exists()) {
				throw new AxelorException(I18n.get("Path not exist: ") + param, 1);
			}
		}

		return param;

	}
	
	private String restartServer(boolean reset) throws AxelorException {
		
		String tomcatPath = getTomcatPath();
		File sourceDir = getSourceDir();
		String warPath = getWarPath(sourceDir);
		
		AppSettings settings = AppSettings.get();
		String webapp = checkParams("studio.webapp.dir",
				settings.get("studio.webapp.dir"), true);
		String logFile = checkParams("studio.restart.log",
				settings.get("studio.restart.log"), true);

		try {
			String scriptPath = getRestartScriptPath();
			
//			String command = scriptPath + " " + tomcatPath + " " +  webapp + " " + warPath;
//			
//			if (reset) {
//				String dbUrl = settings.get("db.default.url");
//				String database = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
//				command += database + " " + settings.get("db.default.user") + " " + settings.get("db.default.password");
// 			}
			
			//commandService.execute(sourceDir, System.getenv(), command, new StringBuffer());
			ProcessBuilder processBuilder = null;
			if (reset) {
				String dbUrl = settings.get("db.default.url");
				String database = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
				processBuilder = new ProcessBuilder(scriptPath, 
						tomcatPath, 
						webapp, 
						warPath,
						database,
						settings.get("db.default.user"),
						settings.get("db.default.password"));
			}
			else {
				processBuilder = new ProcessBuilder(scriptPath, 
						tomcatPath, 
						webapp, 
						warPath );
			}
			processBuilder.environment().putAll(System.getenv());
			processBuilder.redirectOutput(new File(logFile));
			processBuilder.start();
		} catch (IOException e) {
			throw new AxelorException(e, 5);
		}
		
		if (reset) {
			return I18n.get("App reset sucessfully");
		}
		
		return I18n.get("App updated successfully");
	}

	private String getTomcatPath() throws AxelorException {
		
		String tomcatPath = AppSettings.get().get("studio.catalina.home");
		File tomcatDir = null;
		if (tomcatPath != null) {
			tomcatDir = new File(tomcatPath);
		}
		
		if (tomcatDir == null || !tomcatDir.exists()) {
			throw new AxelorException(I18n.get("Tomcat server directory not exist"),1);
		}
		
		return tomcatDir.getAbsolutePath();
	}
	
	private String getWarPath(File sourceDir) throws AxelorException{
		
		File warDir = FileUtils.getFile(sourceDir, "build",
				"libs");
		log.debug("War directory path: {}", warDir.getAbsolutePath());
		if (!warDir.exists()) {
			throw new AxelorException(I18n
					.get("Error in application build. No build directory found"), 1) ;
		}

		for (File file : warDir.listFiles()) {
			if (file.getName().endsWith(".war")) {
				return file.getAbsolutePath();
			}
		}

		throw new AxelorException(I18n
				.get("Error in application build. No build directory found"), 1) ;
	
	}
	
	
	private String getRestartScriptPath() throws IOException, FileNotFoundException {
		
		String ext = "sh";
		if (SystemUtils.IS_OS_WINDOWS) {
			ext = "bat";
		}
		InputStream stream = this.getClass().getResourceAsStream("/script/RestartServer." + ext);
		File script = File.createTempFile("RestartServer", "." + ext);
		script.setExecutable(true);
		FileOutputStream out = new FileOutputStream(script);
		IOUtils.copy(stream, out);
		out.close();
		
		return script.getAbsolutePath();
	}

	
	@Transactional
	public void updateModuleRecorder(ModuleRecorder moduleRecorder, String logText, boolean update) {
		
		moduleRecorder = moduleRecorderRepo.find(moduleRecorder.getId());
		moduleRecorder.setLogText(logText);
		moduleRecorder.setUpdateServer(update);
		
		moduleRecorderRepo.save(moduleRecorder);
	}
	
	@Transactional
	public void setUpdateServer() {
		ModuleRecorder moduleRecorder = moduleRecorderRepo.all().fetchOne();
		
		if (moduleRecorder != null) {
			moduleRecorder.setUpdateServer(true);
			moduleRecorderRepo.save(moduleRecorder);
		}
	}
	
	private String buildView(ModuleRecorder recorder) throws AxelorException {
		
		String viewLog = null;
		for (MetaModule module : configService.getCustomizedModules()) {
			viewLog =  viewBuilderService.build(module.getName(), 
				!recorder.getUpdateServer(), recorder.getAutoCreate(), recorder.getAllViewUpdate());
			if (viewLog != null) {
				break;
			}
		}
		
		return viewLog;
		
	}
	
	private String getAxelorHome(Map<String, String> env) {
		
		String adkPath = AppSettings.get().get("studio.adk.dir");
		if (Strings.isNullOrEmpty(adkPath)) {
			return null;
		}
		File adkDir = new File(adkPath);
		if (!adkDir.exists()) {
			return null;
		}
		
		File axelorHome = FileUtils.getFile(adkDir, "build", "install", "axelor-development-kit");
		
		if (!axelorHome.exists()) {
			String script = "gradlew";
			if (SystemUtils.IS_OS_WINDOWS) {
				script = "gradlew.bat";
			}
			
			String scriptPath = new File(adkDir, script).getAbsolutePath();
			log.debug("ADK build script {}", scriptPath);
			
			String command = scriptPath + " clean installDist";
			StringBuffer result = new StringBuffer();
			int exitStatus = commandService.execute(adkDir, env, command, result);
			if (exitStatus != 0) {
				return null;
			}
			
		}
		
		if (axelorHome.exists()) {
			return axelorHome.getAbsolutePath();
		}
		
		return null;
	
	}
}
