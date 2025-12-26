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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.ComplementaryProduct;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ComplementaryProductRepository;
import com.axelor.apps.sale.db.repo.PackLineRepository;
import com.axelor.apps.sale.db.repo.SaleConfigRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineCreateService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import wslite.json.JSONException;

public class SaleOrderServiceImpl implements SaleOrderService {

  protected AppBaseService appBaseService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderMarginService saleOrderMarginService;
  protected SaleConfigService saleConfigService;
  protected SaleOrderLineCreateService saleOrderLineCreateService;
  protected SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService;
  protected SaleOrderLinePackService saleOrderLinePackService;
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SaleOrderServiceImpl(
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderRepository saleOrderRepo,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleConfigService saleConfigService,
      SaleOrderLineCreateService saleOrderLineCreateService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.appBaseService = appBaseService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderMarginService = saleOrderMarginService;
    this.saleConfigService = saleConfigService;
    this.saleOrderLineCreateService = saleOrderLineCreateService;
    this.saleOrderLineComplementaryProductService = saleOrderLineComplementaryProductService;
    this.saleOrderLinePackService = saleOrderLinePackService;
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  public String getFileName(SaleOrder saleOrder) {
    String prefixFileName = I18n.get("Sale order");
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION
        || saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      prefixFileName = I18n.get("Sale quotation");
    }
    return prefixFileName
        + " "
        + saleOrder.getSaleOrderSeq()
        + ((Beans.get(AppSaleService.class).getAppSale().getManageSaleOrderVersion()
                && saleOrder.getVersionNumber() > 1)
            ? "-V" + saleOrder.getVersionNumber()
            : "");
  }

  @Override
  public void computeAddressStr(SaleOrder saleOrder) {
    AddressService addressService = Beans.get(AddressService.class);
    saleOrder.setMainInvoicingAddressStr(
        addressService.computeAddressStr(saleOrder.getMainInvoicingAddress()));
    saleOrder.setDeliveryAddressStr(
        addressService.computeAddressStr(saleOrder.getDeliveryAddress()));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean enableEditOrder(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_COMPLETED) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALES_ORDER_COMPLETED));
    }

    saleOrder.setOrderBeingEdited(true);
    return false;
  }

  @Override
  public void checkModifiedConfirmedOrder(SaleOrder saleOrder, SaleOrder saleOrderView)
      throws AxelorException {
    // Nothing to check if we don't have supplychain.
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateChanges(SaleOrder saleOrder) throws AxelorException {
    checkUnauthorizedDiscounts(saleOrder);
  }

  @Override
  public void sortSaleOrderLineList(SaleOrder saleOrder) {
    if (saleOrder.getSaleOrderLineList() != null) {
      saleOrder.getSaleOrderLineList().sort(Comparator.comparing(SaleOrderLine::getSequence));
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public SaleOrder addPack(SaleOrder saleOrder, Pack pack, BigDecimal packQty)
      throws AxelorException, MalformedURLException, JSONException {

    List<PackLine> packLineList = pack.getComponents();
    if (ObjectUtils.isEmpty(packLineList)) {
      return saleOrder;
    }
    packLineList.sort(Comparator.comparing(PackLine::getSequence));
    Integer sequence = -1;

    List<SaleOrderLine> soLines = saleOrder.getSaleOrderLineList();
    if (soLines != null && !soLines.isEmpty()) {
      sequence = soLines.stream().mapToInt(SaleOrderLine::getSequence).max().getAsInt();
    }

    if (Boolean.FALSE.equals(pack.getDoNotDisplayHeaderAndEndPack())) {
      if (saleOrderLinePackService.getPackLineTypes(packLineList) == null
          || !saleOrderLinePackService
              .getPackLineTypes(packLineList)
              .contains(PackLineRepository.TYPE_START_OF_PACK)) {
        sequence++;
      }
      soLines =
          saleOrderLinePackService.createNonStandardSOLineFromPack(
              pack, saleOrder, packQty, soLines, sequence);
    }

    boolean doNotDisplayHeaderAndEndPack =
        Boolean.TRUE.equals(pack.getDoNotDisplayHeaderAndEndPack());
    SaleOrderLine soLine;
    for (PackLine packLine : packLineList) {
      if (doNotDisplayHeaderAndEndPack
          && (Objects.equals(packLine.getTypeSelect(), PackLineRepository.TYPE_START_OF_PACK)
              || Objects.equals(packLine.getTypeSelect(), PackLineRepository.TYPE_END_OF_PACK))) {
        continue;
      }
      soLine =
          saleOrderLineCreateService.createSaleOrderLine(
              packLine, saleOrder, packQty, BigDecimal.ONE, ++sequence);
      if (soLine != null) {
        soLine.setSaleOrder(saleOrder);
        soLines.add(soLine);
      }
    }

    if (soLines != null && !soLines.isEmpty()) {
      saleOrderLineComputeService.computeLevels(saleOrder.getSaleOrderLineList(), null);
      saleOrder = saleOrderComputeService.computeSaleOrder(saleOrder);
      saleOrderMarginService.computeMarginSaleOrder(saleOrder);
      saleOrderRepo.save(saleOrder);
    }
    return saleOrder;
  }

  @Override
  public void checkUnauthorizedDiscounts(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        BigDecimal maxDiscountAuthorized =
            saleOrderLineDiscountService.computeMaxDiscount(saleOrder, saleOrderLine);
        if (saleOrderLine.getDiscountDerogation() != null && maxDiscountAuthorized != null) {
          maxDiscountAuthorized = saleOrderLine.getDiscountDerogation().max(maxDiscountAuthorized);
        }
        if (maxDiscountAuthorized != null
            && saleOrderLineDiscountService.isSaleOrderLineDiscountGreaterThanMaxDiscount(
                saleOrderLine, maxDiscountAuthorized)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SaleExceptionMessage.SALE_ORDER_DISCOUNT_TOO_HIGH));
        }
      }
    }
  }

  @Transactional(rollbackOn = Exception.class)
  public SaleOrder separateInNewQuotation(
      SaleOrder saleOrder, ArrayList<LinkedHashMap<String, Object>> saleOrderLines)
      throws AxelorException {

    saleOrder = saleOrderRepo.find(saleOrder.getId());
    List<SaleOrderLine> originalSOLines = saleOrder.getSaleOrderLineList();

    SaleOrder copySaleOrder = saleOrderRepo.copy(saleOrder, true);
    copySaleOrder.clearSaleOrderLineList();
    saleOrderRepo.save(copySaleOrder);

    for (LinkedHashMap<String, Object> soLine : saleOrderLines) {
      if (!soLine.containsKey("selected") || !(boolean) soLine.get("selected")) {
        continue;
      }

      SaleOrderLine saleOrderLine =
          saleOrderLineRepo.find(Long.parseLong(soLine.get("id").toString()));
      List<SaleOrderLine> separatedSOLines = new ArrayList<>();
      separatedSOLines.add(saleOrderLine);
      separatedSOLines.addAll(
          originalSOLines.stream()
              .filter(
                  soline ->
                      StringUtils.notBlank(saleOrderLine.getManualId())
                          && saleOrderLine.getManualId().equals(soline.getParentId()))
              .collect(Collectors.toList()));
      manageSeparatedSOLines(separatedSOLines, originalSOLines, copySaleOrder);
    }

    copySaleOrder = saleOrderComputeService.computeSaleOrder(copySaleOrder);
    saleOrderRepo.save(copySaleOrder);

    // refresh the origin sale order to refresh the field saleOrderLineList
    JPA.refresh(saleOrder);

    saleOrder = saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepo.save(saleOrder);

    return copySaleOrder;
  }

  protected void manageSeparatedSOLines(
      List<SaleOrderLine> separatedSOLines,
      List<SaleOrderLine> originalSOLines,
      SaleOrder copySaleOrder) {

    for (SaleOrderLine separatedLine : separatedSOLines) {
      copySaleOrder.addSaleOrderLineListItem(separatedLine);
      originalSOLines.stream()
          .filter(soLine -> separatedLine.equals(soLine.getMainSaleOrderLine()))
          .forEach(copySaleOrder::addSaleOrderLineListItem);
    }
  }

  @Override
  public void manageComplementaryProductSOLines(SaleOrder saleOrder) throws AxelorException {

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrder.getClientPartner() == null) {
      return;
    }
    List<ComplementaryProduct> complementaryProducts =
        saleOrder.getClientPartner().getComplementaryProductList();

    if (CollectionUtils.isEmpty(saleOrderLineList)
        || CollectionUtils.isEmpty(complementaryProducts)) {
      return;
    }

    List<SaleOrderLine> newComplementarySOLines = new ArrayList<>();
    for (ComplementaryProduct complementaryProduct : complementaryProducts) {
      Product product = complementaryProduct.getProduct();
      if (product == null) {
        continue;
      }

      if (complementaryProduct.getGenerationTypeSelect()
          == ComplementaryProductRepository.GENERATION_TYPE_SALE_ORDER) {
        SaleOrderLine saleOrderLine =
            Collections.max(saleOrderLineList, Comparator.comparing(SaleOrderLine::getSequence));
        if (saleOrderLineList.stream()
            .anyMatch(
                line ->
                    product.equals(line.getProduct())
                        && line.getIsComplementaryPartnerProductsHandled())) {
          continue;
        }
        newComplementarySOLines.addAll(
            saleOrderLineComplementaryProductService.manageComplementaryProductSaleOrderLine(
                complementaryProduct, saleOrder, saleOrderLine));
      } else {
        for (SaleOrderLine saleOrderLine : saleOrderLineList) {
          newComplementarySOLines.addAll(
              saleOrderLineComplementaryProductService.manageComplementaryProductSaleOrderLine(
                  complementaryProduct, saleOrder, saleOrderLine));
        }
      }
    }
    newComplementarySOLines.forEach(saleOrder::addSaleOrderLineListItem);
    saleOrderComputeService.computeSaleOrder(saleOrder);
  }

  @Override
  public boolean isIncotermRequired(SaleOrder saleOrder) {
    return false;
  }

  @Override
  public void checkPrintingSettings(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder.getPrintingSettings() == null) {
      if (saleOrder.getCompany().getPrintingSettings() != null) {
        saleOrder.setPrintingSettings(saleOrder.getCompany().getPrintingSettings());
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            String.format(
                I18n.get(SaleExceptionMessage.SALE_ORDER_MISSING_PRINTING_SETTINGS),
                saleOrder.getSaleOrderSeq()),
            saleOrder);
      }
    }
  }

  @Override
  public boolean getInAti(SaleOrder saleOrder, Company company) throws AxelorException {
    if (company == null) {
      return false;
    }
    SaleConfig saleConfig = saleConfigService.getSaleConfig(company);
    int saleOrderInAtiSelect = saleConfig.getSaleOrderInAtiSelect();
    return saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_ALWAYS
        || saleOrderInAtiSelect == SaleConfigRepository.SALE_ATI_DEFAULT;
  }
}
