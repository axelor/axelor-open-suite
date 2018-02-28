/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.auth.web;

import org.joda.time.LocalDateTime;

import com.axelor.auth.db.IMessage;
import com.axelor.auth.db.repo.PermissionAssistantRepository;
import com.axelor.auth.service.PermissionAssistantService;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PermissionAssitantController {

	@Inject
	PermissionAssistantRepository permissionAssistantRepo;
	
	@Inject
	PermissionAssistantService permissionAssistantService;


	public void createFile(ActionRequest request, ActionResponse response){

		Long permissionAssistantId = (Long)request.getContext().get("id");
		permissionAssistantService.createFile(permissionAssistantRepo.find(permissionAssistantId));
		response.setReload(true);

	}

	public void importPermissions(ActionRequest request, ActionResponse response){

		Long permissionAssistantId = (Long)request.getContext().get("id");
		String errors = permissionAssistantService.importPermissions(permissionAssistantRepo.find(permissionAssistantId));
		response.setValue("importDate", LocalDateTime.now());
		response.setValue("log", errors);

		if(errors.equals("")){
			response.setFlash(I18n.get(IMessage.IMPORT_OK));
		}
		else{
			response.setFlash(I18n.get(IMessage.ERR_IMPORT));
		}

	}

}
