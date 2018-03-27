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
package com.axelor.apps.supplychain.service;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineServiceImpl;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.tool.QueryBuilder;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderLineServiceSupplyChainImpl extends SaleOrderLineServiceImpl implements SaleOrderLineServiceSupplyChain {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	protected AppAccountService appAccountService;

	@Inject
	protected AnalyticMoveLineService analyticMoveLineService;

	@Override
	public void computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
	    super.computeProductInformation(saleOrderLine, saleOrder);
		saleOrderLine.setSaleSupplySelect(saleOrderLine.getProduct().getSaleSupplySelect());
    }

	@Override
	public BigDecimal computeAmount(SaleOrderLine saleOrderLine) {

		BigDecimal price = this.computeDiscount(saleOrderLine);


		BigDecimal amount = saleOrderLine.getQty().multiply(price).setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { saleOrderLine.getQty(), price, amount });

		return amount;
	}


	public SaleOrderLine computeAnalyticDistribution(SaleOrderLine saleOrderLine) throws AxelorException{

		if(appAccountService.getAppAccount().getAnalyticDistributionTypeSelect() == AppAccountRepository.DISTRIBUTION_TYPE_FREE)  {  return saleOrderLine;  }

		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		List<AnalyticMoveLine> analyticMoveLineList = saleOrderLine.getAnalyticMoveLineList();
		if((analyticMoveLineList == null || analyticMoveLineList.isEmpty()))  {
			analyticMoveLineList = analyticMoveLineService.generateLines(saleOrder.getClientPartner(), saleOrderLine.getProduct(), saleOrder.getCompany(), saleOrderLine.getExTaxTotal());
			saleOrderLine.setAnalyticMoveLineList(analyticMoveLineList);
		}
		if(analyticMoveLineList != null)  {
			for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
				this.updateAnalyticMoveLine(analyticMoveLine, saleOrderLine);
			}
		}
		return saleOrderLine;
	}

	public void updateAnalyticMoveLine(AnalyticMoveLine analyticMoveLine, SaleOrderLine saleOrderLine)  {

		analyticMoveLine.setSaleOrderLine(saleOrderLine);
		analyticMoveLine.setAmount(analyticMoveLineService.computeAmount(analyticMoveLine));
		analyticMoveLine.setDate(appAccountService.getTodayDate());
		analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);

	}

	public SaleOrderLine createAnalyticDistributionWithTemplate(SaleOrderLine saleOrderLine) throws AxelorException{
		List<AnalyticMoveLine> analyticMoveLineList = null;
		analyticMoveLineList = analyticMoveLineService.generateLinesWithTemplate(saleOrderLine.getAnalyticDistributionTemplate(), saleOrderLine.getExTaxTotal());
		if(analyticMoveLineList != null)  {
			for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList)  {
				analyticMoveLine.setSaleOrderLine(saleOrderLine);
			}
		}
		saleOrderLine.setAnalyticMoveLineList(analyticMoveLineList);
		return saleOrderLine;
	}

	@Override
	public BigDecimal getAvailableStock(SaleOrderLine saleOrderLine) {
		QueryBuilder<StockLocationLine> queryBuilder = QueryBuilder.of(StockLocationLine.class);
		queryBuilder.add("self.stockLocation = :stockLocation");
		queryBuilder.add("self.product = :product");
		queryBuilder.bind("stockLocation", saleOrderLine.getSaleOrder().getStockLocation());
		queryBuilder.bind("product", saleOrderLine.getProduct());
		StockLocationLine stockLocationLine = queryBuilder.build().fetchOne();
		if (stockLocationLine == null) {
			return BigDecimal.ZERO;
		}

		return stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
	}

	@Transactional
	public void changeReservedQty(SaleOrderLine saleOrderLine, BigDecimal reservedQty) {
	    saleOrderLine.setReservedQty(reservedQty);
	    Beans.get(SaleOrderLineRepository.class).save(saleOrderLine);
	}

    @Override
    public BigDecimal computeUndeliveredQty(SaleOrderLine saleOrderLine) {
        Preconditions.checkNotNull(saleOrderLine);
        SaleOrder saleOrder = saleOrderLine.getSaleOrder();
        Preconditions.checkNotNull(saleOrder);
        Product product = saleOrderLine.getProduct();
        BigDecimal deliveredQty = product != null
                ? saleOrder.getSaleOrderLineList().stream().filter(line -> product.equals(line.getProduct()))
                        .reduce(BigDecimal.ZERO, (qty, line) -> qty.add(line.getDeliveredQty()), BigDecimal::add)
                : BigDecimal.ZERO;

        return saleOrderLine.getQty().subtract(deliveredQty);
    }

    @Override
	public List<Long> getSupplierPartnerList(SaleOrderLine saleOrderLine) {
	    Product product = saleOrderLine.getProduct();
	    if (product == null || product.getSupplierCatalogList() == null) {
	    	return new ArrayList<>();
		}
		return product.getSupplierCatalogList()
				.stream()
				.map(SupplierCatalog::getSupplierPartner)
				.filter(Objects::nonNull)
				.map(Partner::getId)
				.collect(Collectors.toList());
	}

}
