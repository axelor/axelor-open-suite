package com.axelor.apps.contract.service.record;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.contract.db.ContractLine;
import com.axelor.apps.contract.model.AnalyticLineContractModel;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class ContractLineRecordSetServiceImpl implements ContractLineRecordSetService {

  protected AppAccountService appAccountService;
  protected CurrencyService currencyService;

  @Inject
  public ContractLineRecordSetServiceImpl(
      AppAccountService appAccountService, CurrencyService currencyService) {
    this.appAccountService = appAccountService;
    this.currencyService = currencyService;
  }

  public void setCompanyExTaxTotal(
      AnalyticLineContractModel analyticLineContractModel, ContractLine contractLine)
      throws AxelorException {
    if (contractLine != null && analyticLineContractModel != null) {
      BigDecimal companyAmount =
          currencyService.getAmountCurrencyConvertedAtDate(
              analyticLineContractModel.getContract().getCurrency(),
              analyticLineContractModel.getCompany().getCurrency(),
              contractLine.getExTaxTotal(),
              appAccountService.getTodayDate(analyticLineContractModel.getCompany()));
      analyticLineContractModel.setCompanyExTaxTotal(companyAmount);
    }
  }
}
