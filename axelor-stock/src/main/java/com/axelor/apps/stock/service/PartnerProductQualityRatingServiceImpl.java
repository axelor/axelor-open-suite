package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.stock.db.PartnerProductQualityRate;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.PartnerProductQualityRateRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.web.StockMoveLineController;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerProductQualityRatingServiceImpl implements PartnerProductQualityRatingService {
	
	
	@Inject
	private PartnerRepository partnerRepository;
	
	@Inject
	private PartnerProductQualityRateRepository partnerProductQualityRateRepo;
	
	@Inject
	private StockMoveLineRepository stockMoveLineRepo;
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void calculate(StockMove stockMove){
		
		Partner partner = stockMove.getPartner();
		BigDecimal qualityRate;
		Product product = null;
		Integer conformity;
		List<StockMoveLine> stockMoveLines = stockMove.getStockMoveLineList();
				
		for(StockMoveLine line : stockMoveLines) {
			
			// search
			product = searchProduct(line, product, partner);
			
			// create
			if(product == null ) {
				product = line.getProduct();
				createProductMoveLine(partner, product);
			}
			
			// update
			qualityRate = BigDecimal.ZERO;
			conformity = line.getConformitySelect();
			updateProductMoveLine(partner, product, qualityRate, conformity, line);
			
		}
		
		partnerRepository.save(partner);
	}
	
	
	public Product searchProduct(StockMoveLine line, Product product, Partner partner) {
		
		product = line.getProduct();
		PartnerProductQualityRate partnerProductQualityRate = partnerProductQualityRateRepo.findProductByName(product, partner);
		
		
		if(partnerProductQualityRate == null) {
			return null;
		}
		
		return product;		
	}
	
	
	public PartnerProductQualityRate createProductMoveLine(Partner partner, Product product) {
		
		PartnerProductQualityRate productMoveLine = new PartnerProductQualityRate();
		
		productMoveLine.setPartner(partner);
		productMoveLine.setProduct(product);
		
		return partnerProductQualityRateRepo.save(productMoveLine);
		
	}
	
	
	public PartnerProductQualityRate updateProductMoveLine(Partner partner, Product product, BigDecimal qualityRate, Integer conformity, StockMoveLine line) {
		
		List <PartnerProductQualityRate> partnerProductMoveLines = partner.getPartnerProductQualityRateList();
	
		for(PartnerProductQualityRate partnerProductLine : partnerProductMoveLines) {
			
			if(product == partnerProductLine.getProduct()) {
				
				qualityRate = partnerProductLine.getQualityRate();
				BigDecimal productTotal = BigDecimal.ZERO;
				
				BigDecimal partnerProductQualityRate = partnerProductLine.getQualityRate();				
				//BigDecimal lineQty = partnerProductLinesTotal(line, partnerProductLine);
				BigDecimal supplierProductRate = supplierProductRate(partnerProductLine);
				
				if(conformity == StockMoveRepository.CONFORMITY_COMPLIANT) {
					partnerProductQualityRate = partnerProductQualityRate.add(line.getRealQty());
				}
				
				List<StockMoveLine> partnerProductMoveLineTotal = stockMoveLineRepo.all().filter("self.stockMove.typeSelect = 3 AND self.stockMove.partner.fullName = ?1 AND self.product = ?2", partner.getFullName(), product).fetch();
				
				for(StockMoveLine test : partnerProductMoveLineTotal) {
					System.out.println("!! " + productTotal + " !!");

					productTotal = productTotal.add(test.getRealQty());

					System.out.println("++ " + productTotal + " ++");
				}
						
				partnerProductLine.setQualityRate(partnerProductQualityRate);
				partnerProductLine.setSupplierRate(supplierProductRate);
				
				partnerProductLine.setPartnerProductMoveLineTotal(productTotal);

				return partnerProductQualityRateRepo.save(partnerProductLine);
			}
		}
		
		return null;
		
	}
	
	
	public BigDecimal supplierProductRate(PartnerProductQualityRate partnerProductLine) {
		
		BigDecimal productQualityRate = partnerProductLine.getQualityRate();
		BigDecimal productMoveLineRate = partnerProductLine.getPartnerProductMoveLineTotal();
		BigDecimal supplierProductRate = null;
		
		if(productMoveLineRate == BigDecimal.ZERO){
			productMoveLineRate = BigDecimal.ONE;
		} 

		supplierProductRate = productQualityRate.multiply(new BigDecimal(5)).divide(productMoveLineRate, 2, RoundingMode.HALF_UP);
		
		
		System.out.println("== " + supplierProductRate + " ==");
		return supplierProductRate;
	}
	

}
