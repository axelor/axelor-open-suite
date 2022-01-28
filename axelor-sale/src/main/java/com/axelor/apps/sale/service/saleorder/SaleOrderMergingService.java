package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxNumber;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;
import com.axelor.team.db.Team;
import java.util.List;

public interface SaleOrderMergingService {

  interface CommonFields {

    Company getCommonCompany();

    void setCommonCompany(Company commonCompany);

    Currency getCommonCurrency();

    void setCommonCurrency(Currency commonCurrency);

    Partner getCommonClientPartner();

    void setCommonClientPartner(Partner commonClientPartner);

    TaxNumber getCommonTaxNumber();

    void setCommonTaxNumber(TaxNumber commonTaxNumber);

    FiscalPosition getCommonFiscalPosition();

    void setCommonFiscalPosition(FiscalPosition commonFiscalPosition);

    Team getCommonTeam();

    void setCommonTeam(Team commonTeam);

    Partner getCommonContactPartner();

    void setCommonContactPartner(Partner commonContactPartner);

    PriceList getCommonPriceList();

    void setCommonPriceList(PriceList commonPriceList);
  }

  interface Checks {
    boolean isExistCurrencyDiff();

    void setExistCurrencyDiff(boolean existCurrencyDiff);

    boolean isExistCompanyDiff();

    void setExistCompanyDiff(boolean existCompanyDiff);

    boolean isExistClientPartnerDiff();

    void setExistClientPartnerDiff(boolean existClientPartnerDiff);

    boolean isExistTaxNumberDiff();

    void setExistTaxNumberDiff(boolean existTaxNumberDiff);

    boolean isExistFiscalPositionDiff();

    void setExistFiscalPositionDiff(boolean existFiscalPositionDiff);

    boolean isExistTeamDiff();

    void setExistTeamDiff(boolean existTeamDiff);

    boolean isExistContactPartnerDiff();

    void setExistContactPartnerDiff(boolean existContactPartnerDiff);

    boolean isExistPriceListDiff();

    void setExistPriceListDiff(boolean existPriceListDiff);
  }

  interface SaleOrderMergingResult {
    void setSaleOrder(SaleOrder saleOrder);

    SaleOrder getSaleOrder();

    void needConfirmation();

    boolean isConfirmationNeeded();
  }

  SaleOrderMergingResult create();

  CommonFields getCommonFields(SaleOrderMergingResult result);

  Checks getChecks(SaleOrderMergingResult result);

  SaleOrderMergingResult mergeSaleOrders(List<SaleOrder> saleOrdersToMerge) throws AxelorException;

  SaleOrderMergingResult mergeSaleOrdersWithContext(
      List<SaleOrder> saleOrdersToMerge, Context context) throws AxelorException;
}
