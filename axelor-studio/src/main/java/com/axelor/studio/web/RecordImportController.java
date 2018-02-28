/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.joda.time.LocalDateTime;

import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.RecordImportWizard;
import com.axelor.studio.db.repo.RecordImportWizardRepository;
import com.axelor.studio.service.data.importer.DataReader;
import com.axelor.studio.service.data.importer.ExcelReader;
import com.axelor.studio.service.data.record.RecordImporterService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class RecordImportController {
	
	@Inject
	private RecordImportWizardRepository importWizardRepo;
	
	@Inject
	private RecordImporterService importService;
	
	@Inject
	private MetaFiles metaFiles;
	
	public void importRecords(ActionRequest request, ActionResponse response) throws IOException {
		
		RecordImportWizard importWizard = request.getContext().asType(RecordImportWizard.class);
		importWizard = importWizardRepo.find(importWizard.getId());
		
		DataReader reader = new ExcelReader();
		reader.initialize(importWizard.getImportFile());
		
		String msg = I18n.get("Records imported successfully");
		try {
			importService.importRecords(reader, importWizard.getImportFile(), importWizard.getImportPreference());
		} catch (AxelorException e) {
			msg = e.getMessage();
		}
		
		String log = importService.getLog();
		if (!Strings.isNullOrEmpty(log)) {
			msg = I18n.get("Error in import. Please check the log");
		}
		
		response.setFlash(msg);
		response.setValue("importLogFile", getLogFile(log, importWizard.getImportLogFile()));
		response.setValue("importedBy", AuthUtils.getUser());
		response.setValue("importDate", new LocalDateTime());
		
	}

	private MetaFile getLogFile(String log, MetaFile metaFile) throws IOException {
		
		if (Strings.isNullOrEmpty(log)) {
			return null;
		}
		
		File logFile = null;
		if (metaFile == null) {
			metaFile = new MetaFile();
		}
		logFile = File.createTempFile("ImportLog", ".txt");
		
		FileWriter fw = new FileWriter(logFile);
		fw.write(log);
		fw.close();
		
		return metaFiles.upload(logFile, metaFile);
	}
}
