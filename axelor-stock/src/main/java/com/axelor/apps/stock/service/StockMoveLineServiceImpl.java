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
package com.axelor.apps.stock.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.joda.time.LocalDate;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.TrackingNumber;
import com.axelor.apps.base.db.TrackingNumberConfiguration;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class StockMoveLineServiceImpl implements StockMoveLineService  {

	@Inject
	private TrackingNumberService trackingNumberService;
	
	@Inject
	protected GeneralService generalService;

	/**
	 * Méthode générique permettant de créer une ligne de mouvement de stock en gérant les numéros de suivi en fonction du type d'opération.
	 * @param product le produit
	 * @param quantity la quantité
	 * @param parent le StockMove parent
	 * @param type
	 * 1 : Sales
	 * 2 : Purchases
	 * 3 : Productions
	 *
	 * @return l'objet StockMoveLine
	 * @throws AxelorException
	 */
	@Override
	public StockMoveLine createStockMoveLine(Product product, String productName, String description, BigDecimal quantity, BigDecimal unitPrice, Unit unit, StockMove stockMove, int type , boolean taxed, BigDecimal taxRate) throws AxelorException {

		if(product != null) {
			BigDecimal unitPriceUntaxed = BigDecimal.ZERO;
			BigDecimal unitPriceTaxed = BigDecimal.ZERO;
			if(taxed){
				unitPriceTaxed = unitPrice.setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
				unitPriceUntaxed = unitPrice.divide(taxRate.add(BigDecimal.ONE), generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
			}
			else{
				unitPriceUntaxed = unitPrice.setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
				unitPriceTaxed = unitPrice.multiply(taxRate.add(BigDecimal.ONE)).setScale(generalService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
			}
			StockMoveLine stockMoveLine = this.createStockMoveLine(product, productName, description, quantity, unitPriceUntaxed, unitPriceTaxed, unit, stockMove, null);

			TrackingNumberConfiguration trackingNumberConfiguration = product.getTrackingNumberConfiguration();
			if(trackingNumberConfiguration != null)  {

				switch (type) {
					case 1:
						if(trackingNumberConfiguration.getIsSaleTrackingManaged())  {
							if(trackingNumberConfiguration.getGenerateSaleAutoTrackingNbr())  {
								// Générer numéro de série si case cochée
								this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getSaleQtyByTracking());

							}
							else  {
								// Rechercher le numéro de suivi d'apèrs FIFO/LIFO
								this.assignTrackingNumber(stockMoveLine, product, stockMove.getFromLocation());
							}
						}
						break;
					case 2:
						if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() && trackingNumberConfiguration.getGeneratePurchaseAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getPurchaseQtyByTracking());

						}
						break;
					case 3:
						if(trackingNumberConfiguration.getIsProductionTrackingManaged() && trackingNumberConfiguration.getGenerateProductionAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getProductionQtyByTracking());

						}
						break;

					default:
						break;
				}
			}
			return stockMoveLine;
		}
		else{
			StockMoveLine stockMoveLine = this.createStockMoveLine(product, productName, description, quantity, BigDecimal.ZERO, BigDecimal.ZERO, unit, stockMove, null);
			return stockMoveLine;
		}
	}


	@Override
	public void generateTrackingNumber(StockMoveLine stockMoveLine, TrackingNumberConfiguration trackingNumberConfiguration, Product product, BigDecimal qtyByTracking) throws AxelorException  {

		StockMove stockMove = stockMoveLine.getStockMove();

		while(stockMoveLine.getQty().compareTo(trackingNumberConfiguration.getSaleQtyByTracking()) == 1)  {

			BigDecimal minQty = stockMoveLine.getQty().min(qtyByTracking);

			this.splitStockMoveLine(stockMoveLine, minQty, trackingNumberService.getTrackingNumber(product, qtyByTracking, stockMove.getCompany(), stockMove.getEstimatedDate()));

		}
		if(stockMoveLine.getTrackingNumber() == null)  {

			stockMoveLine.setTrackingNumber(trackingNumberService.getTrackingNumber(product, qtyByTracking, stockMove.getCompany(), stockMove.getEstimatedDate()));

		}

	}


	/**
	 * Méthode générique permettant de créer une ligne de mouvement de stock
	 * @param product
	 * @param quantity
	 * @param unit
	 * @param price
	 * @param stockMove
	 * @param trackingNumber
	 * @return
	 * @throws AxelorException
	 */
	@Override
	public StockMoveLine createStockMoveLine(Product product, String  productName, String description, BigDecimal quantity, BigDecimal unitPriceUntaxed, BigDecimal unitPriceTaxed, Unit unit, StockMove stockMove, TrackingNumber trackingNumber) throws AxelorException {

		StockMoveLine stockMoveLine = new StockMoveLine();
		stockMoveLine.setStockMove(stockMove);
		stockMoveLine.setProduct(product);
		stockMoveLine.setProductName(productName);
		stockMoveLine.setDescription(description);
		stockMoveLine.setQty(quantity);
		stockMoveLine.setRealQty(quantity);
		stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
		stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
		stockMoveLine.setUnit(unit);
		stockMoveLine.setTrackingNumber(trackingNumber);

		return stockMoveLine;
	}



	@Override
	public void assignTrackingNumber(StockMoveLine stockMoveLine, Product product, Location location) throws AxelorException  {

		List<? extends LocationLine> locationLineList = this.getLocationLines(product, location);

		if(locationLineList != null)  {
			for(LocationLine locationLine : locationLineList)  {

				BigDecimal qty = locationLine.getFutureQty();
				if(stockMoveLine.getQty().compareTo(qty) == 1)  {
					this.splitStockMoveLine(stockMoveLine, qty, locationLine.getTrackingNumber());
				}
				else  {
					stockMoveLine.setTrackingNumber(locationLine.getTrackingNumber());
					break;
				}

			}
		}
	}



	@Override
	public List<? extends LocationLine> getLocationLines(Product product, Location location) throws AxelorException  {

		List<? extends LocationLine> locationLineList = Beans.get(LocationLineRepository.class).all().
				filter("self.product = ?1 AND self.futureQty > 0 AND self.trackingNumber IS NOT NULL AND self.detailsLocation = ?2"
						+trackingNumberService.getOrderMethod(product.getTrackingNumberConfiguration()), product, location).fetch();

		return locationLineList;

	}



	@Override
	public StockMoveLine splitStockMoveLine(StockMoveLine stockMoveLine, BigDecimal qty, TrackingNumber trackingNumber) throws AxelorException  {

		StockMoveLine newStockMoveLine = this.createStockMoveLine(
				stockMoveLine.getProduct(),
				stockMoveLine.getProductName(),
				stockMoveLine.getDescription(),
				qty,
				stockMoveLine.getUnitPriceUntaxed(),
				stockMoveLine.getUnitPriceTaxed(),
				stockMoveLine.getUnit(),
				stockMoveLine.getStockMove(),
				trackingNumber);

		stockMoveLine.getStockMove().getStockMoveLineList().add(newStockMoveLine);

		stockMoveLine.setQty(stockMoveLine.getQty().subtract(qty));

		return newStockMoveLine;
	}



	@Override
	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList,
			LocalDate lastFutureStockMoveDate, boolean realQty) throws AxelorException  {

		for(StockMoveLine stockMoveLine : stockMoveLineList)  {
			if(stockMoveLine.getProduct() != null){
				Unit productUnit = stockMoveLine.getProduct().getUnit();
				Unit stockMoveLineUnit = stockMoveLine.getUnit();

				BigDecimal qty = null;
				if(realQty)  {
					qty = stockMoveLine.getRealQty();
				}
				else  {
					qty = stockMoveLine.getQty();
				}
				
				if(productUnit != null && !productUnit.equals(stockMoveLineUnit))  {
					qty = Beans.get(UnitConversionService.class).convertWithProduct(stockMoveLineUnit, productUnit, qty, stockMoveLine.getProduct());
				}

				this.updateLocations(fromLocation, toLocation, stockMoveLine.getProduct(), qty, fromStatus, toStatus,
						lastFutureStockMoveDate, stockMoveLine.getTrackingNumber());
			}
		}

	}


	@Override
	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate
			lastFutureStockMoveDate, TrackingNumber trackingNumber) throws AxelorException  {

		LocationLineService locationLineService = Beans.get(LocationLineService.class);

		switch(fromStatus)  {
			case StockMoveRepository.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, true, null, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, false, true, false, null, trackingNumber);
				break;

			case StockMoveRepository.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, true, null, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, true, true, false, null, trackingNumber);
				break;

			default:
				break;
		}

		switch(toStatus)  {
			case StockMoveRepository.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, false, lastFutureStockMoveDate, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, false, true, true, lastFutureStockMoveDate, trackingNumber);
				break;

			case StockMoveRepository.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, false, null, trackingNumber);
				locationLineService.updateLocation(toLocation, product, qty, true, true, true, null, trackingNumber);
				break;

			default:
				break;
		}

	}
	
	@Override
	public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove) throws AxelorException{
		BigDecimal unitPriceUntaxed = BigDecimal.ZERO;
		if(stockMoveLine.getProduct() != null && stockMove != null){
			if(stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING){
				unitPriceUntaxed = stockMoveLine.getProduct().getSalePrice();
			}
			else if(stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING){
				unitPriceUntaxed = stockMoveLine.getProduct().getPurchasePrice();
			}
			else{
				unitPriceUntaxed = stockMoveLine.getProduct().getCostPrice();
			}
			
		}
		stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
		stockMoveLine.setUnitPriceTaxed(unitPriceUntaxed);
		return stockMoveLine;
	}


}
