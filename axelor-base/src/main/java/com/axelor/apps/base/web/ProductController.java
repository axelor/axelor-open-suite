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
package com.axelor.apps.base.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProductController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private GeneralService generalService;
	
	@Inject
	private ProductService productService;
	
	@Inject
	private ProductRepository productRepo;

	public void generateProductVariants(ActionRequest request, ActionResponse response) throws AxelorException {
		Product product = request.getContext().asType(Product.class);
		product = productRepo.find(product.getId());

		if(product.getProductVariantConfig() != null)  {
			productService.generateProductVariants(product);

			response.setFlash(I18n.get(IExceptionMessage.PRODUCT_1));
			response.setReload(true);
		}
	}

	public void updateProductsPrices(ActionRequest request, ActionResponse response) throws AxelorException {
		Product product = request.getContext().asType(Product.class);

		product = productRepo.find(product.getId());

		productService.updateProductPrice(product);

		response.setFlash(I18n.get(IExceptionMessage.PRODUCT_2));
		response.setReload(true);
	}



	public void printProductCatelog(ActionRequest request, ActionResponse response) throws AxelorException {

		User user =  Beans.get(UserService.class).getUser();

		int currentYear = generalService.getTodayDateTime().getYear();
		String productIds = "";

		List<Integer> lstSelectedProduct = (List<Integer>) request.getContext().get("_ids");
		
		if(lstSelectedProduct != null)  {
			for(Integer it : lstSelectedProduct) {
				productIds+= it.toString()+",";
			}
		}

		if(!productIds.equals("")){
			productIds = productIds.substring(0, productIds.length()-1);
		}

		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en";

		String name = I18n.get("Product Catalog");
		
		String fileLink = ReportFactory.createReport(IReport.PRODUCT_CATALOG, name+"-${date}")
				.addParam("UserId", user.getId())
				.addParam("CurrYear", Integer.toString(currentYear))
				.addParam("ProductIds", productIds)
				.addParam("Locale", language)
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}

	public void printProductSheet(ActionRequest request, ActionResponse response) throws AxelorException {

		Product product = request.getContext().asType(Product.class);
		User user =  Beans.get(UserService.class).getUser();

		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en";

		String name = I18n.get("Product") + " " + product.getCode();
		
		String fileLink = ReportFactory.createReport(IReport.PRODUCT_SHEET, name+"-${date}")
				.addParam("ProductId", product.getId())
				.addParam("CompanyId", user.getActiveCompany().getId())
				.addParam("Locale", language)
				.generate()
				.getFileLink();

		logger.debug("Printing "+name);
	
		response.setView(ActionView
				.define(name)
				.add("html", fileLink).map());	
	}
}
