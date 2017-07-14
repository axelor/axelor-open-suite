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
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class AccountingSituationSupplychainServiceImpl extends AccountingSituationServiceImpl implements AccountingSituationSupplychainService {

	private SaleConfigService saleConfigService;

	@Inject
	public AccountingSituationSupplychainServiceImpl(AccountConfigService accountConfigService, AccountingSituationRepository accountingSituationRepo,
													 SaleConfigService saleConfigService) {
		super(accountConfigService, accountingSituationRepo);
		this.saleConfigService = saleConfigService;
	}
	
	@Override
	public AccountingSituation createAccountingSituation(Company company) throws AxelorException {
		
		AccountingSituation accountingSituation = super.createAccountingSituation(company);
		SaleConfig config = saleConfigService.getSaleConfig(accountingSituation.getCompany());
		if (config != null) {
			accountingSituation.setAcceptedCredit(config.getAcceptedCredit());
		}
		
		return accountingSituation;
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateUsedCredit(Partner partner) throws AxelorException {
		List<AccountingSituation> accountingSituationList = accountingSituationRepo.all().filter("self.partner = ?1", partner).fetch();
		for (AccountingSituation accountingSituation : accountingSituationList) {
			accountingSituationRepo.save(this.computeUsedCredit(accountingSituation));
		}
	}
	
	@Transactional
	public void updateCustomerCreditFromSaleOrder(SaleOrder saleOrder) throws AxelorException {
		
		Partner partner = saleOrder.getClientPartner();
		List<AccountingSituation> accountingSituationList = partner.getAccountingSituationList();
		for (AccountingSituation accountingSituation : accountingSituationList) {
			if (accountingSituation.getCompany().equals(saleOrder.getCompany())) {
				// Update UsedCredit
				accountingSituation = this.computeUsedCredit(accountingSituation);
				if (saleOrder.getStatusSelect() == 1) {
					accountingSituation.setUsedCredit(accountingSituation.getUsedCredit().add(saleOrder.getExTaxTotal().subtract(saleOrder.getAmountInvoiced())));
				}
				boolean usedCreditExceeded = isUsedCreditExceeded(accountingSituation);
				if (usedCreditExceeded) {
					saleOrder.setBloqued(true);	// No rollback
					if (!saleOrder.getManualUnblock()) {
						String message = accountingSituation.getCompany().getOrderBloquedMessage();
						if (Strings.isNullOrEmpty(message)) {
							message = I18n.get("Client bloqued");
						}
						throw new AxelorException(message, IException.CONFIGURATION_ERROR);
					}
				}
			}
		}

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

	private boolean isUsedCreditExceeded(AccountingSituation accountingSituation) {
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
