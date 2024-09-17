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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.i18n.I18n;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderGeneratorServiceImpl implements SaleOrderGeneratorService {
  protected SaleOrderRepository saleOrderRepository;
  protected AppSaleService appSaleService;
  protected SaleConfigService saleConfigService;
  protected CompanyService companyService;
  protected SaleOrderInitValueService saleOrderInitValueService;
  protected SaleOrderOnChangeService saleOrderOnChangeService;
  protected SaleOrderDomainService saleOrderDomainService;
  protected PartnerRepository partnerRepository;

  protected SaleConfigRepository saleConfigRepository;

  @Inject
  public SaleOrderGeneratorServiceImpl(
      SaleOrderRepository saleOrderRepository,
      AppSaleService appSaleService,
      CompanyService companyService,
      SaleOrderInitValueService saleOrderInitValueService,
      SaleOrderOnChangeService saleOrderOnChangeService,
      SaleOrderDomainService saleOrderDomainService,
      PartnerRepository partnerRepository,
      SaleConfigService saleConfigService,
      SaleConfigRepository saleConfigRepository) {
    this.saleOrderRepository = saleOrderRepository;
    this.appSaleService = appSaleService;
    this.companyService = companyService;
    this.saleOrderInitValueService = saleOrderInitValueService;
    this.saleOrderOnChangeService = saleOrderOnChangeService;
    this.saleOrderDomainService = saleOrderDomainService;
    this.partnerRepository = partnerRepository;
    this.saleConfigService = saleConfigService;
    this.saleConfigRepository = saleConfigRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrder createSaleOrder(
      Partner clientPartner,
      Company company,
      Partner contactPartner,
      Currency currency,
      Boolean inAti)
      throws AxelorException, JsonProcessingException {
    SaleOrder saleOrder = new SaleOrder();
    boolean isTemplate = false;
    saleOrderInitValueService.setIsTemplate(saleOrder, isTemplate);
    saleOrderInitValueService.getOnNewInitValues(saleOrder);
    if (company != null) {
      saleOrder.setCompany(company);
      saleOrderOnChangeService.companyOnChange(saleOrder);
    }
    checkClientPartner(clientPartner, saleOrder);
    saleOrder.setClientPartner(clientPartner);
    saleOrderOnChangeService.partnerOnChange(saleOrder);
    if (contactPartner != null) {
      checkContact(clientPartner, contactPartner);
      saleOrder.setContactPartner(contactPartner);
    }
    if (currency != null) {
      saleOrder.setCurrency(currency);
    }
    setInAti(inAti, saleOrder);
    saleOrderRepository.save(saleOrder);
    return saleOrder;
  }

  protected void checkClientPartner(Partner clientPartner, SaleOrder saleOrder)
      throws AxelorException {
    Company company = saleOrder.getCompany();
    String domain = saleOrderDomainService.getPartnerBaseDomain(saleOrder.getCompany());
    if (!partnerRepository
        .all()
        .filter(domain)
        .bind("company", company)
        .fetch()
        .contains(clientPartner)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CLIENT_PROVIDED_DOES_NOT_RESPECT_DOMAIN_RESTRICTIONS),
          company.getName());
    }
  }

  protected void checkContact(Partner clientPartner, Partner contactPartner)
      throws AxelorException {
    if (!clientPartner.getContactPartnerSet().contains(contactPartner)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.CONTACT_PROVIDED_DOES_NOT_RESPECT_DOMAIN_RESTRICTIONS));
    }
  }

  protected void setInAti(Boolean inAti, SaleOrder saleOrder) throws AxelorException {
    if (inAti != null) {
      checkinAti(saleOrder);
      saleOrder.setInAti(inAti);
    }
  }

  protected void checkinAti(SaleOrder saleOrder) throws AxelorException {

    SaleConfig saleConfig = saleConfigService.getSaleConfig(saleOrder.getCompany());

    int saleOrderInAtiSelect = saleConfig.getSaleOrderInAtiSelect();

    if (saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_ALWAYS
        || saleOrderInAtiSelect == SaleConfigRepository.SALE_WT_ALWAYS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.ATI_CHANGE_NOT_ALLOWED));
    }
  }
}
