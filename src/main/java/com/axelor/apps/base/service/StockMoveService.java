package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;

import com.google.inject.persist.Transactional;

public class StockMoveService {

	/**
	 * Méthode générique permettant de créer un StockMove.
	 * @param toAddress l'adresse destination
	 * @param company la société
	 * @param clientPartner le tier client
	 * @param refSequence la séquence du StockMove
	 * @return l'objet StockMove
	 */
	@Transactional
	public StockMove createStocksMoves(Address toAddress, Company company, Partner clientPartner, String refSequence, Location location) {

		StockMove stockMove = new StockMove();

		stockMove.setStockMoveSeq(refSequence);
		stockMove.setName(refSequence);
		stockMove.setToAddress(toAddress);
		stockMove.setCompany(company);
		stockMove.setStatusSelect(IStockMove.CONFIRMED);
		stockMove.setRealDate(GeneralService.getTodayDate());
		stockMove.setEstimatedDate(GeneralService.getTodayDate());
		stockMove.setPartner(clientPartner);
		stockMove.setFromLocation(location);

		Location findLocation = Location.all().filter("partner = ?", clientPartner).fetchOne();
		if(findLocation != null) {
			stockMove.setToLocation(findLocation);
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
	public StockMoveLine createStocksMovesLines(Product product, int quantity, StockMove parent) {

		if(product != null && product.getApplicationTypeSelect() == IProduct.PRODUCT_TYPE && product.getProductTypeSelect().equals(IProduct.STOCKABLE)) {
			
			StockMoveLine stockMoveLine = new StockMoveLine();
			stockMoveLine.setStockMove(parent);
			stockMoveLine.setProduct(product);
			stockMoveLine.setQty(quantity);

			stockMoveLine.save();
			return stockMoveLine;
		}
		return null;
	}
}
