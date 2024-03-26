/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Set;

public class PartnerSupplychainServiceImpl extends PartnerSaleServiceImpl
    implements PartnerSupplychainService {

  private InvoiceRepository invoiceRepository;

  private CompanyRepository companyRepository;
  private AccountConfigService accountConfigService;

  @Inject
  public PartnerSupplychainServiceImpl(
      PartnerRepository partnerRepo,
      CompanyRepository companyRepository,
      AppBaseService appBaseService,
      InvoiceRepository invoiceRepository,
      AccountConfigService accountConfigService) {
    super(partnerRepo, appBaseService);
    this.invoiceRepository = invoiceRepository;
    this.accountConfigService = accountConfigService;
    this.companyRepository = companyRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void updateBlockedAccount(Partner partner) throws AxelorException {

    long temp = 0;

    Set<Company> partnerCompany = partner.getCompanySet();

    for (Company company : partnerCompany) {
      temp =
          invoiceRepository
              .all()
              .filter(
                  "self.operationTypeSelect = :operationTypeSelect "
                      + "AND self.amountRemaining > 0 "
                      + "AND self.partner = :partner "
                      + "AND self.dueDate < :date "
                      + "AND self.company = :company")
              .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
              .bind("partner", partner.getId())
              .bind(
                  "date",
                  appBaseService
                      .getTodayDate(company)
                      .plusDays(
                          accountConfigService
                              .getAccountConfig(company)
                              .getNumberOfDaysBeforeAccountBlocking()))
              .bind("company", company)
              .order("id")
              .count();
      if (temp > 0) break;
    }
    if (temp > 0) {
      partner.setHasBlockedAccount(true);
      partnerRepo.save(partner);
      return;
    }
    partner.setHasBlockedAccount(false);
    partnerRepo.save(partner);
    return;
  }

  @Override
  public boolean isBlockedPartnerOrParent(Partner partner) {
    if (partner.getHasBlockedAccount()) {
      return true;
    }
    if (partner.getParentPartner() != null) {
      return isBlockedPartnerOrParent(partner.getParentPartner());
    }
    return false;
  }
}
