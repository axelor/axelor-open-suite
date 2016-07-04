package com.axelor.studio.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.validation.ValidationException;

import org.apache.xmlbeans.impl.common.JarHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.StudioConfiguration;
import com.axelor.studio.db.repo.StudioConfigurationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class use to build application and restart tomcat server.
 * 
 * @author axelor
 *
 */
public class UpdateAppService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private String logText;

	@Inject
	private StudioConfigurationRepository configRepo;

	/**
	 * Method call process to build application.
	 * 
	 * @param moduleRecorder
	 *            ModuleRecorder record containing reference to build directory
	 *            and AxelorHome path..
	 * @return String array with first element as '0' if success and '-1' for
	 *         error. Second element is log from build process.
	 */
	public String[] buildApp() {

		logText = "";

		try {
			AppSettings settings = AppSettings.get();
			String buildDir = checkParams("Build directory",
					settings.get("build.dir"), true);
			String axelorHome = checkParams("Axelor home",
					settings.get("axelor.home"), true);
			File buildDirFile = new File(buildDir);

			StudioConfiguration config = configRepo.all().fetchOne();
			ProcessBuilder processBuilder = null;
			if (config != null) {
				String buildCmd = config.getBuildCmd();
				if (buildCmd != null) {
					processBuilder = new ProcessBuilder(buildCmd.split(" "));
				}
			}
			if (processBuilder == null) {
				processBuilder = new ProcessBuilder("./gradlew", "clean", "-x",
						"test", "build");
			}
			processBuilder.directory(buildDirFile);
			processBuilder.environment().put("AXELOR_HOME", axelorHome);

			Process process = processBuilder.start();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				logText = logText + line + "\n";
			}

			process.waitFor();

			Integer exitStatus = process.exitValue();
			
			log.debug("Exit status: {}, Log text: {}", exitStatus, logText);

			if (exitStatus != 0) {
				return new String[] { "-1", logText };
			}
			
		} catch (ValidationException | IOException | InterruptedException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			logText = sw.toString();
			return new String[] { "-1", logText };
		}

		return new String[] { "0", logText };
	}

	/**
	 * Method call update application on given tomcat webapp path
	 * 
	 * @param moduleRecorder
	 *            Configuration record.
	 */
	public String updateApp(boolean reset) {

		try {
			AppSettings settings = AppSettings.get();
			String buildDirPath = checkParams("Build directory",
					settings.get("build.dir"), true);
			String webappPath = checkParams("Tomcat webapp server path",
					settings.get("tomcat.webapp"), true);

			File warDir = new File(buildDirPath + File.separator + "build",
					"libs");
			log.debug("War directory path: {}", warDir.getAbsolutePath());
			if (!warDir.exists()) {
				return I18n
						.get("Error in application build. No build directory found");
			}
			File webappDir = new File(webappPath);
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
				File appDir = new File(webappDir, appName);
				if (!appDir.exists()) {
					appDir.mkdir();
				}
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
		
		if (reset) {
			return I18n.get("App reset successfully");
		}
		
		return I18n.get("App updated successfully");
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
	
	@Transactional
	public void clearDatabase() {
		
		JPA.em().createNativeQuery("drop schema public cascade").executeUpdate();
		JPA.em().createNativeQuery("create schema public").executeUpdate();
		
	}

}
