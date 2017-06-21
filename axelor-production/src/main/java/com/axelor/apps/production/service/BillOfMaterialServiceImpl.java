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
package com.axelor.apps.production.service;

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.exception.db.IException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class BillOfMaterialServiceImpl implements BillOfMaterialService {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	protected UnitConversionService unitConversionService;

	@Inject
	private ProductService productService;

	@Inject
	protected AppBaseService appBaseService;
	
	@Inject
	protected BillOfMaterialRepository billOfMaterialRepo;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public List<BillOfMaterial> getBillOfMaterialList(Product product)  {

		return billOfMaterialRepo.all().filter("self.product = ?1", product).fetch();


	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {

		Product product = billOfMaterial.getProduct();

		if (product.getCostTypeSelect() != ProductRepository.COST_TYPE_STANDARD) {
		    throw new AxelorException(I18n.get(
		    		IExceptionMessage.COST_TYPE_CANNOT_BE_CHANGED),
					IException.CONFIGURATION_ERROR);
		}

		product.setCostPrice(billOfMaterial.getCostPrice().divide(billOfMaterial.getQty()).setScale(appBaseService.getNbDecimalDigitForUnitPrice(), BigDecimal.ROUND_HALF_UP));

		productService.updateSalePrice(product);

		billOfMaterialRepo.save(billOfMaterial);
	}


	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public BillOfMaterial customizeBillOfMaterial(SaleOrderLine saleOrderLine)  {

		BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();

		if(billOfMaterial != null)  {
			BillOfMaterial personalizedBOM = JPA.copy(billOfMaterial, true);
			billOfMaterialRepo.save(personalizedBOM);
			personalizedBOM.setName(personalizedBOM.getName() + " ("+I18n.get(IExceptionMessage.BOM_1)+" " + personalizedBOM.getId() + ")");
			personalizedBOM.setPersonalized(true);
			return personalizedBOM;
		}

		return null;

	}

	@Transactional
	public BillOfMaterial generateNewVersion(BillOfMaterial billOfMaterial){
		
		BillOfMaterial copy = billOfMaterialRepo.copy(billOfMaterial, true);
		
		copy.setOriginalBillOfMaterial(billOfMaterial);
		copy.clearCostSheetList();
		copy.setCostPrice(BigDecimal.ZERO);
		copy.setOriginalBillOfMaterial(billOfMaterial);
		copy.setVersionNumber(this.getLatestBillOfMaterialVersion(billOfMaterial, billOfMaterial.getVersionNumber(), true) + 1);
		
		return billOfMaterialRepo.save(copy);
	}
	
	public int getLatestBillOfMaterialVersion(BillOfMaterial billOfMaterial, int latestVersion, boolean deep){
		
		List<BillOfMaterial> billOfMaterialList = Lists.newArrayList();
		BillOfMaterial up = billOfMaterial;
		Long previousId = Long.valueOf(0);
		do{
			billOfMaterialList = billOfMaterialRepo.all().filter("self.originalBillOfMaterial = :origin AND self.id != :id").bind("origin", up).bind("id", previousId).order("-versionNumber").fetch();
			if (!billOfMaterialList.isEmpty()){
				latestVersion = (billOfMaterialList.get(0).getVersionNumber() > latestVersion) ? billOfMaterialList.get(0).getVersionNumber() : latestVersion;
				for (BillOfMaterial billOfMaterialIterator : billOfMaterialList) {
					int search = this.getLatestBillOfMaterialVersion(billOfMaterialIterator, latestVersion, false);
					latestVersion = (search > latestVersion) ?  search : latestVersion;
				}
			}
			previousId = up.getId(); 
			up = up.getOriginalBillOfMaterial();
		}while(up != null && deep);
		
		return latestVersion;
	}

	@Override
	public String getLanguageForPrinting(BillOfMaterial billOfMaterial) {
		String language="";
		try{
			language = billOfMaterial.getCompany().getPrintingSettings().getLanguageSelect() != null ? billOfMaterial.getCompany().getPrintingSettings().getLanguageSelect() : "en" ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;
		
		return language;
	}
	
	@Override
	public String getFileName(BillOfMaterial billOfMaterial) {
		
		return I18n.get("Bill of Material") + "-" + billOfMaterial.getName() + ((billOfMaterial.getVersionNumber() > 1) ? "-V" + billOfMaterial.getVersionNumber() : "");
	}

	@Override
	public String getReportLink(BillOfMaterial billOfMaterial, String name, String language, String format)
			throws AxelorException {
		
		return ReportFactory.createReport(IReport.BILL_OF_MATERIAL, name+"-${date}")
				.addParam("Locale", language)
				.addParam("BillOfMaterialId", billOfMaterial.getId())
				.addFormat(format)
				.generate()
				.getFileLink();
	}

}
