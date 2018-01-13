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
package com.axelor.apps.production.service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.axelor.apps.production.db.TempBomTree;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.TempBomTreeRepository;
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

	private final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	protected UnitConversionService unitConversionService;

	@Inject
	private ProductService productService;

	@Inject
	protected AppBaseService appBaseService;
	
	@Inject
	protected BillOfMaterialRepository billOfMaterialRepo;
	
	@Inject
	private TempBomTreeRepository tempBomTreeRepo;
	
	private List<Long> processedBom;
	
	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public List<BillOfMaterial> getBillOfMaterialSet(Product product)  {

		return billOfMaterialRepo.all().filter("self.product = ?1", product).fetch();


	}

	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateProductCostPrice(BillOfMaterial billOfMaterial) throws AxelorException  {

		Product product = billOfMaterial.getProduct();

		if (product.getCostTypeSelect() != ProductRepository.COST_TYPE_STANDARD) {
		    throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.COST_TYPE_CANNOT_BE_CHANGED));
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
		
		List<BillOfMaterial> BillOfMaterialSet = Lists.newArrayList();
		BillOfMaterial up = billOfMaterial;
		Long previousId = Long.valueOf(0);
		do{
			BillOfMaterialSet = billOfMaterialRepo.all().filter("self.originalBillOfMaterial = :origin AND self.id != :id").bind("origin", up).bind("id", previousId).order("-versionNumber").fetch();
			if (!BillOfMaterialSet.isEmpty()){
				latestVersion = (BillOfMaterialSet.get(0).getVersionNumber() > latestVersion) ? BillOfMaterialSet.get(0).getVersionNumber() : latestVersion;
				for (BillOfMaterial billOfMaterialIterator : BillOfMaterialSet) {
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
	
	@Override
	public TempBomTree generateTree(BillOfMaterial billOfMaterial) {
		
		processedBom = new ArrayList<Long>();

		TempBomTree bomTree = getBomTree(billOfMaterial, null, null);
		
		return bomTree;
	}
	
	@Transactional
	public TempBomTree getBomTree(BillOfMaterial bom, BillOfMaterial parentBom, TempBomTree parent) {
		
		TempBomTree bomTree = null;
		if (parentBom == null) {
			bomTree = tempBomTreeRepo.all().filter("self.bom = ?1 and self.parentBom = null", bom).fetchOne();
		}
		else {
			bomTree =  tempBomTreeRepo.all().filter("self.bom = ?1 and self.parentBom = ?2", bom, parentBom).fetchOne();
		}
		
		if (bomTree == null) {
			bomTree = new TempBomTree();
		}
		bomTree.setProdProcess(bom.getProdProcess());
		bomTree.setProduct(bom.getProduct());
		bomTree.setQty(bom.getQty());
		bomTree.setUnit(bom.getUnit());
		bomTree.setParentBom(parentBom);
		bomTree.setParent(parent);
		bomTree.setBom(bom);
		bomTree = tempBomTreeRepo.save(bomTree);
		
		processedBom.add(bom.getId());
		
		List<Long> validBomIds = processChildBom(bom, bomTree);
		
		validBomIds.add(new Long(0));
		
		removeInvalidTree(validBomIds, bom);
			
		return bomTree;
	}

	private List<Long> processChildBom(BillOfMaterial bom, TempBomTree bomTree) {
		
		List<Long> validBomIds = new ArrayList<Long>(); 
		
		for (BillOfMaterial childBom : bom.getBillOfMaterialSet()) {
			if (!processedBom.contains(childBom.getId())) {
				getBomTree(childBom, bom, bomTree);
			}
			else {
				log.debug("Already processed: {}", childBom.getId());
			}
			validBomIds.add(childBom.getId());
		}
		
		return validBomIds;
	}
	
	@Transactional
	public void removeInvalidTree(List<Long> validBomIds, BillOfMaterial bom) {
		
		List<TempBomTree> invalidBomTrees = tempBomTreeRepo.all()
				.filter("self.bom.id not in (?1) and self.parentBom = ?2", validBomIds, bom)
				.fetch();
		
		log.debug("Invalid bom trees: {}", invalidBomTrees);
		
		if (!invalidBomTrees.isEmpty()) {
			List<TempBomTree> childBomTrees = tempBomTreeRepo.all()
					.filter("self.parent in (?1)", invalidBomTrees)
					.fetch();
			
			for (TempBomTree childBomTree : childBomTrees) {
				childBomTree.setParent(null);
				tempBomTreeRepo.save(childBomTree);
			}
		}
		
		for (TempBomTree invalidBomTree: invalidBomTrees) {
			tempBomTreeRepo.remove(invalidBomTree);
		}
		
	}

}
