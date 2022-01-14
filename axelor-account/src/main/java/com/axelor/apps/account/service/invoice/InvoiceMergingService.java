package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.exception.AxelorException;
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
  }

  interface InvoiceMergingResult {
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
      PaymentCondition paymentCondition)
      throws AxelorException;
}
