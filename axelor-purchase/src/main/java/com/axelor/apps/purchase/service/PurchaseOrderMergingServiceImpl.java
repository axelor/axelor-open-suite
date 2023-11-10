package com.axelor.apps.purchase.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.exception.PurchaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.MapHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class PurchaseOrderMergingServiceImpl implements PurchaseOrderMergingService {

  protected static class CommonFieldsImpl implements CommonFields {

    private Company commonCompany = null;
    private Currency commonCurrency = null;
    private Partner commonSupplierPartner = null;
    private Partner commonContactPartner = null;
    private PriceList commonPriceList = null;
    private TradingName commonTradingName = null;
    private boolean allTradingNamesAreNull = true;

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
    public boolean getAllTradingNamesAreNull() {
      return allTradingNamesAreNull;
    }

    @Override
    public void setAllTradingNamesAreNull(boolean allTradingNamesAreNull) {
      this.allTradingNamesAreNull = allTradingNamesAreNull;
    }
  }

  protected static class ChecksImpl implements Checks {

    private boolean existCurrencyDiff = false;
    private boolean existCompanyDiff = false;
    private boolean existSupplierPartnerDiff = false;
    private boolean existContactPartnerDiff = false;
    private boolean existPriceListDiff = false;
    private boolean existTradingNameDiff = false;

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

  @Inject
  public PurchaseOrderMergingServiceImpl(PurchaseOrderService purchaseOrderService) {
    this.purchaseOrderService = purchaseOrderService;
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

    PurchaseOrder firstPurchaseOrder = purchaseOrdersToMerge.get(0);
    fillCommonFields(firstPurchaseOrder, result);
    purchaseOrdersToMerge.stream()
        .skip(1)
        .forEach(
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
    return purchaseOrderService.mergePurchaseOrders(
        purchaseOrdersToMerge,
        getCommonFields(result).getCommonCurrency(),
        getCommonFields(result).getCommonSupplierPartner(),
        getCommonFields(result).getCommonCompany(),
        getCommonFields(result).getCommonContactPartner(),
        getCommonFields(result).getCommonPriceList(),
        getCommonFields(result).getCommonTradingName());
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
  }

  protected void updateDiffsCommonFields(
      PurchaseOrder purchaseOrder, PurchaseOrderMergingResult result) {
    CommonFields commonFields = getCommonFields(result);
    Checks checks = getChecks(result);
    if ((commonFields.getCommonCurrency() == null ^ purchaseOrder.getCurrency() == null)
        || !Objects.equals(commonFields.getCommonCurrency(), purchaseOrder.getCurrency())) {
      commonFields.setCommonCurrency(null);
      checks.setExistCurrencyDiff(true);
    }
    if ((commonFields.getCommonSupplierPartner() == null
            ^ purchaseOrder.getSupplierPartner() == null)
        || !Objects.equals(
            commonFields.getCommonSupplierPartner(), purchaseOrder.getSupplierPartner())) {
      commonFields.setCommonSupplierPartner(null);
      checks.setExistSupplierPartnerDiff(true);
    }
    if ((commonFields.getCommonCompany() == null ^ purchaseOrder.getCompany() == null)
        || !Objects.equals(commonFields.getCommonCompany(), purchaseOrder.getCompany())) {
      commonFields.setCommonCompany(null);
      checks.setExistCompanyDiff(true);
    }
    if ((commonFields.getCommonContactPartner() == null ^ purchaseOrder.getContactPartner() == null)
        || !Objects.equals(
            commonFields.getCommonContactPartner(), purchaseOrder.getContactPartner())) {
      commonFields.setCommonContactPartner(null);
      checks.setExistContactPartnerDiff(true);
    }
    if ((commonFields.getCommonPriceList() == null ^ purchaseOrder.getPriceList() == null)
        || !Objects.equals(commonFields.getCommonPriceList(), purchaseOrder.getPriceList())) {
      commonFields.setCommonPriceList(null);
      checks.setExistPriceListDiff(true);
    }
    if ((commonFields.getCommonTradingName() == null ^ purchaseOrder.getTradingName() == null)
        || !Objects.equals(commonFields.getCommonTradingName(), purchaseOrder.getTradingName())) {
      commonFields.setCommonTradingName(null);
      commonFields.setAllTradingNamesAreNull(false);
      checks.setExistTradingNameDiff(true);
    }
  }

  protected void fillCommonFields(
      PurchaseOrder firstPurchaseOrder, PurchaseOrderMergingResult result) {
    CommonFields commonFields = getCommonFields(result);
    commonFields.setCommonCompany(firstPurchaseOrder.getCompany());
    commonFields.setCommonCurrency(firstPurchaseOrder.getCurrency());
    commonFields.setCommonContactPartner(firstPurchaseOrder.getContactPartner());
    commonFields.setCommonPriceList(firstPurchaseOrder.getPriceList());
    commonFields.setCommonSupplierPartner(firstPurchaseOrder.getSupplierPartner());
    commonFields.setCommonTradingName(firstPurchaseOrder.getTradingName());
    commonFields.setAllTradingNamesAreNull(commonFields.getCommonTradingName() == null);
  }
}
