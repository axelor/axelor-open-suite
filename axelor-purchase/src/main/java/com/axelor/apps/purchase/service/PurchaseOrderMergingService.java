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
package com.axelor.apps.purchase.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.rpc.Context;
import java.util.List;

public interface PurchaseOrderMergingService {

  interface CommonFields {
    Company getCommonCompany();

    void setCommonCompany(Company commonCompany);

    Currency getCommonCurrency();

    void setCommonCurrency(Currency commonCurrency);

    Partner getCommonSupplierPartner();

    void setCommonSupplierPartner(Partner commonSupplierPartner);

    Partner getCommonContactPartner();

    void setCommonContactPartner(Partner commonContactPartner);

    PriceList getCommonPriceList();

    void setCommonPriceList(PriceList commonPriceList);

    TradingName getCommonTradingName();

    void setCommonTradingName(TradingName commonTradingName);

    FiscalPosition getCommonFiscalPosition();

    void setCommonFiscalPosition(FiscalPosition commonFiscalPosition);

    boolean getAllTradingNamesAreNull();

    void setAllTradingNamesAreNull(boolean allTradingNamesAreNull);

    boolean getAllFiscalPositionsAreNull();

    void setAllFiscalPositionsAreNull(boolean allFiscalPositionsAreNull);

    TaxNumber getCommonCompanyTaxNumber();

    void setCommonCompanyTaxNumber(TaxNumber commonCompanyTaxNumber);
  }

  interface Checks {
    boolean isExistCurrencyDiff();

    void setExistCurrencyDiff(boolean existCurrencyDiff);

    boolean isExistCompanyDiff();

    void setExistCompanyDiff(boolean existCompanyDiff);

    boolean isExistSupplierPartnerDiff();

    void setExistSupplierPartnerDiff(boolean existSupplierPartnerDiff);

    boolean isExistContactPartnerDiff();

    void setExistContactPartnerDiff(boolean existContactPartnerDiff);

    boolean isExistPriceListDiff();

    void setExistPriceListDiff(boolean existPriceListDiff);

    boolean isExistTradingNameDiff();

    void setExistTradingNameDiff(boolean existTradingNameDiff);

    boolean isExistFiscalPositionDiff();

    void setExistFiscalPositionDiff(boolean existFiscalPositionDiff);

    boolean isExistAtiDiff();

    void setExistAtiDiff(boolean existAtiDiff);
  }

  interface PurchaseOrderMergingResult {
    void setPurchaseOrder(PurchaseOrder purchaseOrder);

    PurchaseOrder getPurchaseOrder();

    void needConfirmation();

    boolean isConfirmationNeeded();
  }

  PurchaseOrderMergingResult create();

  CommonFields getCommonFields(PurchaseOrderMergingResult result);

  Checks getChecks(PurchaseOrderMergingResult result);

  /**
   * Merge purchase orders. This method can update the database, removing existing purchase orders
   * and creating a new one.
   *
   * @param purchaseOrdersToMerge purchase orders to merge
   * @return a {@link PurchaseOrderMergingResult} object.
   * @throws AxelorException
   */
  PurchaseOrderMergingResult mergePurchaseOrders(List<PurchaseOrder> purchaseOrdersToMerge)
      throws AxelorException;

  /**
   * Do the same actions as {@link this#mergePurchaseOrders(List)}, but does not update the
   * database. This can be used to check if multiple purchase orders are merge-compatible and
   * extract their common fields.
   *
   * @param purchaseOrdersToMerge purchase orders to merge
   * @return a {@link PurchaseOrderMergingResult} object.
   * @throws AxelorException
   */
  PurchaseOrderMergingResult simulateMergePurchaseOrders(List<PurchaseOrder> purchaseOrdersToMerge)
      throws AxelorException;

  /**
   * Merge purchase orders.
   *
   * @param purchaseOrdersToMerge purchase orders to merge
   * @param context a context with the parameters the user chose for conflicting fields (example:
   *     contactPartner)
   * @return a PurchaseOrderMergingResult object.
   * @throws AxelorException
   */
  PurchaseOrderMergingResult mergePurchaseOrdersWithContext(
      List<PurchaseOrder> purchaseOrdersToMerge, Context context) throws AxelorException;

  /**
   * Do the same actions as {@link this#mergePurchaseOrdersWithContext(List, Context)}, but does not
   * update the database. This can be used to check if multiple sale orders are merge-compatible and
   * extract their common fields.
   *
   * @param purchaseOrdersToMerge sale orders to merge
   * @param context a context with the parameters the user chose for conflicting fields (example:
   *     contactPartner)
   * @return a {@link PurchaseOrderMergingResult} object.
   * @throws AxelorException
   */
  PurchaseOrderMergingResult simulateMergePurchaseOrdersWithContext(
      List<PurchaseOrder> purchaseOrdersToMerge, Context context) throws AxelorException;

  List<PurchaseOrder> convertSelectedLinesToMergeLines(List<Integer> idList);
}
