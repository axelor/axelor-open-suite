/*
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
package com.axelor.apps.prestashop.web;

import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.app.AppPrestaShopService;
import com.axelor.apps.prestashop.imports.service.ImportMetaDataService;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppPrestaShopController {
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	private AppPrestaShopService service;

	@Inject
	private ImportMetaDataService metadataService;

	/**
	 * Test connection with prestashop
	 *
	 * @param request
	 * @param response
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 */
	public void testConnection(ActionRequest request, ActionResponse response) throws PrestaShopWebserviceException, TransformerException {
		AppPrestashop appConfig = request.getContext().asType(AppPrestashop.class);
		final List<String> errors = new LinkedList<>();
		final List<String> warnings = new LinkedList<>();
		final List<String> info = new LinkedList<>();
		service.checkAccess(appConfig, errors, warnings, info);

		if(errors.isEmpty() == false) {
			response.setError(StringUtils.join(errors, "<br/>"));
		} else if(warnings.isEmpty() == false) {
			response.setAlert(StringUtils.join(warnings, "<br/>"));
		} else if(info.isEmpty() == false) {
			response.setFlash(StringUtils.join(info, "<br/>"));
		} else {
			response.setFlash(I18n.get("Connection successful"));
		}
	}

	public void importMetadata(ActionRequest request, ActionResponse response) {
		AppPrestashop appConfig = request.getContext().asType(AppPrestashop.class);
		PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		try {
			metadataService.importLanguages(ws);
			metadataService.importOrderStatuses(appConfig.getTextsLanguage(), ws);
		} catch(PrestaShopWebserviceException e) {
			response.setError(String.format(I18n.get("Error while fetching metadata, please perform a connection check: %s"), e.getLocalizedMessage()));
			log.error("Error while fetch PrestaShop metadata", e);
		}
	}
}
