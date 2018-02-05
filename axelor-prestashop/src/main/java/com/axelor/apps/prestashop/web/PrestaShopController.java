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

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.prestashop.batch.PrestaShopBatchService;
import com.axelor.apps.prestashop.db.PrestaShopBatch;
import com.axelor.apps.prestashop.db.repo.PrestaShopBatchRepository;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PrestaShopController {
	
	@Inject
	private PrestaShopBatchService prestaShopBatchService;

	@Inject
	private PrestaShopBatchRepository prestaShopBatchRepo;
	
	/**
	 * Import objects/resources from prestashop to ABS
	 * 
	 * @param request
	 * @param response
	 * @throws PrestaShopWebserviceException
	 * @throws ParseException
	 * @throws TransformerException
	 */
	public void importPrestShop(ActionRequest request, ActionResponse response) throws PrestaShopWebserviceException, ParseException, TransformerException{
		
		PrestaShopBatch prestaShopBatch = request.getContext().asType(PrestaShopBatch.class);
		Batch batch = prestaShopBatchService.importPrestaShop(prestaShopBatchRepo.find(prestaShopBatch.getId()));
		response.setValue("prestaShopBatchLog", batch.getPrestaShopBatchLog());
		
		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}
	
	/**
	 * Export objects/resources from ABS to prestashop
	 * 
	 * @param request
	 * @param response
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws NumberFormatException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void exportPrestShop(ActionRequest request, ActionResponse response) throws PrestaShopWebserviceException, TransformerException, NumberFormatException, MalformedURLException, IOException, SAXException, ParserConfigurationException{

		PrestaShopBatch prestaShopBatch = request.getContext().asType(PrestaShopBatch.class);
		
		Batch batch = prestaShopBatchService.exportPrestaShop(prestaShopBatchRepo.find(prestaShopBatch.getId()));
		response.setValue("prestaShopBatchLog", batch.getPrestaShopBatchLog());
		
		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}
}
