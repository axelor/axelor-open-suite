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
package com.axelor.apps.admin.web;

import org.joda.time.LocalDateTime;

import com.axelor.apps.admin.db.ViewDoc;
import com.axelor.apps.admin.service.ViewDocExportService;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ViewDocController {
	
	@Inject
	private ViewDocExportService exportService;
	
	@Inject
	private MetaFileRepository metaFileRepo;
	
	public void export(ActionRequest request, ActionResponse response){
		
		ViewDoc viewDoc = request.getContext().asType(ViewDoc.class);
		
		MetaFile exportFile = viewDoc.getExportFile();
		if(exportFile != null && exportFile.getId() != null){
			exportFile = metaFileRepo.find(exportFile.getId());
			exportFile = exportService.export(exportFile, viewDoc.getExportOnlyPanel());
		}
		else{
			exportFile = exportService.export(null, viewDoc.getExportOnlyPanel());
		}
		
		response.setValue("exportFile", exportFile);
		
		response.setValue("exportDate", LocalDateTime.now());
		
	}
}
