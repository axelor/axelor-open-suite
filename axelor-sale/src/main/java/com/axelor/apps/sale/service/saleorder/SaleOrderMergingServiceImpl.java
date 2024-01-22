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

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.team.db.Team;
import com.axelor.utils.MapTools;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class SaleOrderMergingServiceImpl implements SaleOrderMergingService {

  protected static class CommonFieldsImpl implements CommonFields {

    private Company commonCompany = null;
    private Currency commonCurrency = null;
    private Partner commonClientPartner = null;
    private TaxNumber commonTaxNumber = null;
    private FiscalPosition commonFiscalPosition = null;
    private Team commonTeam = null;
    private Partner commonContactPartner = null;
    private PriceList commonPriceList = null;

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

  @Inject
  public SaleOrderMergingServiceImpl(SaleOrderCreateService saleOrderCreateService) {
    this.saleOrderCreateService = saleOrderCreateService;
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
    Objects.requireNonNull(saleOrdersToMerge);
    SaleOrderMergingResult result = controlSaleOrdersToMerge(saleOrdersToMerge);

    if (isConfirmationNeeded(result)) {
      result.needConfirmation();
      return result;
    }
    result.setSaleOrder(mergeSaleOrders(saleOrdersToMerge, result));
    return result;
  }

  @Override
  public SaleOrderMergingResult mergeSaleOrdersWithContext(
      List<SaleOrder> saleOrdersToMerge, Context context) throws AxelorException {
    Objects.requireNonNull(saleOrdersToMerge);
    Objects.requireNonNull(context);

    SaleOrderMergingResult result = controlSaleOrdersToMerge(saleOrdersToMerge);
    updateResultWithContext(result, context);
    result.setSaleOrder(mergeSaleOrders(saleOrdersToMerge, result));
    return result;
  }

  protected void updateResultWithContext(SaleOrderMergingResult result, Context context) {
    if (context.get("priceList") != null) {
      getCommonFields(result)
          .setCommonPriceList(MapTools.findObject(PriceList.class, context.get("priceList")));
    }
    if (context.get("contactPartner") != null) {
      getCommonFields(result)
          .setCommonContactPartner(
              MapTools.findObject(Partner.class, context.get("contactPartner")));
    }
    if (context.get("team") != null) {
      getCommonFields(result).setCommonTeam(MapTools.findObject(Team.class, context.get("team")));
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
    saleOrdersToMerge.stream()
        .skip(1)
        .forEach(
            saleOrder -> {
              updateDiffsCommonFields(saleOrder, result);
            });

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
        || getChecks(result).isExistTeamDiff();
  }

  protected SaleOrder mergeSaleOrders(
      List<SaleOrder> saleOrdersToMerge, SaleOrderMergingResult result) throws AxelorException {
    return saleOrderCreateService.mergeSaleOrders(
        saleOrdersToMerge,
        getCommonFields(result).getCommonCurrency(),
        getCommonFields(result).getCommonClientPartner(),
        getCommonFields(result).getCommonCompany(),
        getCommonFields(result).getCommonContactPartner(),
        getCommonFields(result).getCommonPriceList(),
        getCommonFields(result).getCommonTeam(),
        getCommonFields(result).getCommonTaxNumber(),
        getCommonFields(result).getCommonFiscalPosition());
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
  }
}
