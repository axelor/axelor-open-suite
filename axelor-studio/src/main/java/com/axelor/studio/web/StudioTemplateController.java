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
package com.axelor.studio.web;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.StudioTemplate;
import com.axelor.studio.db.repo.StudioTemplateRepository;
import com.axelor.studio.service.template.StudioTemplateService;
import com.google.inject.Inject;

public class StudioTemplateController {

	@Inject
	private StudioTemplateService studioTemplateService;

	@Inject
	private StudioTemplateRepository templateRepo;

	public void importTemplate(ActionRequest request, ActionResponse response) {

		StudioTemplate template = request.getContext().asType(
				StudioTemplate.class);
		template = templateRepo.find(template.getId());

		String msg = studioTemplateService.importTemplate(template);
		response.setFlash(msg);
		response.setReload(true);
	}

	public void exportTemplate(ActionRequest request, ActionResponse response) {

		StudioTemplate template = request.getContext().asType(
				StudioTemplate.class);

		MetaFile metaFile = studioTemplateService.export(template.getName());
		if (metaFile == null) {
			response.setFlash(I18n.get("Error in export"));
		}

		response.setValue("metaFile", metaFile);
	}

}
