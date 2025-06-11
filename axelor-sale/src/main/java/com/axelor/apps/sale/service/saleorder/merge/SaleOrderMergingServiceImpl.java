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
package com.axelor.apps.sale.service.saleorder.merge;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.team.db.Team;
import com.axelor.utils.helpers.MapHelper;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class SaleOrderMergingServiceImpl implements SaleOrderMergingService {

  @FunctionalInterface
  protected interface MergingMethod {
    SaleOrder createOrMergeSaleOrders(
        List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException;
  }

  @FunctionalInterface
  protected interface SaleOrderStringGetter {
    String getString(SaleOrder saleOrder);
  }

  protected static class CommonFieldsImpl implements CommonFields {

    private Company commonCompany = null;
    private Currency commonCurrency = null;
    private Partner commonClientPartner = null;
    private TaxNumber commonTaxNumber = null;
    private FiscalPosition commonFiscalPosition = null;
    private Team commonTeam = null;
    private Partner commonContactPartner = null;
    private PriceList commonPriceList = null;
    private TradingName commonTradingName = null;

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
    public Partner getCommonClientPartner() {
      return this.commonClientPartner;
    }

    @Override
    public void setCommonClientPartner(Partner commonClientPartner) {
      this.commonClientPartner = commonClientPartner;
    }

    @Override
    public TaxNumber getCommonTaxNumber() {
      return commonTaxNumber;
    }

    @Override
    public void setCommonTaxNumber(TaxNumber commonTaxNumber) {
      this.commonTaxNumber = commonTaxNumber;
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
    public Team getCommonTeam() {
      return commonTeam;
    }

    @Override
    public void setCommonTeam(Team commonTeam) {
      this.commonTeam = commonTeam;
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
    public void setCommonTradingName(TradingName tradingName) {
      this.commonTradingName = tradingName;
    }
  }

  protected static class ChecksImpl implements Checks {

    private boolean existCurrencyDiff = false;
    private boolean existCompanyDiff = false;
    private boolean existClientPartnerDiff = false;
    private boolean existTaxNumberDiff = false;
    private boolean existFiscalPositionDiff = false;
    private boolean existTeamDiff = false;
    private boolean existContactPartnerDiff = false;
    private boolean existPriceListDiff = false;
    private boolean existTradingNameDiff = false;
    private boolean existAtiDiff = false;

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
    public boolean isExistClientPartnerDiff() {
      return existClientPartnerDiff;
    }

    @Override
    public void setExistClientPartnerDiff(boolean existClientPartnerDiff) {
      this.existClientPartnerDiff = existClientPartnerDiff;
    }

    @Override
    public boolean isExistTaxNumberDiff() {
      return existTaxNumberDiff;
    }

    @Override
    public void setExistTaxNumberDiff(boolean existTaxNumberDiff) {
      this.existTaxNumberDiff = existTaxNumberDiff;
    }

    @Override
    public boolean isExistFiscalPositionDiff() {
      return existFiscalPositionDiff;
    }

    @Override
    public void setExistFiscalPositionDiff(boolean existFiscalPositionDiff) {
      this.existFiscalPositionDiff = existFiscalPositionDiff;
    }

    @Override
    public boolean isExistTeamDiff() {
      return existTeamDiff;
    }

    @Override
    public void setExistTeamDiff(boolean existTeamDiff) {
      this.existTeamDiff = existTeamDiff;
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
    public boolean isExistAtiDiff() {
      return existAtiDiff;
    }

    @Override
    public void setExistAtiDiff(boolean existAtiDiff) {
      this.existAtiDiff = existAtiDiff;
    }
  }

  protected static class SaleOrderMergingResultImpl implements SaleOrderMergingResult {

    private SaleOrder saleOrder;
    private boolean isConfirmationNeeded;
    private final CommonFieldsImpl commonFields;
    private final ChecksImpl checks;

    public SaleOrderMergingResultImpl() {
      this.saleOrder = null;
      this.isConfirmationNeeded = false;
      this.commonFields = new CommonFieldsImpl();
      this.checks = new ChecksImpl();
    }

    public SaleOrder getSaleOrder() {
      return saleOrder;
    }

    public void setSaleOrder(SaleOrder saleOrder) {
      this.saleOrder = saleOrder;
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

  protected SaleOrderCreateService saleOrderCreateService;

  protected SaleOrderRepository saleOrderRepository;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected DMSService dmsService;
  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderMergingServiceImpl(
      SaleOrderCreateService saleOrderCreateService,
      SaleOrderRepository saleOrderRepository,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      DMSService dmsService,
      AppBaseService appBaseService) {
    this.saleOrderCreateService = saleOrderCreateService;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.dmsService = dmsService;
    this.appBaseService = appBaseService;
  }

  @Override
  public SaleOrderMergingResultImpl create() {
    return new SaleOrderMergingResultImpl();
  }

  @Override
  public CommonFieldsImpl getCommonFields(SaleOrderMergingResult result) {
    return ((SaleOrderMergingResultImpl) result).commonFields;
  }

  @Override
  public ChecksImpl getChecks(SaleOrderMergingResult result) {
    return ((SaleOrderMergingResultImpl) result).checks;
  }

  @Override
  public SaleOrderMergingResult mergeSaleOrders(List<SaleOrder> saleOrdersToMerge)
      throws AxelorException {
    return mergeSaleOrders(saleOrdersToMerge, this::mergeSaleOrders);
  }

  @Override
  public SaleOrderMergingResult mergeSaleOrdersWithContext(
      List<SaleOrder> saleOrdersToMerge, Context context) throws AxelorException {
    SaleOrderMergingResult saleOrderMergeResult =
        mergeSaleOrdersWithContext(saleOrdersToMerge, context, this::mergeSaleOrders);
    setDummySaleOrderSeq(saleOrderMergeResult, saleOrdersToMerge);
    return saleOrderMergeResult;
  }

  @Override
  public SaleOrderMergingResult simulateMergeSaleOrders(List<SaleOrder> saleOrdersToMerge)
      throws AxelorException {
    SaleOrderMergingResult saleOrderMergeResult =
        mergeSaleOrders(saleOrdersToMerge, this::generateSaleOrder);
    setDummySaleOrderSeq(saleOrderMergeResult, saleOrdersToMerge);
    return saleOrderMergeResult;
  }

  /**
   * Since the process is done without updating the database, the sale order is not saved and does
   * not have a sequence. So this method will compute a "dummy sequence". This method cannot be
   * called when merging sale orders in database.
   */
  protected void setDummySaleOrderSeq(
      SaleOrderMergingResult result, List<SaleOrder> saleOrderList) {
    if (result.getSaleOrder() != null) {
      result
          .getSaleOrder()
          .setSaleOrderSeq(
              StringHelper.cutTooLongString(
                  saleOrderList.stream()
                      .map(SaleOrder::getSaleOrderSeq)
                      .filter(s -> s != null && !s.isEmpty())
                      .collect(Collectors.joining("-"))));
    }
  }

  @Override
  public SaleOrderMergingResult simulateMergeSaleOrdersWithContext(
      List<SaleOrder> saleOrdersToMerge, Context context) throws AxelorException {
    return mergeSaleOrdersWithContext(saleOrdersToMerge, context, this::generateSaleOrder);
  }

  /**
   * Generic method to merge a sale order that can update the database or not.
   *
   * @param saleOrdersToMerge list of sales order to merge
   * @param mergeMethod can be {@link this#mergeSaleOrders( List, SaleOrderMergingResult)} which
   *     will update the database or {@link this#generateSaleOrder( List, SaleOrderMergingResult)}
   *     which will not update the database, only create the merged sale order in memory.
   * @return a sale order merging result object
   * @throws AxelorException
   */
  protected SaleOrderMergingResult mergeSaleOrders(
      List<SaleOrder> saleOrdersToMerge, MergingMethod mergeMethod) throws AxelorException {
    Objects.requireNonNull(saleOrdersToMerge);
    SaleOrderMergingResult result = controlSaleOrdersToMerge(saleOrdersToMerge);

    if (isConfirmationNeeded(result)) {
      result.needConfirmation();
      return result;
    }
    result.setSaleOrder(mergeMethod.createOrMergeSaleOrders(saleOrdersToMerge, result));
    return result;
  }

  /**
   * Generic method to merge a sale order that can update the database or not.
   *
   * @param saleOrdersToMerge list of sales order to merge
   * @param context a context with the parameters the user chose for conflicting fields (example:
   *     contactPartner)
   * @param mergeMethod can be {@link this#mergeSaleOrders( List, SaleOrderMergingResult)} which
   *     will update the database or {@link this#generateSaleOrder( List, SaleOrderMergingResult)}
   *     which will not update the database, only create the merged sale order in memory.
   * @return a sale order merging result object
   * @throws AxelorException
   */
  protected SaleOrderMergingResult mergeSaleOrdersWithContext(
      List<SaleOrder> saleOrdersToMerge, Context context, MergingMethod mergeMethod)
      throws AxelorException {
    Objects.requireNonNull(saleOrdersToMerge);
    Objects.requireNonNull(context);

    SaleOrderMergingResult result = controlSaleOrdersToMerge(saleOrdersToMerge);
    updateResultWithContext(result, context);
    result.setSaleOrder(mergeMethod.createOrMergeSaleOrders(saleOrdersToMerge, result));
    return result;
  }

  protected void updateResultWithContext(SaleOrderMergingResult result, Context context) {
    if (context.get("priceList") != null) {
      getCommonFields(result)
          .setCommonPriceList(MapHelper.get(context, PriceList.class, "priceList"));
    }
    if (context.get("contactPartner") != null) {
      getCommonFields(result)
          .setCommonContactPartner(MapHelper.get(context, Partner.class, "contactPartner"));
    }
    if (context.get("team") != null) {
      getCommonFields(result).setCommonTeam(MapHelper.get(context, Team.class, "team"));
    }
    if (context.get("tradingName") != null) {
      getCommonFields(result)
          .setCommonTradingName(MapHelper.get(context, TradingName.class, "tradingName"));
    }
  }

  protected SaleOrderMergingResult controlSaleOrdersToMerge(List<SaleOrder> saleOrdersToMerge)
      throws AxelorException {
    SaleOrderMergingResult result = create();

    if (saleOrdersToMerge.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_LIST_EMPTY));
    }

    SaleOrder firstSaleOrder = saleOrdersToMerge.get(0);
    fillCommonFields(firstSaleOrder, result);
    checkDiffs(saleOrdersToMerge, result, firstSaleOrder);

    StringJoiner fieldErrors = new StringJoiner("<BR/>");
    checkErrors(fieldErrors, result);
    if (fieldErrors.length() > 0) {
      throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, fieldErrors.toString());
    }
    return result;
  }

  protected boolean isConfirmationNeeded(SaleOrderMergingResult result) {

    return getChecks(result).isExistContactPartnerDiff()
        || getChecks(result).isExistPriceListDiff()
        || getChecks(result).isExistTeamDiff()
        || getChecks(result).isExistTradingNameDiff();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected SaleOrder mergeSaleOrders(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException {

    SaleOrder saleOrderMerged = generateSaleOrder(saleOrdersToMerge, result);
    return updateDatabase(saleOrderMerged, saleOrdersToMerge);
  }

  protected SaleOrder generateSaleOrder(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException {
    String internalNote =
        computeConcatenatedString(saleOrdersToMerge, SaleOrder::getInternalNote, "<br>");
    String numSeq = computeConcatenatedString(saleOrdersToMerge, SaleOrder::getSaleOrderSeq, "-");
    String externalRef =
        computeConcatenatedString(saleOrdersToMerge, SaleOrder::getExternalReference, "|");

    SaleOrder saleOrderMerged =
        saleOrderCreateService.createSaleOrder(
            AuthUtils.getUser(),
            getCommonFields(result).getCommonCompany(),
            getCommonFields(result).getCommonContactPartner(),
            getCommonFields(result).getCommonCurrency(),
            null,
            numSeq,
            externalRef,
            getCommonFields(result).getCommonPriceList(),
            getCommonFields(result).getCommonClientPartner(),
            getCommonFields(result).getCommonTeam(),
            getCommonFields(result).getCommonTaxNumber(),
            internalNote,
            getCommonFields(result).getCommonFiscalPosition(),
            getCommonFields(result).getCommonTradingName());

    saleOrderMerged.setInAti(saleOrdersToMerge.stream().anyMatch(SaleOrder::getInAti));

    this.attachToNewSaleOrder(saleOrdersToMerge, saleOrderMerged);
    saleOrderComputeService.computeSaleOrder(saleOrderMerged);
    updateChildrenOrder(saleOrdersToMerge, saleOrderMerged);

    return saleOrderMerged;
  }

  protected void updateChildrenOrder(List<SaleOrder> saleOrdersToMerge, SaleOrder saleOrderMerged) {
    for (SaleOrder saleOrder : saleOrdersToMerge) {
      for (SaleOrder childOrder :
          saleOrderRepository
              .all()
              .filter("self.originSaleQuotation = :saleOrder")
              .bind("saleOrder", saleOrder)
              .fetch()) {
        childOrder.setOriginSaleQuotation(saleOrderMerged);
      }
    }
  }

  protected String computeConcatenatedString(
      List<SaleOrder> saleOrderList, SaleOrderStringGetter getter, String joiner) {
    return saleOrderList.stream()
        .map(getter::getString)
        .filter(s -> s != null && !s.isEmpty())
        .collect(Collectors.joining(joiner));
  }

  protected SaleOrder updateDatabase(SaleOrder saleOrderMerged, List<SaleOrder> saleOrdersToMerge) {

    saleOrderRepository.save(saleOrderMerged);

    dmsService.addLinkedDMSFiles(saleOrdersToMerge, saleOrderMerged);

    this.removeOldSaleOrders(saleOrdersToMerge);

    return saleOrderMerged;
  }

  /** Attach all sale order lines to new sale order */
  protected void attachToNewSaleOrder(List<SaleOrder> saleOrderList, SaleOrder saleOrderMerged) {
    for (SaleOrder saleOrder : saleOrderList) {
      int countLine = 1;
      for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
        SaleOrderLine copiedSaleOrderLine = saleOrderLineRepository.copy(saleOrderLine, false);
        saleOrderLine.setOrderedQty(BigDecimal.ZERO);
        copiedSaleOrderLine.setSequence(countLine * 10);
        saleOrderMerged.addSaleOrderLineListItem(copiedSaleOrderLine);
        countLine++;
      }
    }
  }

  /** Remove old sale orders after merge */
  protected void removeOldSaleOrders(List<SaleOrder> saleOrderList) {
    for (SaleOrder saleOrder : saleOrderList) {
      saleOrderRepository.remove(saleOrder);
    }
  }

  protected void checkErrors(StringJoiner fieldErrors, SaleOrderMergingResult result) {
    if (getChecks(result).isExistCurrencyDiff()
        || getCommonFields(result).getCommonCurrency() == null) {
      fieldErrors.add(I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_ERROR_CURRENCY));
    }
    if (getChecks(result).isExistClientPartnerDiff()
        || getCommonFields(result).getCommonClientPartner() == null) {
      fieldErrors.add(I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_ERROR_CLIENT_PARTNER));
    }
    if (getChecks(result).isExistCompanyDiff()
        || getCommonFields(result).getCommonCompany() == null) {
      fieldErrors.add(I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_ERROR_COMPANY));
    }
    // TaxNumber can be null
    if (getChecks(result).isExistTaxNumberDiff()) {
      fieldErrors.add(I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_ERROR_TAX_NUMBER));
    }
    // FiscalPosition can be null
    if (getChecks(result).isExistFiscalPositionDiff()) {
      fieldErrors.add(I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_ERROR_FISCAL_POSITION));
    }

    if (getChecks(result).isExistAtiDiff()) {
      fieldErrors.add(I18n.get(SaleExceptionMessage.SALE_ORDER_MERGE_ERROR_ATI_CONFIG));
    }
  }

  protected void updateDiffsCommonFields(SaleOrder saleOrder, SaleOrderMergingResult result) {
    CommonFields commonFields = getCommonFields(result);
    Checks checks = getChecks(result);
    if ((commonFields.getCommonCurrency() == null ^ saleOrder.getCurrency() == null)
        || (commonFields.getCommonCurrency() != saleOrder.getCurrency()
            && !commonFields.getCommonCurrency().equals(saleOrder.getCurrency()))) {
      commonFields.setCommonCurrency(null);
      checks.setExistCurrencyDiff(true);
    }
    if ((commonFields.getCommonClientPartner() == null ^ saleOrder.getClientPartner() == null)
        || (commonFields.getCommonClientPartner() != saleOrder.getClientPartner()
            && !commonFields.getCommonClientPartner().equals(saleOrder.getClientPartner()))) {
      commonFields.setCommonClientPartner(null);
      checks.setExistClientPartnerDiff(true);
    }
    if ((commonFields.getCommonCompany() == null ^ saleOrder.getCompany() == null)
        || (commonFields.getCommonCompany() != saleOrder.getCompany()
            && !commonFields.getCommonCompany().equals(saleOrder.getCompany()))) {
      commonFields.setCommonCompany(null);
      checks.setExistCompanyDiff(true);
    }
    if ((commonFields.getCommonContactPartner() == null ^ saleOrder.getContactPartner() == null)
        || (commonFields.getCommonContactPartner() != saleOrder.getContactPartner()
            && !commonFields.getCommonContactPartner().equals(saleOrder.getContactPartner()))) {
      commonFields.setCommonContactPartner(null);
      checks.setExistContactPartnerDiff(true);
    }
    if ((commonFields.getCommonTeam() == null ^ saleOrder.getTeam() == null)
        || (commonFields.getCommonTeam() != saleOrder.getTeam()
            && !commonFields.getCommonTeam().equals(saleOrder.getTeam()))) {
      commonFields.setCommonTeam(null);
      checks.setExistTeamDiff(true);
    }
    if ((commonFields.getCommonPriceList() == null ^ saleOrder.getPriceList() == null)
        || (commonFields.getCommonPriceList() != saleOrder.getPriceList()
            && !commonFields.getCommonPriceList().equals(saleOrder.getPriceList()))) {
      commonFields.setCommonPriceList(null);
      checks.setExistPriceListDiff(true);
    }
    if ((commonFields.getCommonTaxNumber() == null ^ saleOrder.getTaxNumber() == null)
        || (commonFields.getCommonTaxNumber() != saleOrder.getTaxNumber()
            && !commonFields.getCommonTaxNumber().equals(saleOrder.getTaxNumber()))) {
      commonFields.setCommonTaxNumber(null);
      checks.setExistTaxNumberDiff(true);
    }
    if ((commonFields.getCommonFiscalPosition() == null ^ saleOrder.getFiscalPosition() == null)
        || (commonFields.getCommonFiscalPosition() != saleOrder.getFiscalPosition()
            && !commonFields.getCommonFiscalPosition().equals(saleOrder.getFiscalPosition()))) {
      commonFields.setCommonFiscalPosition(null);
      checks.setExistFiscalPositionDiff(true);
    }
    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && ((commonFields.getCommonTradingName() == null ^ saleOrder.getTradingName() == null)
            || (commonFields.getCommonTradingName() != saleOrder.getTradingName()
                && !commonFields.getCommonTradingName().equals(saleOrder.getTradingName())))) {
      commonFields.setCommonTradingName(null);
      checks.setExistTradingNameDiff(true);
    }
  }

  protected void fillCommonFields(SaleOrder firstSaleOrder, SaleOrderMergingResult result) {
    CommonFields commonFields = getCommonFields(result);
    commonFields.setCommonCompany(firstSaleOrder.getCompany());
    commonFields.setCommonCurrency(firstSaleOrder.getCurrency());
    commonFields.setCommonContactPartner(firstSaleOrder.getContactPartner());
    commonFields.setCommonFiscalPosition(firstSaleOrder.getFiscalPosition());
    commonFields.setCommonPriceList(firstSaleOrder.getPriceList());
    commonFields.setCommonTaxNumber(firstSaleOrder.getTaxNumber());
    commonFields.setCommonTeam(firstSaleOrder.getTeam());
    commonFields.setCommonClientPartner(firstSaleOrder.getClientPartner());
    commonFields.setCommonTradingName(firstSaleOrder.getTradingName());
  }

  @Override
  public List<SaleOrder> convertSelectedLinesToMergeLines(List<Integer> idList) {
    return Optional.ofNullable(idList)
        .map(
            list ->
                list.stream()
                    .map(id -> saleOrderRepository.find(Long.valueOf(id)))
                    .collect(Collectors.toList()))
        .orElse(List.of());
  }

  protected void checkDiffs(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result, SaleOrder firstSaleOrder) {
    saleOrdersToMerge.stream()
        .skip(1)
        .forEach(saleOrder -> updateDiffsCommonFields(saleOrder, result));

    if (saleOrdersToMerge.stream()
        .anyMatch(order -> order.getInAti() != firstSaleOrder.getInAti())) {
      Checks checks = getChecks(result);
      checks.setExistAtiDiff(true);
    }
  }
}
