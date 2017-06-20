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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.service.AccountingSituationServiceImpl;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountingSituationSupplychainServiceImpl extends AccountingSituationServiceImpl {

	private SaleConfigService saleConfigService;

	@Inject
	public AccountingSituationSupplychainServiceImpl(AccountConfigService accountConfigService, AccountingSituationRepository accountingSituationRepo,
													 SaleConfigService saleConfigService) {
		super(accountConfigService, accountingSituationRepo);
		this.saleConfigService = saleConfigService;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateUsedCredit(Long partnerId) throws AxelorException {
		List<AccountingSituation> accountingSituationList = accountingSituationRepo.all().filter("self.partner.id = ?1", partnerId).fetch();
		for (AccountingSituation accountingSituation : accountingSituationList) {
			accountingSituationRepo.save(this.computeUsedCredit(accountingSituation));
		}
	}

	public Map<String, Object> updateCustomerCreditFromSaleOrder(Partner partner, SaleOrder saleOrder) throws AxelorException {
		Map<String, Object> map = new HashMap<String, Object>();

		List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();
		for (AccountingSituation accountingSituation : accountingSituationList) {
			if (accountingSituation.getCompany().equals(saleOrder.getCompany())) {
				// Update UsedCredit
				accountingSituation = this.computeUsedCredit(accountingSituation);
				accountingSituation.setUsedCredit(accountingSituation.getUsedCredit().add(saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced())));
				// Update AcceptedCredit
				accountingSituation.setAcceptedCredit(saleConfigService.getSaleConfig(accountingSituation.getCompany()).getAcceptedCredit());

				boolean usedCreditExceeded = isUsedCreditExceeded(accountingSituation);
				map.put("bloqued", usedCreditExceeded);
				if (usedCreditExceeded) {
					if (Strings.isNullOrEmpty(accountingSituation.getCompany().getOrderBloquedMessage())) {
						map.put("message", I18n.get("Client bloqued"));
					} else {
						map.put("message", accountingSituation.getCompany().getOrderBloquedMessage());
					}
				}
			}
		}

		return map;
	}

	public AccountingSituation computeUsedCredit(AccountingSituation accountingSituation) {
		BigDecimal sum = BigDecimal.ZERO;
		List<SaleOrder> saleOrderList = Beans.get(SaleOrderRepository.class)
											 .all()
											 .filter("self.company = ?1 AND self.clientPartner = ?2 AND self.statusSelect > ?3 AND self.statusSelect < ?4", accountingSituation.getCompany(), accountingSituation.getPartner(), ISaleOrder.STATUS_DRAFT, ISaleOrder.STATUS_CANCELED)
											 .fetch();
		for (SaleOrder saleOrder : saleOrderList) {
			sum = sum.add(saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced()));
		}
		sum = accountingSituation.getBalanceCustAccount().add(sum);
		accountingSituation.setUsedCredit(sum.setScale(2, RoundingMode.HALF_EVEN));

		return accountingSituation;
	}

	public boolean isUsedCreditExceeded(AccountingSituation accountingSituation) {
		return accountingSituation.getUsedCredit().compareTo(accountingSituation.getAcceptedCredit()) > 0;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public boolean checkBlockedPartner(Partner partner, Company company) throws AxelorException {
		AccountingSituation accountingSituation = accountingSituationRepo.all().filter("self.company = ?1 AND self.partner = ?2", company, partner).fetchOne();
		accountingSituation = this.computeUsedCredit(accountingSituation);
		accountingSituationRepo.save(accountingSituation);

		return this.isUsedCreditExceeded(accountingSituation);
	}
}
