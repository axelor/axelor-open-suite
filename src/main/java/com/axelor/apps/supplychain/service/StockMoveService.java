package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.ILocation;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveService {

	@Inject
	SequenceService sequenceService;
	
	/**
	 * Méthode permettant d'obtenir le code de la séquence du StockMove en fonction des Locations.
	 * @param fromLocation Location de départ
	 * @param toLocation Location d'arrivée
	 * @return code de la séquence
	 */
	public String getCodeSequence(Location fromLocation, Location toLocation) {
		if(fromLocation.getTypeSelect() == ILocation.INTERNAL && toLocation.getTypeSelect() == ILocation.INTERNAL) {
			return IAdministration.INTERNAL;
		}
		else if(fromLocation.getTypeSelect() == ILocation.SUPPLIER && toLocation.getTypeSelect() == ILocation.INTERNAL) {	
			return IAdministration.INCOMING;
		}
		else if(fromLocation.getTypeSelect() == ILocation.INTERNAL && toLocation.getTypeSelect() == ILocation.CUSTOMER) {
			return IAdministration.OUTGOING;
		}
		return "";
	}
	
	/**
	 * Méthode permettant d'obtenir la séquence du StockMove.
	 * @param fromLocation Location de départ
	 * @param toLocation Location d'arrivée
	 * @param company la société
	 * @return la chaine contenant la séquence du StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public String getSequenceStockMove(Location fromLocation, Location toLocation, Company company) throws AxelorException {

		String ref = "";
		String code = getCodeSequence(fromLocation, toLocation);
		
		if(code.equals(IAdministration.INTERNAL)) {
			ref = sequenceService.getSequence(IAdministration.INTERNAL, company, null, false);
		}
		else if(code.equals(IAdministration.INCOMING)) {	
			ref = sequenceService.getSequence(IAdministration.INCOMING, company, null, false);
		}
		else if(code.equals(IAdministration.OUTGOING)) {
			ref = sequenceService.getSequence(IAdministration.OUTGOING, company, null, false);
		}
		
		if (ref == null || ref.isEmpty() || ref.equals(""))
			throw new AxelorException("Aucune séquence configurée pour les livraisons de la société "+company.getName()+" avec le code "+code, IException.CONFIGURATION_ERROR);
		return ref;
	}
	
	/**
	 * Méthode générique permettant de créer un StockMove.
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @param refSequence la séquence du StockMove
	 * @return l'objet StockMove
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	@Transactional
	public StockMove createStocksMoves(Address toAddress, Company company, Partner clientPartner, Location location) throws AxelorException {

		StockMove stockMove = new StockMove();
		stockMove.setToAddress(toAddress);
		stockMove.setCompany(company);
		stockMove.setStatusSelect(IStockMove.CONFIRMED);
		stockMove.setRealDate(GeneralService.getTodayDate());
		stockMove.setEstimatedDate(GeneralService.getTodayDate());
		stockMove.setPartner(clientPartner);
		stockMove.setFromLocation(location);
		// Find the location depending on the partner
		Location findLocation = Location.all().filter("partner = ?", clientPartner).fetchOne();
		if(findLocation != null) {
			stockMove.setToLocation(findLocation);
		}
		
		Location fromLocation = stockMove.getFromLocation();
		Location toLocation = stockMove.getToLocation();
		// Set the sequence
		String refSequence = getSequenceStockMove(fromLocation, toLocation, company);
		stockMove.setStockMoveSeq(refSequence);
		stockMove.setName(refSequence);
		// Set the type select
		String codeSequence = getCodeSequence(fromLocation, toLocation);
		if(toLocation != null && codeSequence.equals(IAdministration.INTERNAL)) {
			stockMove.setTypeSelect(IStockMove.INTERNAL);
		}
		else if(toLocation != null && codeSequence.equals(IAdministration.INCOMING)) {	
			stockMove.setTypeSelect(IStockMove.INCOMING);
		}
		else if(toLocation != null && codeSequence.equals(IAdministration.OUTGOING)) {
			stockMove.setTypeSelect(IStockMove.OUTGOING);
		}

		stockMove.save();
		return stockMove;
	}

	/**
	 * Méthode générique permettant de créer un StockMoveLine.
	 * @param product le produit
	 * @param quantity la quantité
	 * @param parent le StockMove parent
	 * @return l'objet StockMoveLine
	 */
	@Transactional
	public StockMoveLine createStocksMovesLines(Product product, int quantity, Unit unit, BigDecimal price, StockMove parent) {

		if(product != null && product.getApplicationTypeSelect() == IProduct.PRODUCT_TYPE && product.getProductTypeSelect().equals(IProduct.STOCKABLE)) {
			
			StockMoveLine stockMoveLine = new StockMoveLine();
			stockMoveLine.setStockMove(parent);
			stockMoveLine.setProduct(product);
			stockMoveLine.setQty(quantity);
			stockMoveLine.setUnit(unit);
			stockMoveLine.setPrice(price);
			stockMoveLine.save();
			return stockMoveLine;
		}
		return null;
	}
}
