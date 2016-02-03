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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AnalyticDistributionLine;
import com.axelor.apps.account.service.AnalyticDistributionLineService;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.SaleOrderLineServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class SaleOrderLineServiceSupplyChainImpl extends SaleOrderLineServiceImpl  {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderLineServiceSupplyChainImpl.class);


	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected AnalyticDistributionLineService analyticDistributionLineService;

	@Override
	public BigDecimal computeAmount(SaleOrderLine saleOrderLine) {

		BigDecimal price = this.computeDiscount(saleOrderLine);


		BigDecimal amount = saleOrderLine.getQty().multiply(price).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);

		if (ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(saleOrderLine.getProduct().getProductTypeSelect())) {
			if(saleOrderLine.getSubscriptionList() != null
				&& !saleOrderLine.getSubscriptionList().isEmpty()){
				amount = amount.multiply(new BigDecimal(saleOrderLine.getSubscriptionList().size())).setScale(IAdministration.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_EVEN);
			}
			else  {
				amount = BigDecimal.ZERO;
			}
		}

		LOG.debug("Calcul du montant HT avec une quantité de {} pour {} : {}", new Object[] { saleOrderLine.getQty(), price, amount });

		return amount;
	}
	
	
	public SaleOrderLine computeAnalyticDistribution(SaleOrderLine saleOrderLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = saleOrderLine.getAnalyticDistributionLineList();
		if((analyticDistributionLineList == null || analyticDistributionLineList.isEmpty()) && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			analyticDistributionLineList = analyticDistributionLineService.generateLines(saleOrderLine.getSaleOrder().getClientPartner(), saleOrderLine.getProduct(), saleOrderLine.getSaleOrder().getCompany(), saleOrderLine.getExTaxTotal());
			if(analyticDistributionLineList != null){
				for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
					analyticDistributionLine.setSaleOrderLine(saleOrderLine);
					analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
					analyticDistributionLine.setDate(generalService.getTodayDate());
				}
				saleOrderLine.setAnalyticDistributionLineList(analyticDistributionLineList);
			}
		}
		if(analyticDistributionLineList != null && generalService.getGeneral().getAnalyticDistributionTypeSelect() != GeneralRepository.DISTRIBUTION_TYPE_FREE){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setSaleOrderLine(saleOrderLine);
				analyticDistributionLine.setAmount(analyticDistributionLineService.computeAmount(analyticDistributionLine));
				analyticDistributionLine.setDate(generalService.getTodayDate());
			}
		}
		return saleOrderLine;
	}
	
	public SaleOrderLine createAnalyticDistributionWithTemplate(SaleOrderLine saleOrderLine) throws AxelorException{
		List<AnalyticDistributionLine> analyticDistributionLineList = null;
		analyticDistributionLineList = analyticDistributionLineService.generateLinesWithTemplate(saleOrderLine.getAnalyticDistributionTemplate(), saleOrderLine.getExTaxTotal());
		if(analyticDistributionLineList != null){
			for (AnalyticDistributionLine analyticDistributionLine : analyticDistributionLineList) {
				analyticDistributionLine.setSaleOrderLine(saleOrderLine);
			}
		}
		saleOrderLine.setAnalyticDistributionLineList(analyticDistributionLineList);
		return saleOrderLine;
	}

}
