/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLinePriceService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineProductService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSale;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.axelor.team.db.Team;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderCreateServiceImpl implements SaleOrderCreateService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PartnerService partnerService;
  protected SaleOrderRepository saleOrderRepo;
  protected AppSaleService appSaleService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderLineProductService saleOrderLineProductService;
  protected SaleOrderLinePriceService saleOrderLinePriceService;
  protected SaleOrderDateService saleOrderDateService;
  protected final SubSaleOrderLineComputeService subSaleOrderLineComputeService;

  @Inject
  public SaleOrderCreateServiceImpl(
      PartnerService partnerService,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderLinePriceService saleOrderLinePriceService,
      SaleOrderDateService saleOrderDateService,
      SubSaleOrderLineComputeService subSaleOrderLineComputeService) {
    this.partnerService = partnerService;
    this.saleOrderRepo = saleOrderRepo;
    this.appSaleService = appSaleService;
    this.saleOrderService = saleOrderService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
    this.saleOrderDateService = saleOrderDateService;
    this.subSaleOrderLineComputeService = subSaleOrderLineComputeService;
  }

  @Override
  public SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate estimatedShippingDate,
      String internalReference,
      String externalReference,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      String internalNote,
      FiscalPosition fiscalPosition,
      TradingName tradingName)
      throws AxelorException {
    SaleOrder saleOrder =
        createSaleOrder(
            salespersonUser,
            company,
            contactPartner,
            currency,
            estimatedShippingDate,
            internalReference,
            externalReference,
            priceList,
            clientPartner,
            team,
            taxNumber,
            fiscalPosition,
            tradingName);
    saleOrder.setInternalNote(internalNote);
    saleOrder.setTradingName(tradingName);
    return saleOrder;
  }

  @Override
  public SaleOrder createSaleOrder(
      User salespersonUser,
      Company company,
      Partner contactPartner,
      Currency currency,
      LocalDate estimatedShippingDate,
      String internalReference,
      String externalReference,
      PriceList priceList,
      Partner clientPartner,
      Team team,
      TaxNumber taxNumber,
      FiscalPosition fiscalPosition,
      TradingName tradingName)
      throws AxelorException {

    logger.debug(
        "Creation of a sale order: Company = {},  External reference = {}, Supplier partner = {}",
        company,
        externalReference,
        clientPartner.getFullName());

    SaleOrder saleOrder = new SaleOrder();
    saleOrder.setClientPartner(clientPartner);
    saleOrder.setCreationDate(appSaleService.getTodayDate(company));
    saleOrder.setContactPartner(contactPartner);
    saleOrder.setCurrency(currency);
    saleOrder.setExternalReference(externalReference);
    saleOrder.setEstimatedShippingDate(estimatedShippingDate);
    saleOrder.setEstimatedDeliveryDate(estimatedShippingDate);
    saleOrder.setTaxNumber(taxNumber);
    saleOrder.setFiscalPosition(fiscalPosition);

    saleOrder.setPrintingSettings(
        Beans.get(TradingNameService.class).getDefaultPrintingSettings(tradingName, company));

    if (salespersonUser == null) {
      salespersonUser = AuthUtils.getUser();
    }
    saleOrder.setSalespersonUser(salespersonUser);

    if (team == null) {
      team = salespersonUser.getActiveTeam();
    }
    saleOrder.setTeam(team);

    if (company == null) {
      company = salespersonUser.getActiveCompany();
    }
    saleOrder.setCompany(company);

    saleOrder.setMainInvoicingAddress(partnerService.getInvoicingAddress(clientPartner));
    saleOrder.setDeliveryAddress(partnerService.getDeliveryAddress(clientPartner));

    saleOrderService.computeAddressStr(saleOrder);

    if (priceList == null) {
      priceList =
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(clientPartner, PriceListRepository.TYPE_SALE);
    }
    saleOrder.setPriceList(priceList);

    saleOrder.setSaleOrderLineList(new ArrayList<>());

    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);

    saleOrderDateService.computeEndOfValidityDate(saleOrder);

    return saleOrder;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder createSaleOrder(
      SaleOrder context, Currency wizardCurrency, PriceList wizardPriceList)
      throws AxelorException {
    SaleOrder copy = saleOrderRepo.copy(context, true);
    copy.setCreationDate(appSaleService.getTodayDate(context.getCompany()));
    copy.setCurrency(wizardCurrency);
    copy.setPriceList(wizardPriceList);

    saleOrderDateService.computeEndOfValidityDate(copy);

    this.updateSaleOrderLineList(copy);

    saleOrderComputeService.computeSaleOrder(copy);

    copy.setTemplate(false);
    copy.setTemplateUser(null);

    return copy;
  }

  public void updateSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        if (saleOrderLine.getProduct() != null) {
          if (!saleOrder.getTemplate()) {
            saleOrderLinePriceService.resetPrice(saleOrderLine);
          }
          AppSale appSale = appSaleService.getAppSale();
          if (appSale.getIsSOLPriceTotalOfSubLines()
              && appSale.getListDisplayTypeSelect()
                  == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
              && CollectionUtils.isNotEmpty(saleOrderLine.getSubSaleOrderLineList())) {
            subSaleOrderLineComputeService.computeSumSubLineList(saleOrderLine, saleOrder);
          } else {
            saleOrderLineProductService.fillPrice(saleOrderLine, saleOrder);
            saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);
          }
        }
      }
    }
  }

  @Override
  @Transactional
  public SaleOrder createTemplate(SaleOrder context) {
    SaleOrder copy = saleOrderRepo.copy(context, true);
    copy.setTemplate(true);
    copy.setTemplateUser(AuthUtils.getUser());
    return copy;
  }
}
