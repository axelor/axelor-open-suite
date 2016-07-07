package com.axelor.studio.web;

import java.io.File;
import java.io.IOException;

import javax.validation.ValidationException;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ModelImporter;
import com.axelor.studio.db.repo.ModelImporterRepository;
import com.axelor.studio.service.importer.ModelImporterService;
import com.google.inject.Inject;

public class ModelImporterController {

	@Inject
	private ModelImporterService modelImporterService;

	@Inject
	private ModelImporterRepository modelImporterRepo;
	
	@Inject
	private MetaFiles metaFiles;

	public void importModels(ActionRequest request, ActionResponse response)
			throws IOException, AxelorException {

		ModelImporter modelImporter = request.getContext().asType(
				ModelImporter.class);

		modelImporter = modelImporterRepo.find(modelImporter.getId());

		try {
			File logFile = modelImporterService.importModels(modelImporter);
			if (logFile != null) {
				response.setFlash(I18n.get("Input file is not valid. "
						+ "Please check the log file generated"));
				response.setValue("logFile", metaFiles.upload(logFile));
			}
			else {
				response.setValue("logFile", null);
				response.setFlash(I18n.get("Models imported successfully"));
			}
			
		} catch (ValidationException e) {
			response.setFlash(I18n.get("Error") + ": " + e.getMessage());
		}
		
		
	}
}
