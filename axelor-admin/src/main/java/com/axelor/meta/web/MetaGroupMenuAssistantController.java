/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2015 Axelor (<http://axelor.com>).
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
package com.axelor.meta.web;

import org.joda.time.LocalDateTime;

import com.axelor.meta.db.repo.MetaGroupMenuAssistantRepository;
import com.axelor.meta.service.MetaGroupMenuAssistantService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MetaGroupMenuAssistantController{

	@Inject
	private MetaGroupMenuAssistantService groupMenuAssistantService;
	
	@Inject
	private MetaGroupMenuAssistantRepository groupMenuAssistantRepo;


	public void createGroupMenuFile(ActionRequest request, ActionResponse response) {

		Long groupMenuAssistantId = (Long)request.getContext().get("id");
		groupMenuAssistantService.createGroupMenuFile(groupMenuAssistantRepo.find(groupMenuAssistantId));
		response.setReload(true);
	}

	public void importGroupMenu(ActionRequest request, ActionResponse response) {

		Long groupMenuAssistantId = (Long)request.getContext().get("id");
		String errorLog = groupMenuAssistantService.importGroupMenu(groupMenuAssistantRepo.find(groupMenuAssistantId));

		if(errorLog.equals("")){
			response.setFlash("Import completed succesfully");
			response.setValue("importDate", LocalDateTime.now());
		}
		else{
			response.setValue("log", errorLog);
			response.setFlash("Error in import. Please check the log");
		}

	}
}
