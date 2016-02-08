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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class ManufOrderStockMoveService {

	@Inject
	private StockMoveService stockMoveService;

	@Inject
	private StockMoveLineService stockMoveLineService;

	@Inject
	private ProductionConfigService productionConfigService;

	@Inject
	private LocationRepository locationRepo;

	private static final Logger log = LoggerFactory.getLogger(ManufOrderStockMoveService.class);

	public void createToConsumeStockMove(ManufOrder manufOrder) throws AxelorException {

		Company company = manufOrder.getCompany();

		if(manufOrder.getToConsumeProdProductList() != null && company != null) {

			StockMove stockMove = this._createToConsumeStockMove(manufOrder, company);

			for(ProdProduct prodProduct: manufOrder.getToConsumeProdProductList()) {

				StockMoveLine stockMoveLine = this._createStockMoveLine(prodProduct);
				stockMove.addStockMoveLineListItem(stockMoveLine);
				manufOrder.addConsumedStockMoveLineListItem(stockMoveLine);

			}

			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMove.setExTaxTotal(stockMoveService.compute(stockMove));
				stockMoveService.plan(stockMove);
				manufOrder.setInStockMove(stockMove);
			}
		}

	}


	private StockMove _createToConsumeStockMove(ManufOrder manufOrder, Company company) throws AxelorException  {

		Location virtualLocation = productionConfigService.getProductionVirtualLocation(productionConfigService.getProductionConfig(company));

		Location fromLocation = null;

		if(manufOrder.getProdProcess() != null && manufOrder.getProdProcess().getLocation() != null)  {

			fromLocation = manufOrder.getProdProcess().getLocation();
		}
		else  {
			fromLocation = locationRepo.all().filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3",
					company, true, LocationRepository.TYPE_INTERNAL).fetchOne();
		}

		StockMove stockMove = stockMoveService.createStockMove(
				null,
				null,
				company,
				null,
				fromLocation,
				virtualLocation,
				manufOrder.getPlannedStartDateT().toLocalDate(),
				null);

		return stockMove;
	}


	public void createToProduceStockMove(ManufOrder manufOrder) throws AxelorException {

		Company company = manufOrder.getCompany();

		if(manufOrder.getToProduceProdProductList() != null && company != null) {

			StockMove stockMove = this._createToProduceStockMove(manufOrder, company);

			for(ProdProduct prodProduct: manufOrder.getToProduceProdProductList()) {

				StockMoveLine stockMoveLine = this._createStockMoveLine(prodProduct);
				stockMove.addStockMoveLineListItem(stockMoveLine);
				manufOrder.addProducedStockMoveLineListItem(stockMoveLine);

			}

			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMove.setExTaxTotal(stockMoveService.compute(stockMove));
				stockMoveService.plan(stockMove);
				manufOrder.setOutStockMove(stockMove);
			}
		}

	}


	private StockMove _createToProduceStockMove(ManufOrder manufOrder, Company company) throws AxelorException  {

		Location virtualLocation = productionConfigService.getProductionVirtualLocation(productionConfigService.getProductionConfig(company));

		LocalDateTime plannedEndDateT = manufOrder.getPlannedEndDateT();
		LocalDate plannedEndDate = plannedEndDateT != null ? plannedEndDateT.toLocalDate() : null;

		StockMove stockMove = stockMoveService.createStockMove(
				null,
				null,
				company,
				null,
				virtualLocation,
				manufOrder.getProdProcess().getLocation(),
				plannedEndDate,
				null);

		return stockMove;
	}


	private StockMoveLine _createStockMoveLine(ProdProduct prodProduct) throws AxelorException  {

		return stockMoveLineService.createStockMoveLine(
				prodProduct.getProduct(),
				prodProduct.getProduct().getName(),
				prodProduct.getProduct().getDescription(),
				prodProduct.getQty(),
				prodProduct.getProduct().getCostPrice(),
				prodProduct.getUnit(),
				null,
				StockMoveLineService.TYPE_PRODUCTIONS, false, BigDecimal.ZERO);

	}


	public void finish(ManufOrder manufOrder) throws AxelorException  {

		this.finishStockMove(manufOrder.getInStockMove());
		this.finishStockMove(manufOrder.getOutStockMove());

	}


	public void finishStockMove(StockMove stockMove) throws AxelorException  {

		if(stockMove != null && stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)  {

			stockMoveService.copyQtyToRealQty(stockMove);
			stockMoveService.realize(stockMove);

		}
	}


	public void cancel(ManufOrder manufOrder) throws AxelorException  {

		this.cancel(manufOrder.getInStockMove());
		this.cancel(manufOrder.getOutStockMove());

	}


	public void cancel(StockMove stockMove) throws AxelorException  {

		if(stockMove != null)  {

			stockMoveService.cancel(stockMove);

			for(StockMoveLine stockMoveLine : stockMove.getStockMoveLineList())  {

				stockMoveLine.setProducedManufOrder(null);

			}

		}

	}

}
