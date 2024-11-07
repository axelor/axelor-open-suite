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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.service.PartnerSaleServiceImpl;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PartnerSupplychainServiceImpl extends PartnerSaleServiceImpl
    implements PartnerSupplychainService {

  private InvoiceRepository invoiceRepository;
  private AccountConfigService accountConfigService;
  public PartnerService partnerService;

  @Inject
  public PartnerSupplychainServiceImpl(
      PartnerRepository partnerRepo,
      AppBaseService appBaseService,
      PartnerService partnerService,
      InvoiceRepository invoiceRepository,
      AccountConfigService accountConfigService) {
    super(partnerRepo, appBaseService);
    this.partnerService = partnerService;
    this.invoiceRepository = invoiceRepository;
    this.accountConfigService = accountConfigService;
  }

  @Override
  @Transactional
  public void updateBlockedAccount(Partner partner) {

    long invoiceCount =
        invoiceRepository
            .all()
            .filter(
                "self.operationTypeSelect = :operationTypeSelect "
                    + "AND self.amountRemaining > 0 "
                    + "AND self.partner = :partner "
                    + "AND self.statusSelect = :invoiceStatusVentilated "
                    + "AND self.dueDate < "
                    + "(SELECT DATE(:todayDate) + config.numberOfDaysBeforeAccountBlocking "
                    + "FROM AccountConfig config WHERE config.company = self.company)")
            .bind("operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE)
            .bind("partner", partner.getId())
            .bind("invoiceStatusVentilated", InvoiceRepository.STATUS_VENTILATED)
            .bind("todayDate", appBaseService.getTodayDate(null))
            .count();

    partner.setHasBlockedAccount(invoiceCount > 0);
    partnerRepo.save(partner);
  }

  @Override
  public boolean isBlockedPartnerOrParent(Partner partner) throws AxelorException {
    if (partner.getHasBlockedAccount()) {
      return true;
    }
    if (partner.getParentPartner() != null) {
      if (!partnerService.getParentPartnerList(partner).contains(partner.getParentPartner())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(SupplychainExceptionMessage.PARENT_PARTNER_CONFIGURATION_ERROR),
                partner.getFullName()));
      }
      return isBlockedPartnerOrParent(partner.getParentPartner());
    }
    return false;
  }
}
