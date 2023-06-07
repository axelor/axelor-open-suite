package com.axelor.apps.production.service;

import java.math.BigDecimal;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class BillOfMaterialLineServiceImpl implements BillOfMaterialLineService {

	protected ProductRepository productRepository;
	
	@Inject
	public BillOfMaterialLineServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	@Override
	public BillOfMaterialLine createFromRawMaterial(long productId, int priority) {
	    Product product = productRepository.find(productId);
	    BillOfMaterial bom = null;
	    if (product != null && product.getProductSubTypeSelect().equals(ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT)) {
	    	bom = product.getDefaultBillOfMaterial();
	    }
	    
	    return newBillOfMaterial(product, bom, BigDecimal.ONE, priority, false);

	}

	@Override
	public BillOfMaterialLine newBillOfMaterial(Product product, BillOfMaterial billOfMaterial, BigDecimal qty,
			Integer priority, boolean hasNoManageStock) {
		
		BillOfMaterialLine billOfMaterialLine = new BillOfMaterialLine();
		
		billOfMaterialLine.setProduct(product);
		billOfMaterialLine.setBillOfMaterial(billOfMaterial);
		billOfMaterialLine.setQty(qty);
		billOfMaterialLine.setPriority(priority);
		billOfMaterial.setHasNoManageStock(hasNoManageStock);
		
		return billOfMaterialLine;

	}

	
	
}
