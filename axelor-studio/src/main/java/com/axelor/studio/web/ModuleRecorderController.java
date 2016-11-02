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
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ModuleRecorder;
import com.axelor.studio.service.ModuleRecorderService;
import com.google.inject.Inject;

public class ModuleRecorderController {

	@Inject
	private ModuleRecorderService moduleRecorderService;

	public void update(ActionRequest request, ActionResponse response) {
		
		ModuleRecorder moduleRecorder = request.getContext().asType(
				ModuleRecorder.class);
		
		response.setSignal("refresh-app", true);
		try {
			String msg = moduleRecorderService.update(moduleRecorder);
			response.setFlash(msg);
		} catch(Exception e) {
			e.printStackTrace();
			response.setFlash(e.getMessage());
		}

	}

	public void reset(ActionRequest request, ActionResponse response) 
			throws IOException, AxelorException {
		
		ModuleRecorder moduleRecorder = request.getContext().asType(
				ModuleRecorder.class);
		
		response.setSignal("refresh-app", true);
		try {
			String msg = moduleRecorderService.reset(moduleRecorder);
			response.setFlash(msg);
		} catch(Exception e) {
			e.printStackTrace();
			response.setFlash(e.getMessage());
		}
		
	}
	
}
