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
package com.axelor.apps.prestashop.web;

import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.app.AppPrestaShopService;
import com.axelor.apps.prestashop.service.PrestaShopWebserviceException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AppPrestaShopController {
	
	@Inject
	private AppPrestaShopService service;
	
	/**
	 * Test connection with prestashop
	 * 
	 * @param request
	 * @param response
	 * @throws PrestaShopWebserviceException 
	 * @throws TransformerException
	 */
	public void testConnection(ActionRequest request, ActionResponse response) throws PrestaShopWebserviceException, TransformerException {
		
		AppPrestashop ps = request.getContext().asType(AppPrestashop.class);
		boolean test = service.connection(ps);
		
		if(test) {
			response.setAlert("Connection Sucessfully");
		} else {
			response.setAlert("Connection Fail");
		}
	}
	
	/**
	 * Validate url which are set in configuration
	 * 
	 * @param request
	 * @param response
	 */
	public void validUrl(ActionRequest request, ActionResponse response) {
		
		AppPrestashop ps = request.getContext().asType(AppPrestashop.class);
		boolean valid = service.urlTest(ps);
		
		if(valid) {
			response.setError("Invalid url");
		}
	}
}
