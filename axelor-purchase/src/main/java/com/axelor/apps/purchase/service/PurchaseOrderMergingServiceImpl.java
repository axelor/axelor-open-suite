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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class PurchaseOrderMergingServiceImpl implements PurchaseOrderMergingService {

  protected static class CommonFieldsImpl implements CommonFields {

    private Company commonCompany = null;
    private Currency commonCurrency = null;
    private Partner commonSupplierPartner = null;
    private Partner commonContactPartner = null;
    private PriceList commonPriceList = null;
    private TradingName commonTradingName = null;
    private FiscalPosition commonFiscalPosition = null;
    private boolean allTradingNamesAreNull = true;
    private boolean allFiscalPositionsAreNull = true;

    @Override
    public Company getCommonCompany() {
      return commonCompany;
    }

    @Override
    public void setCommonCompany(Company commonCompany) {
      this.commonCompany = commonCompany;
    }

    @Override
    public Currency getCommonCurrency() {
      return commonCurrency;
    }

    @Override
    public void setCommonCurrency(Currency commonCurrency) {
      this.commonCurrency = commonCurrency;
    }

    @Override
    public Partner getCommonSupplierPartner() {
      return this.commonSupplierPartner;
    }

    @Override
    public void setCommonSupplierPartner(Partner commonSupplierPartner) {
      this.commonSupplierPartner = commonSupplierPartner;
    }

    @Override
    public Partner getCommonContactPartner() {
      return commonContactPartner;
    }

    @Override
    public void setCommonContactPartner(Partner commonContactPartner) {
      this.commonContactPartner = commonContactPartner;
    }

    @Override
    public PriceList getCommonPriceList() {
      return commonPriceList;
    }

    @Override
    public void setCommonPriceList(PriceList commonPriceList) {
      this.commonPriceList = commonPriceList;
    }

    @Override
    public TradingName getCommonTradingName() {
      return commonTradingName;
    }

    @Override
    public void setCommonTradingName(TradingName commonTradingName) {
      this.commonTradingName = commonTradingName;
    }

    @Override
    public FiscalPosition getCommonFiscalPosition() {
      return commonFiscalPosition;
    }

    @Override
    public void setCommonFiscalPosition(FiscalPosition commonFiscalPosition) {
      this.commonFiscalPosition = commonFiscalPosition;
    }

    @Override
    public boolean getAllTradingNamesAreNull() {
      return allTradingNamesAreNull;
    }

    @Override
    public void setAllTradingNamesAreNull(boolean allTradingNamesAreNull) {
      this.allTradingNamesAreNull = allTradingNamesAreNull;
    }

    @Override
    public boolean getAllFiscalPositionsAreNull() {
      return allFiscalPositionsAreNull;
    }

    @Override
    public void setAllFiscalPositionsAreNull(boolean allFiscalPositionsAreNull) {
      this.allFiscalPositionsAreNull = allFiscalPositionsAreNull;
    }
  }

  protected static class ChecksImpl implements Checks {

    private boolean existCurrencyDiff = false;
    private boolean existCompanyDiff = false;
    private boolean existSupplierPartnerDiff = false;
    private boolean existContactPartnerDiff = false;
    private boolean existPriceListDiff = false;
    private boolean existTradingNameDiff = false;
    private boolean existFiscalPositionDiff = false;

    @Override
    public boolean isExistCurrencyDiff() {
      return existCurrencyDiff;
    }

    @Override
    public void setExistCurrencyDiff(boolean existCurrencyDiff) {
      this.existCurrencyDiff = existCurrencyDiff;
    }

    @Override
    public boolean isExistCompanyDiff() {
      return existCompanyDiff;
    }

    @Override
    public void setExistCompanyDiff(boolean existCompanyDiff) {
      this.existCompanyDiff = existCompanyDiff;
    }

    @Override
    public boolean isExistSupplierPartnerDiff() {
      return existSupplierPartnerDiff;
    }

    @Override
    public void setExistSupplierPartnerDiff(boolean existSupplierPartnerDiff) {
      this.existSupplierPartnerDiff = existSupplierPartnerDiff;
    }

    @Override
    public boolean isExistContactPartnerDiff() {
      return existContactPartnerDiff;
    }

    @Override
    public void setExistContactPartnerDiff(boolean existContactPartnerDiff) {
      this.existContactPartnerDiff = existContactPartnerDiff;
    }

    @Override
    public boolean isExistPriceListDiff() {
      return existPriceListDiff;
    }

    @Override
    public void setExistPriceListDiff(boolean existPriceListDiff) {
      this.existPriceListDiff = existPriceListDiff;
    }

    @Override
    public boolean isExistTradingNameDiff() {
      return existTradingNameDiff;
    }

    @Override
    public void setExistTradingNameDiff(boolean existTradingNameDiff) {
      this.existTradingNameDiff = existTradingNameDiff;
    }

    @Override
    public boolean isExistFiscalPositionDiff() {
      return existFiscalPositionDiff;
    }

    @Override
    public void setExistFiscalPositionDiff(boolean existFiscalPositionDiff) {
      this.existFiscalPositionDiff = existFiscalPositionDiff;
    }
  }

  protected static class PurchaseOrderMergingResultImpl implements PurchaseOrderMergingResult {

    private PurchaseOrder purchaseOrder;
    private boolean isConfirmationNeeded;
    private final CommonFieldsImpl commonFields;
    private final ChecksImpl checks;

    public PurchaseOrderMergingResultImpl() {
      this.purchaseOrder = null;
      this.isConfirmationNeeded = false;
      this.commonFields = new CommonFieldsImpl();
      this.checks = new ChecksImpl();
    }

    public PurchaseOrder getPurchaseOrder() {
      return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
      this.purchaseOrder = purchaseOrder;
    }

    @Override
    public void needConfirmation() {
      this.isConfirmationNeeded = true;
    }

    @Override
    public boolean isConfirmationNeeded() {
      return isConfirmationNeeded;
    }
  }

  protected PurchaseOrderService purchaseOrderService;

  protected PurchaseOrderRepository purchaseOrderRepository;

  @Inject
  public PurchaseOrderMergingServiceImpl(
      PurchaseOrderService purchaseOrderService, PurchaseOrderRepository purchaseOrderRepository) {
    this.purchaseOrderService = purchaseOrderService;
    this.purchaseOrderRepository = purchaseOrderRepository;
  }

  @Override
  public PurchaseOrderMergingResultImpl create() {
    return new PurchaseOrderMergingResultImpl();
  }

  @Override
  public CommonFieldsImpl getCommonFields(PurchaseOrderMergingResult result) {
    return ((PurchaseOrderMergingResultImpl) result).commonFields;
  }

  @Override
  public ChecksImpl getChecks(PurchaseOrderMergingResult result) {
    return ((PurchaseOrderMergingResultImpl) result).checks;
  }

  @Override
  public PurchaseOrderMergingResult mergePurchaseOrders(List<PurchaseOrder> purchaseOrdersToMerge)
      throws AxelorException {
    Objects.requireNonNull(purchaseOrdersToMerge);
    PurchaseOrderMergingResult result = controlPurchaseOrdersToMerge(purchaseOrdersToMerge);

    if (isConfirmationNeeded(result)) {
      result.needConfirmation();
      return result;
    }
    result.setPurchaseOrder(mergePurchaseOrders(purchaseOrdersToMerge, result));
    return result;
  }

  @Override
  public PurchaseOrderMergingResult mergePurchaseOrdersWithContext(
      List<PurchaseOrder> purchaseOrdersToMerge, Context context) throws AxelorException {
    Objects.requireNonNull(purchaseOrdersToMerge);
    Objects.requireNonNull(context);

    PurchaseOrderMergingResult result = controlPurchaseOrdersToMerge(purchaseOrdersToMerge);
    updateResultWithContext(result, context);
    result.setPurchaseOrder(mergePurchaseOrders(purchaseOrdersToMerge, result));
    return result;
  }

  protected void updateResultWithContext(PurchaseOrderMergingResult result, Context context) {
    if (context.get("priceList") != null) {
      getCommonFields(result)
          .setCommonPriceList(MapHelper.get(context, PriceList.class, "priceList"));
    }
    if (context.get("contactPartner") != null) {
      getCommonFields(result)
          .setCommonContactPartner(MapHelper.get(context, Partner.class, "contactPartner"));
    }
  }

  protected PurchaseOrderMergingResult controlPurchaseOrdersToMerge(
      List<PurchaseOrder> purchaseOrdersToMerge) throws AxelorException {
    PurchaseOrderMergingResult result = create();

    if (purchaseOrdersToMerge.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_LIST_EMPTY));
    }

    fillCommonFields(purchaseOrdersToMerge, result);
    purchaseOrdersToMerge.forEach(
        purchaseOrder -> {
          updateDiffsCommonFields(purchaseOrder, result);
        });

    StringJoiner fieldErrors = new StringJoiner("<BR/>");
    checkErrors(fieldErrors, result);
    if (fieldErrors.length() > 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, fieldErrors.toString());
    }
    return result;
  }

  protected boolean isConfirmationNeeded(PurchaseOrderMergingResult result) {
    return getChecks(result).isExistContactPartnerDiff()
        || getChecks(result).isExistPriceListDiff();
  }

  protected PurchaseOrder mergePurchaseOrders(
      List<PurchaseOrder> purchaseOrdersToMerge, PurchaseOrderMergingResult result)
      throws AxelorException {
    PurchaseOrder purchaseOrder =
        purchaseOrderService.mergePurchaseOrders(
            purchaseOrdersToMerge,
            getCommonFields(result).getCommonCurrency(),
            getCommonFields(result).getCommonSupplierPartner(),
            getCommonFields(result).getCommonCompany(),
            getCommonFields(result).getCommonContactPartner(),
            getCommonFields(result).getCommonPriceList(),
            getCommonFields(result).getCommonTradingName());
    purchaseOrder.setFiscalPosition(getCommonFields(result).getCommonFiscalPosition());
    return purchaseOrder;
  }

  protected void checkErrors(StringJoiner fieldErrors, PurchaseOrderMergingResult result) {
    if (getChecks(result).isExistCurrencyDiff()
        || getCommonFields(result).getCommonCurrency() == null) {
      fieldErrors.add(I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_CURRENCY));
    }
    if (getChecks(result).isExistSupplierPartnerDiff()
        || getCommonFields(result).getCommonSupplierPartner() == null) {
      fieldErrors.add(
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER));
    }
    if (getChecks(result).isExistCompanyDiff()
        || getCommonFields(result).getCommonCompany() == null) {
      fieldErrors.add(I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_COMPANY));
    }
    if ((getChecks(result).isExistTradingNameDiff()
            || getCommonFields(result).getCommonTradingName() == null)
        && !getCommonFields(result).getAllTradingNamesAreNull()) {
      fieldErrors.add(I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_TRADING_NAME));
    }
    if ((getChecks(result).isExistFiscalPositionDiff()
            || getCommonFields(result).getCommonFiscalPosition() == null)
        && !getCommonFields(result).getAllFiscalPositionsAreNull()) {
      fieldErrors.add(
          I18n.get(PurchaseExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_FISCAL_POSITION));
    }
  }

  protected void updateDiffsCommonFields(
      PurchaseOrder purchaseOrder, PurchaseOrderMergingResult result) {
    CommonFields commonFields = getCommonFields(result);
    Checks checks = getChecks(result);
    if (commonFields.getCommonCurrency() != null
        && !getCommonFields(result).getCommonCurrency().equals(purchaseOrder.getCurrency())) {
      commonFields.setCommonCurrency(null);
      checks.setExistCurrencyDiff(true);
    }
    if (commonFields.getCommonSupplierPartner() != null
        && !getCommonFields(result)
            .getCommonSupplierPartner()
            .equals(purchaseOrder.getSupplierPartner())) {
      commonFields.setCommonSupplierPartner(null);
      checks.setExistSupplierPartnerDiff(true);
    }
    if (commonFields.getCommonCompany() != null
        && !getCommonFields(result).getCommonCompany().equals(purchaseOrder.getCompany())) {
      commonFields.setCommonCompany(null);
      checks.setExistCompanyDiff(true);
    }
    if (commonFields.getCommonContactPartner() != null
        && !getCommonFields(result)
            .getCommonContactPartner()
            .equals(purchaseOrder.getContactPartner())) {
      commonFields.setCommonContactPartner(null);
      checks.setExistContactPartnerDiff(true);
    }
    if (commonFields.getCommonPriceList() != null
        && !getCommonFields(result).getCommonPriceList().equals(purchaseOrder.getPriceList())) {
      commonFields.setCommonPriceList(null);
      checks.setExistPriceListDiff(true);
    }
    if (commonFields.getCommonTradingName() != null
        && !getCommonFields(result).getCommonTradingName().equals(purchaseOrder.getTradingName())) {
      commonFields.setCommonTradingName(null);
      commonFields.setAllTradingNamesAreNull(false);
      checks.setExistTradingNameDiff(true);
    }
    if (commonFields.getCommonFiscalPosition() != null
        && !getCommonFields(result)
            .getCommonFiscalPosition()
            .equals(purchaseOrder.getFiscalPosition())) {
      commonFields.setCommonFiscalPosition(null);
      commonFields.setAllFiscalPositionsAreNull(false);
      checks.setExistFiscalPositionDiff(true);
    }
  }

  protected void fillCommonFields(
      List<PurchaseOrder> purchaseOrdersToMerge, PurchaseOrderMergingResult result) {
    CommonFields commonFields = getCommonFields(result);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getCompany)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonCompany);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getCurrency)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonCurrency);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getContactPartner)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonContactPartner);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getPriceList)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonPriceList);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getSupplierPartner)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonSupplierPartner);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getTradingName)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonTradingName);
    commonFields.setAllTradingNamesAreNull(commonFields.getCommonTradingName() == null);
    purchaseOrdersToMerge.stream()
        .map(PurchaseOrder::getFiscalPosition)
        .filter(Objects::nonNull)
        .findFirst()
        .ifPresent(commonFields::setCommonFiscalPosition);
    commonFields.setAllFiscalPositionsAreNull(commonFields.getCommonFiscalPosition() == null);
  }

  @Override
  public List<PurchaseOrder> convertSelectedLinesToMergeLines(List<Integer> idList) {
    return Optional.ofNullable(idList)
        .map(
            list ->
                list.stream()
                    .map(id -> purchaseOrderRepository.find(Long.valueOf(id)))
                    .collect(Collectors.toList()))
        .orElse(List.of());
  }
}
