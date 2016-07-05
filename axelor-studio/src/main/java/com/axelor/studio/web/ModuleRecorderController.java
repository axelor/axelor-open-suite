package com.axelor.studio.web;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.common.FileUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ModuleRecorder;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.UpdateAppService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.axelor.studio.service.wkf.WkfService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ModuleRecorderController {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private ModelBuilderService modelBuilderService;

	@Inject
	private ViewBuilderService viewBuilderService;

	@Inject
	private UpdateAppService updateAppService;

	@Inject
	private MetaModelRepository metaModelRepo;

	@Inject
	private ConfigurationService configService;

	@Inject
	private WkfService wkfService;

	public void update(ActionRequest request, ActionResponse response) throws InterruptedException {
		
		if (configService == null) {
			response.setFlash("Error in configuration. Please check configuration properties");
			return;
		}

		String wkfProcess = wkfService.processWkfs();
		if (wkfProcess != null) {
			response.setFlash("Error in workflow processing: \n" + wkfProcess);
			return;
		}

		ModuleRecorder moduleRecorder = request.getContext().asType(
				ModuleRecorder.class);
		Boolean lastRunOk = moduleRecorder.getLastRunOk();

		MetaModel metaModel = metaModelRepo.all()
				.filter("self.edited = true and self.customised = true")
				.fetchOne();

		if (metaModel != null || !lastRunOk) {
			File domainDir = configService.getDomainDir();
			boolean modelRecorded = modelBuilderService.build(domainDir);

			if (!modelRecorded) {
				response.setFlash(I18n
						.get("Error in model recording. Please check the log"));
				return;
			}
			
			ResourceBundle.clearCache(Thread.currentThread().getContextClassLoader());

			String[] result = updateAppService.buildApp();
			response.setValue("logText", result[1]);
			if (!result[0].equals("0")) {
				response.setFlash(I18n
						.get("Error in build. Please check the log"));
				response.setValue("lastRunOk", false);
				return;
			}

			String errors = updateView(response, false);
			if (Strings.isNullOrEmpty(errors)) {
				response.setSignal("refresh-app", true);
				response.setFlash(updateAppService.updateApp(false));
			}
			else{
				response.setValue("logText", errors);
				response.setFlash("Erorr in view updates");
			}
		} else {
			response.setValue("logText", null);
			updateView(response, true);
		}

		response.setValue("lastRunOk", true);

	}

	private String updateView(ActionResponse response, boolean updateOnly) {

		File viewDir = configService.getViewDir();
		String errors = viewBuilderService.build(viewDir, updateOnly);

		if (errors == null) {
			if (updateOnly) {
				response.setFlash(I18n.get("Views updated successfully"));
			}
			return null;
		} else {
			response.setFlash(I18n
					.get("Error in recording. Please check the log"));
			return errors;
		}

	}

	public void checkEdited(ActionRequest request, ActionResponse response) {

		MetaModel metaModel = metaModelRepo.all()
				.filter("self.edited = true and self.customised = true")
				.fetchOne();

		if (metaModel != null) {
			response.setAlert("Server restart required due to updated models. Are you sure to continue ?");
		}

	}

	public void setDefaultProperties(ActionRequest request,
			ActionResponse response) {

		AppSettings appSettings = AppSettings.get();

		String buildDir = appSettings.get("build.dir");
		String axelorHome = appSettings.get("axelor.home");
		String tomcatPath = appSettings.get("tomcat.path");
		String appName = appSettings.get("app.name");
		String dbName = appSettings.get("db.default.url");

		log.debug("Build dir: {}", buildDir);

		if (!Strings.isNullOrEmpty(buildDir)) {
			response.setValue("buildDir", buildDir);
		}

		if (!Strings.isNullOrEmpty(axelorHome)) {
			response.setValue("axelorHome", axelorHome);
		}

		if (!Strings.isNullOrEmpty(tomcatPath)) {
			response.setValue("tomcatPath", tomcatPath);
		}

		if (!Strings.isNullOrEmpty(appName)) {
			response.setValue("appName", appName);
		}

		if (!Strings.isNullOrEmpty(dbName)) {
			response.setValue("dbName",
					dbName.substring(dbName.lastIndexOf("/") + 1));
		}

	}
	
	public void reset(ActionRequest request, ActionResponse response) throws IOException, InterruptedException {
		
		if (configService == null) {
			response.setFlash("Error in configuration. Please check configuration properties");
			return;
		}
		
		File moduleDir = configService.getModuleDir();
		log.debug("Deleting directory: {}",moduleDir.getPath());
		FileUtils.deleteDirectory(moduleDir);
		String[] result = updateAppService.buildApp();
		if (!result[0].equals("0")) {
			response.setFlash(I18n
					.get("Error in build. Please check the log"));
			return;
		}
		
		response.setFlash(updateAppService.updateApp(true));
		response.setSignal("refresh-app", true);
		
		updateAppService.clearDatabase();
		
	}
	
}
