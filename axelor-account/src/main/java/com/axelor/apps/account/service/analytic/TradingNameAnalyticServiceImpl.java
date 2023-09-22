package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.TradingName;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.stream.Collectors;

public class TradingNameAnalyticServiceImpl implements TradingNameAnalyticService {

  protected AccountConfigService accountConfigService;

  @Inject
  public TradingNameAnalyticServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  public String getDomainOnCompany(TradingName tradingName) {
    String companyIds =
        tradingName.getCompanySet().stream()
            .map(Company::getId)
            .map(Object::toString)
            .collect(Collectors.joining(","))
            .toString();
    String domain =
        !ObjectUtils.isEmpty(companyIds)
            ? String.format("self.company.id in (%s) AND self.isSpecific = false", companyIds)
            : "self.id = 0";
    return domain;
  }

  @Override
  public boolean isAnalyticTypeByTradingName(TradingName tradingName) {
    return tradingName.getCompanySet().stream()
            .filter(
                c -> {
                  try {
                    return accountConfigService
                            .getAccountConfig(c)
                            .getAnalyticDistributionTypeSelect()
                        == AccountConfigRepository.DISTRIBUTION_TYPE_TRADING_NAME;
                  } catch (AxelorException e) {
                    return false;
                  }
                })
            .count()
        == 0;
  }
}
