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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.TradingName;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceMergingService {

  interface CommonFields {
    Company getCommonCompany();

    void setCommonCompany(Company commonCompany);

    Currency getCommonCurrency();

    void setCommonCurrency(Currency commonCurrency);

    Partner getCommonPartner();

    void setCommonPartner(Partner commonPartner);

    PaymentCondition getCommonPaymentCondition();

    void setCommonPaymentCondition(PaymentCondition commonPaymentCondition);

    Partner getCommonContactPartner();

    void setCommonContactPartner(Partner commonContactPartner);

    PriceList getCommonPriceList();

    void setCommonPriceList(PriceList commonPriceList);

    PaymentMode getCommonPaymentMode();

    void setCommonPaymentMode(PaymentMode commonPaymentMode);

    String getCommonSupplierInvoiceNb();

    void setCommonSupplierInvoiceNb(String commonSupplierInvoiceNb);

    LocalDate getCommonOriginDate();

    void setCommonOriginDate(LocalDate commonOriginDate);

    void setCommonTradingName(TradingName commonTradingName);

    TradingName getCommonTradingName();

    void setCommonFiscalPosition(FiscalPosition commonFiscalPosition);

    FiscalPosition getCommonFiscalPosition();
  }

  interface Checks {
    boolean isExistPaymentConditionDiff();

    void setExistPaymentConditionDiff(boolean existPaymentConditionDiff);

    boolean isExistContactPartnerDiff();

    void setExistContactPartnerDiff(boolean existContactPartnerDiff);

    boolean isExistPriceListDiff();

    void setExistPriceListDiff(boolean existPriceListDiff);

    boolean isExistPaymentModeDiff();

    void setExistPaymentModeDiff(boolean existPaymentModeDiff);

    boolean isExistSupplierInvoiceNbDiff();

    void setExistSupplierInvoiceNbDiff(boolean existSupplierInvoiceNbDiff);

    boolean isExistOriginDateDiff();

    void setExistOriginDateDiff(boolean existOriginDateDiff);

    void setExistTradingNameDiff(boolean existTradingNameDiff);

    boolean isExistTradingNameDiff();

    void setExistFiscalPositionDiff(boolean existFiscalPositionDiff);

    boolean isExistFiscalPositionDiff();
  }

  interface InvoiceMergingResult {
    void setInvoiceType(Integer type);

    Integer getInvoiceType();

    void setInvoice(Invoice invoice);

    Invoice getInvoice();

    void needConfirmation();

    boolean isConfirmationNeeded();
  }

  InvoiceMergingResult create();

  CommonFields getCommonFields(InvoiceMergingResult result);

  Checks getChecks(InvoiceMergingResult result);

  InvoiceMergingResult mergeInvoices(List<Invoice> invoicesToMerge) throws AxelorException;

  InvoiceMergingResult mergeInvoices(
      List<Invoice> invoicesToMerge,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition)
      throws AxelorException;

  InvoiceMergingResult mergeInvoices(
      List<Invoice> invoicesToMerge,
      Partner contactPartner,
      PriceList priceList,
      PaymentMode paymentMode,
      PaymentCondition paymentCondition,
      TradingName tradingName,
      FiscalPosition fiscalPosition,
      String supplierInvoiceNb,
      LocalDate originDate)
      throws AxelorException;
}
