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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.validation.ValidationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.xmlbeans.impl.common.JarHelper;
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
		
		updateModuleRecorder(recorder, null, false);
		
		if (recorder.getUpdateServer()) {
			return updateApp(false);
		}
		
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
		
		return updateApp(true);
	}
	
	/**
	 * Method call process to build application.
	 * 
	 * @param moduleRecorder
	 *            ModuleRecorder record containing reference to build directory
	 *            and AxelorHome path..
	 * @return String array with first element as '0' if success and '-1' for
	 *         error. Second element is log from build process.
	 */
	public boolean buildApp(ModuleRecorder moduleRecorder) {

		String logText = "";
		boolean build = true;
		try {
			AppSettings settings = AppSettings.get();
			String buildDirPath = checkParams("Source directory",
					settings.get("studio.source.dir"), true);
			
			log.debug("JAVA HOME: {}", System.getProperty("java.home"));
			String script = "gradlew";
			File buildDir = new File(buildDirPath);
			if (SystemUtils.IS_OS_WINDOWS) {
				script = "gradlew.bat";
			}
			String scriptPath = new File(buildDir, script).getAbsolutePath();
			log.debug("Script path: {}", scriptPath);
			ProcessBuilder processBuilder = new ProcessBuilder(scriptPath, "clean", "-x",
					"test", "build");
			
			String adkDir = settings.get("studio.adk.dir");
			if (!Strings.isNullOrEmpty(adkDir)) {
				File adk = new File(adkDir);
				if (adk.exists()) {
					String axelorHome = getAxelorHome(adk);
					if (axelorHome != null) {
						processBuilder.environment().put("AXELOR_HOME", axelorHome);
					}
				}
			}
			processBuilder.redirectErrorStream(true);
			processBuilder.directory(buildDir);

			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				log.debug("Build : {}", line);
				logText +=  line + "\n";
			}

			process.waitFor();

			Integer exitStatus = process.exitValue();
			
			if (exitStatus != 0) {
				build =  false;
			}
			
		} catch (ValidationException | IOException | InterruptedException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logText = sw.toString();
			build =  false;
		}
		
		updateModuleRecorder(moduleRecorder, logText, build);
		
		return build;
	}

	/**
	 * Method call update application on given tomcat webapp path
	 * 
	 * @param moduleRecorder
	 *            Configuration record.
	 * @throws AxelorException 
	 * @throws InterruptedException 
	 */
	public String updateApp(boolean reset) throws AxelorException{
		
		try {
			AppSettings settings = AppSettings.get();
			String sourceDir = checkParams("Source directory",
					settings.get("studio.source.dir"), true);
			
			String webappDir = checkParams("Weapp directory",
					settings.get("studio.webapp.dir"), true);

			File warDir = new File(sourceDir + File.separator + "build",
					"libs");
			log.debug("War directory path: {}", warDir.getAbsolutePath());
			if (!warDir.exists()) {
				return I18n
						.get("Error in application build. No build directory found");
			}
			File warFile = null;
			for (File file : warDir.listFiles()) {
				if (file.getName().endsWith(".war")) {
					warFile = file;
					break;
				}
			}

			if (warFile == null) {
				return I18n
						.get("Error in application build. No war file generated.");
			} else {
				String appName = warFile.getName();
				appName = appName.substring(0, appName.length() - 4);
				File appDir = new File(webappDir);
				if (appDir.exists()) {
					FileUtils.deleteDirectory(appDir);
				}
				appDir.mkdir();
				log.debug("Webapp app directory: {}", appDir.getAbsolutePath());
				log.debug("War file: {}", warFile.getAbsolutePath());
				JarHelper jarHelper = new JarHelper();
				jarHelper.unjarDir(warFile, appDir);
			}
			
		} catch (ValidationException | IOException e) {
			e.printStackTrace();
			String msg = I18n.get("Error in update, please check the log.");
			if (reset) {
				msg = I18n.get("Error in reset, please check the log.");
			}
			return msg + e.getMessage();
		}
		
		String msg = I18n.get("App updated successfully");
		if (reset) {
			msg = I18n.get("App reset successfully");
			restartServer(reset);
		}
		
		return msg;
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
			throws ValidationException {

		if (param == null) {
			throw new ValidationException(
					I18n.get("Required parameter is empty: ") + name);
		}

		if (isFile) {
			if (!(new File(param)).exists()) {
				throw new ValidationException(I18n.get("Path not exist: ")
						+ param);
			}
		}

		return param;

	}
	
	private void restartServer(boolean reset) throws AxelorException {
		
		String tomcatPath = AppSettings.get().get("studio.catalina.home");
		File tomcatDir = null;
		if (tomcatPath != null) {
			tomcatDir = new File(tomcatPath);
		}
		
		if (tomcatDir == null || !tomcatDir.exists()) {
			throw new AxelorException(I18n.get("Tomcat server directory not exist"),1);
		}
		
		try {
			String ext = "sh";
			if (SystemUtils.IS_OS_WINDOWS) {
				ext = "bat";
			}
			InputStream stream = this.getClass().getResourceAsStream("/script/RestartServer." + ext);
			File script = File.createTempFile("RestartServer", "." + ext);
			FileOutputStream out = new FileOutputStream(script);
			
			IOUtils.copy(stream, out);
			ProcessBuilder processBuilder = null;
			if (reset) {
				String dbUrl = AppSettings.get().get("db.default.url");
				String database = dbUrl.substring(dbUrl.lastIndexOf("/") + 1);
				processBuilder = new ProcessBuilder(script.getAbsolutePath(), 
						tomcatDir.getAbsolutePath(),
						database,
						AppSettings.get().get("db.default.user"),
						AppSettings.get().get("db.default.password"));
			}
			else {
				processBuilder = new ProcessBuilder(script.getAbsolutePath(), 
						tomcatDir.getAbsolutePath());
			}
			
			processBuilder.start();
		} catch (IOException e) {
			throw new AxelorException(e, 5);
		}
		
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
	
	private String getAxelorHome(File adkDir) throws IOException, InterruptedException {
		
		File axelorHome = FileUtils.getFile(adkDir, "build", "install", "axelor-development-kit");
		
		if (!axelorHome.exists()) {
			String script = "gradlew";
			if (SystemUtils.IS_OS_WINDOWS) {
				script = "gradlew.bat";
			}
			
			String scriptPath = new File(adkDir, script).getAbsolutePath();
			log.debug("ADK build script {}", scriptPath);
			
			ProcessBuilder builder = new ProcessBuilder(scriptPath, "clean", "installDist");
			builder.directory(adkDir);
			Process process = builder.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			
			String line = "";
			while ((line = reader.readLine()) != null) {
				log.debug("Build ADK : {}", line);
			}
			
			process.waitFor();
			
		}
		
		if (axelorHome.exists()) {
			return axelorHome.getAbsolutePath();
		}
		
		return null;
	
	}
}
