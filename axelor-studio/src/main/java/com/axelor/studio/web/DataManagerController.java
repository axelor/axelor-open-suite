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
package com.axelor.studio.web;

import java.io.IOException;


import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.service.data.exporter.ExporterService;
import com.axelor.studio.db.DataManager;
import com.axelor.studio.db.repo.DataManagerRepository;
import com.axelor.studio.service.data.exporter.AsciidocExporter;
import com.axelor.studio.service.data.exporter.DataWriter;
import com.axelor.studio.service.data.exporter.ExcelWriter;
import com.axelor.studio.service.data.importer.DataReader;
import com.axelor.studio.service.data.importer.ExcelReader;
import com.google.inject.Inject;

public class DataManagerController {
	
	@Inject
	private ExporterService exporterService;
	
	@Inject
	private AsciidocExporter asciidocExporter;
	
	@Inject
	private MetaFileRepository metaFileRepo;
	
	public void exportData(ActionRequest request, ActionResponse response) throws AxelorException, ClassNotFoundException{
		
		DataManager dataManager = request.getContext().asType(DataManager.class);
		
		MetaFile exportFile = dataManager.getMetaFile();
		DataWriter writer = new ExcelWriter();
		DataReader reader = new ExcelReader();
		if(exportFile != null && exportFile.getId() != null){
			exportFile = metaFileRepo.find(exportFile.getId());
			exportFile = exporterService.export(exportFile, writer, reader);
		}
		else{
			exportFile = exporterService.export(null, writer, reader);
		}
		
		response.setValue("metaFile", exportFile);
		
		
	}
	
	public void generateAsciidoc(ActionRequest request, ActionResponse response) throws AxelorException {
		
		DataManager dataManager = request.getContext().asType(DataManager.class);
		DataReader reader = new ExcelReader();
		MetaFile exportFile = dataManager.getMetaFile();
		
		try {
			MetaFile asciidocFile = asciidocExporter.export(exportFile, reader, 
					dataManager.getLanguageSelect(), "AsciiDoc");
			response.setValue("asciidocFile", asciidocFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
