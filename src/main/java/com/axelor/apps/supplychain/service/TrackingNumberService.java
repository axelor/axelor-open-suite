package com.axelor.apps.supplychain.service;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.TrackingNumberConfiguration;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;

public class TrackingNumberService {
	
	private static final Logger LOG = LoggerFactory.getLogger(TrackingNumberService.class); 

	@Inject
	private SequenceService sequenceService;
	
	private String exceptionMsg;
	
	@Inject
	public TrackingNumberService() {

		this.exceptionMsg = GeneralService.getExceptionAccountingMsg();
		
	}
	
	
	public TrackingNumber getTrackingNumber(Product product, int sizeOfLot, Company company, LocalDate date) throws AxelorException  {
		
		TrackingNumber trackingNumber = TrackingNumber.all().filter("self.product = ?1 and self.counter < ?2", product, sizeOfLot).fetchOne();
	
		if(trackingNumber == null)  {
			trackingNumber = this.createTrackingNumber(product, company, date);
		}
		
		trackingNumber.setCounter(trackingNumber.getCounter()+1);
		
		return trackingNumber;
		
	}
	
	
	
	public String getOrderMethod(TrackingNumberConfiguration trackingNumberConfiguration)  {
		switch (trackingNumberConfiguration.getSaleAutoTrackingNbrOrderSelect()) {
			case IProduct.FIFO:
				return " ORDER BY self.trackingNumber ASC";
				
			case IProduct.LIFO:
				return " ORDER BY self.trackingNumber DESC";
	
			default:
				return "";
		}
	}
	
	

	public TrackingNumber createTrackingNumber(Product product, Company company, LocalDate date) throws AxelorException  {
		
		TrackingNumber trackingNumber = new TrackingNumber();
		
		if(product.getIsPerishable())  {
			trackingNumber.setPerishableExpirationDate(date.plusMonths(product.getPerishableNbrOfMonths()));
		}
		if(product.getHasWarranty())  {
			trackingNumber.setWarrantyExpirationDate(date.plusMonths(product.getWarrantyNbrOfMonths()));
		}
		
		trackingNumber.setProduct(product);
		trackingNumber.setCounter(0);
		
		String seq = sequenceService.getSequence(IAdministration.PRODUCT_TRACKING_NUMBER, product, company, false);
		if (seq == null)  {
			throw new AxelorException(String.format("%s Aucune séquence configurée pour les Numéros de suivi pour la société %s et le produit %s ",
					exceptionMsg, company.getName(), product.getCode()), IException.CONFIGURATION_ERROR);
		}
		
		trackingNumber.setTrackingNumberSeq(seq);
		
		return trackingNumber;
	}
	
	
	
	
	
}
