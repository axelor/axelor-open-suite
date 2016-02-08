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
package com.axelor.apps.stock.web;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.db.repo.ProductFamilyRepository;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.report.IReport;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class LocationController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@Inject
	private InventoryService inventoryService;
	
	@Inject
	private LocationRepository locationRepo;
	
	public void checkIsDefaultLocation(ActionRequest request, ActionResponse response){
		
		Location location = request.getContext().asType(Location.class);
		
		if(location != null && location.getIsDefaultLocation() && location.getCompany() != null && location.getTypeSelect() != null) {
			
			Location findLocation = locationRepo.all().filter("company = ? and typeSelect = ? and isDefaultLocation = ?", location.getCompany(),location.getTypeSelect(),location.getIsDefaultLocation()).fetchOne();
			
			if(findLocation != null) {
				response.setFlash(I18n.get(IExceptionMessage.LOCATION_1)+" "+findLocation.getName());
				response.setValue("isDefaultLocation", false);
			}
		}
	}
	
	public void createInventory(ActionRequest request, ActionResponse response) throws Exception {
		Context context = request.getContext();
		LocalDate date = new LocalDate(context.get("inventoryDate"));
		String description = (String) context.get("description");
		
		boolean excludeOutOfStock = (Boolean) context.get("excludeOutOfStock");
		boolean includeObsolete = (Boolean) context.get("includeObsolete");
		
		// Récupération de l'entrepot
		Map<String, Object> locationContext = (Map<String, Object>) context.get("location");
		
		Location location = null;
		
		if(locationContext != null)  {
			location = locationRepo.find(((Integer)locationContext.get("id")).longValue());
		}
		
		// Récupération de la famille de produit
		Map<String, Object> productFamilyContext = (Map<String, Object>) context.get("productFamily");
		
		ProductFamily productFamily = null;
		
		if (productFamilyContext != null) {
			productFamily = Beans.get(ProductFamilyRepository.class).find(((Integer)productFamilyContext.get("id")).longValue());
		}
		
		// Récupération de la catégorie de produit
		Map<String, Object> productCategoryContext = (Map<String, Object>) context.get("productCategory");
		
		ProductCategory productCategory = null;
		
		if (productCategoryContext != null) {
			productCategory = Beans.get(ProductCategoryRepository.class).find(((Integer)productCategoryContext.get("id")).longValue());
		}
		
		
		Inventory inventory = inventoryService.createInventoryFromWizard(date, description, location, excludeOutOfStock,
										includeObsolete, productFamily, productCategory);
		response.setValue("inventoryId", inventory.getId());
	}
	
	/**
	 * Method that generate inventory as a pdf
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void print(ActionRequest request, ActionResponse response) throws AxelorException {


		Location location = request.getContext().asType(Location.class );
		String locationIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedLocations = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedLocations != null){
			for(Integer it : lstSelectedLocations) {
				locationIds+= it.toString()+",";
			}
		}	
			
		if(!locationIds.equals("")){
			locationIds = locationIds.substring(0, locationIds.length()-1);	
			location = locationRepo.find(new Long(lstSelectedLocations.get(0)));
		}else if(location.getId() != null){
			locationIds = location.getId().toString();			
		}
		
		if(!locationIds.equals("")){
			
			User user = AuthUtils.getUser();
			Company company = location.getCompany();
			
			String language = "en";
			if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			String title = I18n.get("Location");
			if(location.getName() != null)  {
				title = lstSelectedLocations == null ? I18n.get("Location") + " " + location.getName() : I18n.get("Location(s)");
			}
		
			String fileLink = ReportFactory.createReport(IReport.STOCK_LOCATION, title+"-${date}")
					.addParam("StockLocationId", locationIds)
					.addParam("Locale", language)
					.generate()
					.getFileLink();

			logger.debug("Printing "+title);
		
			response.setView(ActionView
					.define(title)
					.add("html", fileLink).map());	
				
		}else{
			response.setFlash(I18n.get(IExceptionMessage.LOCATION_2));
		}	
	}
	
}
