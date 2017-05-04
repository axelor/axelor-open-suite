package com.axelor.apps.stock.service;

import java.math.BigDecimal;
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
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void calculate(StockMove stockMove){
		
		Partner partner = stockMove.getPartner();
		Integer valueConformity = partner.getSupplierQualityRating();
		Integer qualityRate;
		Integer qualityRating;
		Product product = null;
		Integer conformity;
		List<StockMoveLine> stockMoveLines = stockMove.getStockMoveLineList();
				
		for(StockMoveLine line : stockMoveLines) {
			
			// search
			product = searchProduct(line, product, partner);
			
			// create
			if(product.equals(null)) {
				product = line.getProduct();
				createProductMoveLine(partner, product);
				System.out.println("je suis dans la methode create");
			}
			
			// update
			qualityRate = 0;
			conformity = line.getConformitySelect();
			updateProductMoveLine(partner, product, qualityRate, conformity, line);
			
			
		/*		
			if(conformity == StockMoveRepository.CONFORMITY_COMPLIANT) {
				valueConformity++;
			}
		*/
			
		}
		
		/*qualityRating = valueConformity;
		partner.setSupplierQualityRating(qualityRating);
		
		partnerRepository.save(partner);*/
		
	}
	
	
	public Product searchProduct(StockMoveLine line, Product product, Partner partner) {
		
		product = line.getProduct();
		PartnerProductQualityRate partnerProductQualityRate = partnerProductQualityRateRepo.findProductByName(product, partner);
		
		if(partnerProductQualityRate.equals(null)) {
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
	
	
	public PartnerProductQualityRate updateProductMoveLine(Partner partner, Product product, Integer qualityRate, Integer conformity, StockMoveLine line) {

		
		List <PartnerProductQualityRate> partnerProductMoveLines = partner.getPartnerProductQualityRateList();
	
		for(PartnerProductQualityRate partnerProductLine : partnerProductMoveLines) {
			
			if(product == partnerProductLine.getProduct()) {
				
				qualityRate = partnerProductLine.getQualityRate();
				
				Integer partnerProductQualityRate = partnerProductLine.getQualityRate();
				
				if(conformity == StockMoveRepository.CONFORMITY_COMPLIANT) {
					partnerProductQualityRate++;
				}
				
				
				BigDecimal lineQty = partnerProductLinesTotal(line, partnerProductLine);
				

				System.out.println(" == " + lineQty + " == ");
				
				partnerProductLine.setQualityRate(partnerProductQualityRate);
				partnerProductLine.setPartnerProductMoveLineTotal(lineQty);
				
				return partnerProductQualityRateRepo.save(partnerProductLine);
			}
		}
		
		return null;
		
	}
	
	public BigDecimal partnerProductLinesTotal(StockMoveLine line, PartnerProductQualityRate partnerProductLine) {
		
		BigDecimal lineQty = line.getRealQty();
		BigDecimal partnerProductQty = partnerProductLine.getPartnerProductMoveLineTotal();

		lineQty = lineQty.add(partnerProductQty);		
		
		return lineQty;
	}
	

}
