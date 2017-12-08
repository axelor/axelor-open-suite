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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.*;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StockMoveLineServiceImpl implements StockMoveLineService  {

	int generateTrakingNumberCounter = 0;

	@Inject
	private TrackingNumberService trackingNumberService;

	@Inject
	protected AppBaseService appBaseService;

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

		if (product != null) {
			BigDecimal unitPriceUntaxed;
			BigDecimal unitPriceTaxed;
			if(taxed){
				unitPriceTaxed = unitPrice.setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
				unitPriceUntaxed = unitPrice.divide(taxRate.add(BigDecimal.ONE), appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
			}
			else{
				unitPriceUntaxed = unitPrice.setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
				unitPriceTaxed = unitPrice.multiply(taxRate.add(BigDecimal.ONE)).setScale(appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
			}
			StockMoveLine stockMoveLine = this.createStockMoveLine(product, productName, description, quantity, unitPriceUntaxed, unitPriceTaxed, unit, stockMove, null);

			TrackingNumberConfiguration trackingNumberConfiguration = product.getTrackingNumberConfiguration();
			if(trackingNumberConfiguration != null)  {

				switch (type) {
					case StockMoveLineService.TYPE_SALES:
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
					case StockMoveLineService.TYPE_PURCHASES:
						if(trackingNumberConfiguration.getIsPurchaseTrackingManaged() && trackingNumberConfiguration.getGeneratePurchaseAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getPurchaseQtyByTracking());

						}
						break;
					case StockMoveLineService.TYPE_OUT_PRODUCTIONS:
						if(trackingNumberConfiguration.getIsProductionTrackingManaged() && trackingNumberConfiguration.getGenerateProductionAutoTrackingNbr())  {
							// Générer numéro de série si case cochée
							this.generateTrackingNumber(stockMoveLine, trackingNumberConfiguration, product, trackingNumberConfiguration.getProductionQtyByTracking());

						}
						break;
					case StockMoveLineService.TYPE_IN_PRODUCTIONS:
						if (trackingNumberConfiguration.getHasProductAutoSelectTrackingNbr()) {
						    //searching for the tracking number using FIFO or LIFO
							this.assignTrackingNumber(stockMoveLine, product, stockMove.getFromLocation());
						}
						break;
					case StockMoveLineService.TYPE_WASTE_PRODUCTIONS:
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
	public void generateTrackingNumber(StockMoveLine stockMoveLine, TrackingNumberConfiguration trackingNumberConfiguration, Product product, BigDecimal qtyByTracking) throws AxelorException {

		StockMove stockMove = stockMoveLine.getStockMove();

		if (qtyByTracking.compareTo(BigDecimal.ZERO) <= 0) {
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get("The tracking number configuration sale quantity is equal to zero, it must be at least one"));
		}
		while (stockMoveLine.getQty().compareTo(trackingNumberConfiguration.getSaleQtyByTracking()) == 1) {

			BigDecimal minQty = stockMoveLine.getQty().min(qtyByTracking);

			this.splitStockMoveLine(stockMoveLine, minQty, trackingNumberService.getTrackingNumber(product, qtyByTracking, stockMove.getCompany(), stockMove.getEstimatedDate()));

			generateTrakingNumberCounter++;

			if (generateTrakingNumberCounter == 1000) {
				break;
			}
		}
		if (stockMoveLine.getTrackingNumber() == null) {

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
	public StockMoveLine createStockMoveLine(Product product, String  productName, String description, BigDecimal quantity, BigDecimal unitPriceUntaxed, BigDecimal unitPriceTaxed, Unit unit, StockMove stockMove, TrackingNumber trackingNumber) {
        Preconditions.checkArgument(quantity != null && quantity.signum() > 0);

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

		if (product != null) {
			stockMoveLine.setProductTypeSelect(product.getProductTypeSelect());
		}

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

		stockMoveLine.getStockMove().addStockMoveLineListItem(newStockMoveLine);

		stockMoveLine.setQty(stockMoveLine.getQty().subtract(qty));
		stockMoveLine.setRealQty(stockMoveLine.getRealQty().subtract(qty));

		return newStockMoveLine;
	}



	@Override
	public void updateLocations(Location fromLocation, Location toLocation, int fromStatus, int toStatus, List<StockMoveLine> stockMoveLineList,
			LocalDate lastFutureStockMoveDate, boolean realQty) throws AxelorException  {

		for(StockMoveLine stockMoveLine : stockMoveLineList)  {

			Product product = stockMoveLine.getProduct();

			if(product != null && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE))  {
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

				if (toLocation.getTypeSelect() != LocationRepository.TYPE_VIRTUAL)  {
					this.updateAveragePriceLocationLine(toLocation, stockMoveLine, toStatus);
				}
				this.updateLocations(fromLocation, toLocation, stockMoveLine.getProduct(), qty, fromStatus, toStatus,
						lastFutureStockMoveDate, stockMoveLine.getTrackingNumber(), BigDecimal.ZERO);
				Beans.get(LocationServiceImpl.class).computeAvgPriceForProduct(stockMoveLine.getProduct());
			}
		}

	}

	@Override
	public void updateAveragePriceLocationLine(Location location, StockMoveLine stockMoveLine, int toStatus) {
		LocationLine locationLine = Beans.get(LocationLineService.class)
				.getLocationLine(location, stockMoveLine.getProduct());
		if (toStatus == StockMoveRepository.STATUS_REALIZED) {
			this.computeNewAveragePriceLocationLine(locationLine, stockMoveLine);
		}
		else if (toStatus == StockMoveRepository.STATUS_CANCELED) {
			this.cancelAveragePriceLocationLine(locationLine, stockMoveLine);
		}
	}

	protected void computeNewAveragePriceLocationLine(LocationLine locationLine, StockMoveLine stockMoveLine) {
	    int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
		BigDecimal oldAvgPrice = locationLine.getAvgPrice();
		BigDecimal oldQty = locationLine.getCurrentQty();
		BigDecimal newPrice = stockMoveLine.getUnitPriceUntaxed();
		BigDecimal newQty = stockMoveLine.getRealQty();
		BigDecimal newAvgPrice;
		if (oldAvgPrice == null || oldQty == null || oldAvgPrice.compareTo(BigDecimal.ZERO) == 0 || oldQty.compareTo(BigDecimal.ZERO) == 0) {
		    oldAvgPrice = BigDecimal.ZERO;
			oldQty = BigDecimal.ZERO;
		}
		BigDecimal sum = oldAvgPrice.multiply(oldQty);
		sum = sum.add(newPrice.multiply(newQty));
		BigDecimal denominator = oldQty.add(newQty);
		if (denominator.compareTo(BigDecimal.ZERO) != 0) {
			newAvgPrice = sum.divide(denominator, scale, RoundingMode.HALF_UP);
		}
		else {
			newAvgPrice = oldAvgPrice;
		}
        locationLine.setAvgPrice(newAvgPrice);
	}

	protected void cancelAveragePriceLocationLine(LocationLine locationLine, StockMoveLine stockMoveLine) {
		int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
		BigDecimal currentAvgPrice = locationLine.getAvgPrice();
		BigDecimal currentTotalQty = locationLine.getCurrentQty();

		BigDecimal currentLinePrice = stockMoveLine.getUnitPriceUntaxed();
		BigDecimal currentLineQty = stockMoveLine.getRealQty();

		BigDecimal diff = currentTotalQty.multiply(currentAvgPrice).			//(currentAvgPrice*totalQty -
				subtract(currentLinePrice.multiply(currentLineQty));			// currentLinePrice*currentLineQty)
		BigDecimal denominator = currentTotalQty.subtract(currentLineQty);
		BigDecimal updatedAvgPrice;
		if (denominator.compareTo(BigDecimal.ZERO) != 0) {
			updatedAvgPrice = 											// /  (totalQty - currentLineQty)
					diff.divide(denominator, scale, RoundingMode.HALF_UP);
		} else {
			updatedAvgPrice = BigDecimal.ZERO;
		}
		locationLine.setAvgPrice(updatedAvgPrice);
	}

	@Override
    public void checkConformitySelection(StockMoveLine stockMoveLine, StockMove stockMove) throws AxelorException {
        Product product = stockMoveLine.getProduct();
        //check if the product configuration forces to select a conformity
        if ((product == null) || !product.getControlOnReceipt()) {
	    	return;
		}
		//check the stock move type
		if (stockMove.getTypeSelect()
				!= StockMoveRepository.TYPE_INCOMING) {
        	return;
		}

		//check the conformity
		if (stockMoveLine.getConformitySelect() <= StockMoveLineRepository.CONFORMITY_NONE) {
		    throw new AxelorException(stockMoveLine, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_MOVE_LINE_MUST_FILL_CONFORMITY), product.getName());
		}
	}

	@Override
	public void checkConformitySelection(StockMove stockMove) throws AxelorException {
	    List<String> productsWithErrors = new ArrayList<>();
		for(StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

			Product product = stockMoveLine.getProduct();

			if (product != null && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
				try {
					checkConformitySelection(stockMoveLine, stockMove);
				}
				catch (AxelorException e) {
				    productsWithErrors.add(product.getName());
				}
			}
		}
		if (!productsWithErrors.isEmpty()) {
			String productsWithErrorStr = productsWithErrors.stream().collect(Collectors.joining(", "));
			throw new AxelorException(stockMove, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_MOVE_LINE_MUST_FILL_CONFORMITY), productsWithErrorStr);
		}
    }

	@Override
	public void checkExpirationDates(StockMove stockMove) throws AxelorException {
		List<String> errorList = new ArrayList<>();

		for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
			TrackingNumber trackingNumber = stockMoveLine.getTrackingNumber();

			if (trackingNumber == null) {
				continue;
			}

			Product product = trackingNumber.getProduct();

			if (product == null || !product.getCheckExpirationDateAtStockMoveRealization()) {
				continue;
			}

			if (product.getHasWarranty()
					&& trackingNumber.getWarrantyExpirationDate().isBefore(appBaseService.getTodayDate())
					|| product.getIsPerishable()
							&& trackingNumber.getPerishableExpirationDate().isBefore(appBaseService.getTodayDate())) {
				errorList.add(product.getName());
			}
		}

		if (!errorList.isEmpty()) {
			String errorStr = errorList.stream().collect(Collectors.joining(", "));
			throw new AxelorException(IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.STOCK_MOVE_LINE_EXPIRED_PRODUCTS), errorStr);
		}
	}

	@Override
	public void updateLocations(Location fromLocation, Location toLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate
			lastFutureStockMoveDate, TrackingNumber trackingNumber, BigDecimal reservedQty) throws AxelorException  {

		LocationLineService locationLineService = Beans.get(LocationLineService.class);

		switch(fromStatus)  {
			case StockMoveRepository.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, true, null, trackingNumber, reservedQty);
				locationLineService.updateLocation(toLocation, product, qty, false, true, false, null, trackingNumber, reservedQty);
				break;

			case StockMoveRepository.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, true, null, trackingNumber, reservedQty);
				locationLineService.updateLocation(toLocation, product, qty, true, true, false, null, trackingNumber, reservedQty);
				break;

			default:
				break;
		}

		switch(toStatus)  {
			case StockMoveRepository.STATUS_PLANNED:
				locationLineService.updateLocation(fromLocation, product, qty, false, true, false, lastFutureStockMoveDate, trackingNumber, reservedQty);
				locationLineService.updateLocation(toLocation, product, qty, false, true, true, lastFutureStockMoveDate, trackingNumber, reservedQty);
				break;

			case StockMoveRepository.STATUS_REALIZED:
				locationLineService.updateLocation(fromLocation, product, qty, true, true, false, null, trackingNumber, reservedQty);
				locationLineService.updateLocation(toLocation, product, qty, true, true, true, null, trackingNumber, reservedQty);
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

	@Override
	public void storeCustomsCodes(List<StockMoveLine> stockMoveLineList) {
		if (stockMoveLineList == null) {
			return;
		}

		for (StockMoveLine stockMoveLine : stockMoveLineList) {
			Product product = stockMoveLine.getProduct();
			CustomsCodeNomenclature customsCodeNomenclature = product != null ? product.getCustomsCodeNomenclature()
					: null;
			stockMoveLine.setCustomsCodeNomenclature(customsCodeNomenclature);
			stockMoveLine.setCustomsCode(customsCodeNomenclature != null ? customsCodeNomenclature.getCode() : null);
		}
	}


	@Override
	public StockMoveLine getMergedStockMoveLine(List<StockMoveLine> stockMoveLineList) {
		if (stockMoveLineList == null || stockMoveLineList.isEmpty()) {
			return null;
		}

		StockMoveLine firstStockMoveLine = stockMoveLineList.get(0);

		if (stockMoveLineList.size() == 1) {
			return firstStockMoveLine;
		}

		Product product = firstStockMoveLine.getProduct();
		String productName = firstStockMoveLine.getProductName();
		String description = firstStockMoveLine.getDescription();
		BigDecimal quantity = firstStockMoveLine.getQty();
		BigDecimal unitPriceUntaxed = firstStockMoveLine.getUnitPriceUntaxed();
		BigDecimal unitPriceTaxed = firstStockMoveLine.getUnitPriceTaxed();
		Unit unit = firstStockMoveLine.getUnit();
		StockMove stockMove = firstStockMoveLine.getStockMove();
		TrackingNumber trackingNumber = firstStockMoveLine.getTrackingNumber();

		for (StockMoveLine stockMoveLine : stockMoveLineList.subList(1, stockMoveLineList.size())) {
			quantity = quantity.add(stockMoveLine.getQty());
		}

		return createStockMoveLine(product, productName, description, quantity, unitPriceUntaxed, unitPriceTaxed, unit,
				stockMove, trackingNumber);
	}

	@Override
	public boolean computeFullySpreadOverLogisticalFormLinesFlag(StockMoveLine stockMoveLine) {
		return computeSpreadableQtyOverLogisticalFormLines(stockMoveLine).signum() <= 0;
	}

	@Override
	public BigDecimal computeSpreadableQtyOverLogisticalFormLines(StockMoveLine stockMoveLine) {
		return computeSpreadableQtyOverLogisticalFormLines(stockMoveLine, stockMoveLine.getLogisticalFormLineList());
	}

	@Override
	public BigDecimal computeSpreadableQtyOverLogisticalFormLines(StockMoveLine stockMoveLine,
			LogisticalForm logisticalForm) {

		if (logisticalForm == null) {
			return computeSpreadableQtyOverLogisticalFormLines(stockMoveLine,
					stockMoveLine.getLogisticalFormLineList());
		}

		List<LogisticalFormLine> updatedLogisticalFormLineList = new ArrayList<>();

		if (stockMoveLine.getLogisticalFormLineList() != null) {
			for (LogisticalFormLine logisticalFormLine : stockMoveLine.getLogisticalFormLineList()) {
				if (!logisticalForm.equals(logisticalFormLine.getLogisticalForm())) {
					updatedLogisticalFormLineList.add(logisticalFormLine);
				}
			}
		}

		if (logisticalForm.getLogisticalFormLineList() != null) {
			for (LogisticalFormLine logisticalFormLine : logisticalForm.getLogisticalFormLineList()) {
				if (stockMoveLine.equals(logisticalFormLine.getStockMoveLine())) {
					updatedLogisticalFormLineList.add(logisticalFormLine);
				}
			}
		}

		return computeSpreadableQtyOverLogisticalFormLines(stockMoveLine, updatedLogisticalFormLineList);
	}

    private BigDecimal computeSpreadableQtyOverLogisticalFormLines(StockMoveLine stockMoveLine,
            List<LogisticalFormLine> logisticalFormLineList) {
        if (logisticalFormLineList == null) {
            return stockMoveLine.getRealQty();
        }

        BigDecimal qtySpreadOverLogisticalMoveLines = logisticalFormLineList.stream()
                .map(logisticalFormLine -> logisticalFormLine.getQty() != null ? logisticalFormLine.getQty()
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return stockMoveLine.getRealQty().subtract(qtySpreadOverLogisticalMoveLines);
    }

}
