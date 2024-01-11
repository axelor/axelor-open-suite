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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.currency.CurrencyConversionFactory;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.ComplementaryProduct;
import com.axelor.apps.sale.db.ComplementaryProductSelected;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ComplementaryProductRepository;
import com.axelor.apps.sale.db.repo.PackLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.JpaSequence;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

  protected SaleOrderLineService saleOrderLineService;
  protected AppBaseService appBaseService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderMarginService saleOrderMarginService;

  @Inject
  public SaleOrderServiceImpl(
      SaleOrderLineService saleOrderLineService,
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderRepository saleOrderRepo,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService) {
    this.saleOrderLineService = saleOrderLineService;
    this.appBaseService = appBaseService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderMarginService = saleOrderMarginService;
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
  public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder) {
    Company company = saleOrder.getCompany();
    if (saleOrder.getDuration() == null && company != null && company.getSaleConfig() != null) {
      saleOrder.setDuration(company.getSaleConfig().getDefaultValidityDuration());
    }
    if (saleOrder.getCreationDate() != null) {
      saleOrder.setEndOfValidityDate(
          Beans.get(DurationService.class)
              .computeDuration(saleOrder.getDuration(), saleOrder.getCreationDate()));
    }
    return saleOrder;
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
      throws AxelorException {

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

    BigDecimal conversionRate = BigDecimal.valueOf(1.00);
    if (pack.getCurrency() != null
        && !pack.getCurrency().getCodeISO().equals(saleOrder.getCurrency().getCodeISO())) {
      try {
        conversionRate =
            Beans.get(CurrencyConversionFactory.class)
                .getCurrencyConversionService()
                .convert(pack.getCurrency(), saleOrder.getCurrency());
      } catch (MalformedURLException | JSONException | AxelorException e) {
        TraceBackService.trace(e);
      }
    }

    if (Boolean.FALSE.equals(pack.getDoNotDisplayHeaderAndEndPack())) {
      if (saleOrderLineService.getPackLineTypes(packLineList) == null
          || !saleOrderLineService
              .getPackLineTypes(packLineList)
              .contains(PackLineRepository.TYPE_START_OF_PACK)) {
        sequence++;
      }
      soLines =
          saleOrderLineService.createNonStandardSOLineFromPack(
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
          saleOrderLineService.createSaleOrderLine(
              packLine, saleOrder, packQty, conversionRate, ++sequence);
      if (soLine != null) {
        soLine.setSaleOrder(saleOrder);
        soLines.add(soLine);
      }
    }

    if (soLines != null && !soLines.isEmpty()) {
      try {
        saleOrder = saleOrderComputeService.computeSaleOrder(saleOrder);
        saleOrderMarginService.computeMarginSaleOrder(saleOrder);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }

      saleOrderRepo.save(saleOrder);
    }
    return saleOrder;
  }

  @Override
  public List<SaleOrderLine> handleComplementaryProducts(SaleOrder saleOrder)
      throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList == null) {
      saleOrderLineList = new ArrayList<>();
    }

    SaleOrderLine originSoLine = null;
    for (SaleOrderLine soLine : saleOrderLineList) {
      if (soLine.getIsComplementaryProductsUnhandledYet()) {
        originSoLine = soLine;
        if (originSoLine.getManualId() == null || originSoLine.getManualId().equals("")) {
          this.setNewManualId(originSoLine);
        }
        break;
      }
    }

    if (originSoLine != null
        && originSoLine.getProduct() != null
        && originSoLine.getSelectedComplementaryProductList() != null) {
      for (ComplementaryProductSelected compProductSelected :
          originSoLine.getSelectedComplementaryProductList()) {
        // Search if there is already a line for this product to modify or remove
        SaleOrderLine newSoLine = null;
        for (SaleOrderLine soLine : saleOrderLineList) {
          if (originSoLine.getManualId().equals(soLine.getParentId())
              && soLine.getProduct() != null
              && soLine.getProduct().equals(compProductSelected.getProduct())) {
            // Edit line if it already exists instead of recreating, otherwise remove if already
            // exists and is no longer selected
            if (compProductSelected.getIsSelected()) {
              newSoLine = soLine;
            } else {
              saleOrderLineList.remove(soLine);
            }
            break;
          }
        }

        if (newSoLine == null) {
          if (compProductSelected.getIsSelected()) {
            newSoLine = new SaleOrderLine();
            newSoLine.setProduct(compProductSelected.getProduct());
            newSoLine.setSaleOrder(saleOrder);
            newSoLine.setQty(
                originSoLine
                    .getQty()
                    .multiply(compProductSelected.getQty())
                    .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN));

            saleOrderLineService.computeProductInformation(newSoLine, newSoLine.getSaleOrder());
            saleOrderLineService.computeValues(newSoLine.getSaleOrder(), newSoLine);

            newSoLine.setParentId(originSoLine.getManualId());

            int targetIndex = saleOrderLineList.indexOf(originSoLine) + 1;
            saleOrderLineList.add(targetIndex, newSoLine);
          }
        } else {
          newSoLine.setQty(
              originSoLine
                  .getQty()
                  .multiply(compProductSelected.getQty())
                  .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN));

          saleOrderLineService.computeProductInformation(newSoLine, newSoLine.getSaleOrder());
          saleOrderLineService.computeValues(newSoLine.getSaleOrder(), newSoLine);
        }
      }
      originSoLine.setIsComplementaryProductsUnhandledYet(false);
    }

    for (int i = 0; i < saleOrderLineList.size(); i++) {
      saleOrderLineList.get(i).setSequence(i);
    }

    return saleOrderLineList;
  }

  @Override
  public void checkUnauthorizedDiscounts(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (saleOrderLineList != null) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        BigDecimal maxDiscountAuthorized =
            saleOrderLineService.computeMaxDiscount(saleOrder, saleOrderLine);
        if (saleOrderLine.getDiscountDerogation() != null && maxDiscountAuthorized != null) {
          maxDiscountAuthorized = saleOrderLine.getDiscountDerogation().max(maxDiscountAuthorized);
        }
        if (maxDiscountAuthorized != null
            && saleOrderLineService.isSaleOrderLineDiscountGreaterThanMaxDiscount(
                saleOrderLine, maxDiscountAuthorized)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SaleExceptionMessage.SALE_ORDER_DISCOUNT_TOO_HIGH));
        }
      }
    }
  }

  @Transactional
  protected void setNewManualId(SaleOrderLine saleOrderLine) {
    saleOrderLine.setManualId(JpaSequence.nextValue("sale.order.line.idSeq"));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder updateProductQtyWithPackHeaderQty(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    boolean isStartOfPack = false;
    BigDecimal newQty = BigDecimal.ZERO;
    BigDecimal oldQty = BigDecimal.ZERO;
    this.sortSaleOrderLineList(saleOrder);

    for (SaleOrderLine SOLine : saleOrderLineList) {

      if (SOLine.getTypeSelect() == SaleOrderLineRepository.TYPE_START_OF_PACK && !isStartOfPack) {
        newQty = SOLine.getQty();
        oldQty = saleOrderLineRepo.find(SOLine.getId()).getQty();
        if (newQty.compareTo(oldQty) != 0) {
          isStartOfPack = true;
          SOLine = EntityHelper.getEntity(SOLine);
          saleOrderLineRepo.save(SOLine);
        }
      } else if (isStartOfPack) {
        if (SOLine.getTypeSelect() == SaleOrderLineRepository.TYPE_END_OF_PACK) {
          break;
        }
        saleOrderLineService.updateProductQty(SOLine, saleOrder, oldQty, newQty);
      }
    }
    return saleOrder;
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
            saleOrderLineService.manageComplementaryProductSaleOrderLine(
                complementaryProduct, saleOrder, saleOrderLine));
      } else {
        for (SaleOrderLine saleOrderLine : saleOrderLineList) {
          newComplementarySOLines.addAll(
              saleOrderLineService.manageComplementaryProductSaleOrderLine(
                  complementaryProduct, saleOrder, saleOrderLine));
        }
      }
    }
    newComplementarySOLines.forEach(saleOrder::addSaleOrderLineListItem);
    saleOrderComputeService.computeSaleOrder(saleOrder);
  }
}
