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
package com.axelor.apps.production.web;

import java.util.List;

import javax.inject.Inject;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.CostSheetService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;

public class BillOfMaterialController {

	@Inject
	BillOfMaterialService billOfMaterialService;
	
	@Inject
	CostSheetService costSheetService;
	
	@Inject
	BillOfMaterialRepository billOfMaterialRepo;
	
	public void computeCostPrice (ActionRequest request, ActionResponse response) throws AxelorException {

		BillOfMaterial billOfMaterial = request.getContext().asType( BillOfMaterial.class );

		CostSheet costSheet = costSheetService.computeCostPrice(billOfMaterialRepo.find(billOfMaterial.getId()));
		
		response.setView(ActionView
				.define(String.format(I18n.get("Cost sheet - %s"), billOfMaterial.getName()))
				.model(CostSheet.class.getName())
				.param("popup", "true")
				.param("show-toolbar", "false")
				.param("show-confirm", "false")
				.param("popup-save", "false")
				.add("grid", "cost-sheet-bill-of-material-grid")
				.add("form", "cost-sheet-bill-of-material-form")
				.context("_showRecord", String.valueOf(costSheet.getId())).map());
		
		response.setReload(true);
		
	}
	
	
	public void updateProductCostPrice (ActionRequest request, ActionResponse response) throws AxelorException {

		BillOfMaterial billOfMaterial = request.getContext().asType( BillOfMaterial.class );

		billOfMaterialService.updateProductCostPrice(billOfMaterialRepo.find(billOfMaterial.getId()));
		
		response.setReload(true);
		
	}
	
	public void checkOriginalBillOfMaterial(ActionRequest request, ActionResponse response){
		
		BillOfMaterial billOfMaterial = billOfMaterialRepo.find( request.getContext().asType(BillOfMaterial.class).getId() );
		
		List<BillOfMaterial> billOfMaterialList = Lists.newArrayList();
		billOfMaterialList = billOfMaterialRepo.all().filter("self.originalBillOfMaterial = :origin").bind("origin", billOfMaterial).fetch();
		String message;
		
		if(!billOfMaterialList.isEmpty()){
			
			String existingVersions = "";
			for (BillOfMaterial billOfMaterialVersion : billOfMaterialList) {
				existingVersions += "<li>" + billOfMaterialVersion.getFullName() + "</li>";
			}
			message = String.format(I18n.get("This bill of material already has the following versions : <br/><ul> %s </ul>And these versions may also have ones. Do you still wish to create a new one ?"), existingVersions);
		}
		else{
			message = I18n.get("Do you really wish to create a new version of this bill of material ?");
		}
		
		response.setAlert(message);
		
	}
	
	public void generateNewVersion(ActionRequest request, ActionResponse response){
		
		BillOfMaterial billOfMaterial = billOfMaterialRepo.find( request.getContext().asType(BillOfMaterial.class).getId() );
		
		BillOfMaterial copy = billOfMaterialService.generateNewVersion(billOfMaterial);
		
		response.setView(ActionView.define("Bill of material")
				.model(BillOfMaterial.class.getName())
				   .add("form","bill-of-material-form")
				   .add("grid","bill-of-material-grid")
				   .domain("self.isRawMaterial = false AND self.personalized = false AND self.parentBillOfMaterial IS NULL")
				   .context("_showRecord", String.valueOf(copy.getId()))
				   .map());
	}
	
}
