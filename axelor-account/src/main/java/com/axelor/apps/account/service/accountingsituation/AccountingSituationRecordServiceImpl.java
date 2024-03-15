package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class AccountingSituationRecordServiceImpl implements AccountingSituationRecordService {

  protected CompanyRepository companyRepository;
  protected AccountConfigService accountConfigService;
  protected CompanyService companyService;

  @Inject
  public AccountingSituationRecordServiceImpl(
      CompanyRepository companyRepository,
      AccountConfigService accountConfigService,
      CompanyService companyService) {
    this.companyRepository = companyRepository;
    this.accountConfigService = accountConfigService;
    this.companyService = companyService;
  }

  @Override
  public void setDefaultCompany(AccountingSituation accountingSituation, Partner partner) {
    Company company = Optional.of(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null);

    if (company == null) {
      company = companyService.getDefaultCompany(null);
    }

    Company finalCompany = company;
    if (company != null
        && partner != null
        && ObjectUtils.isEmpty(partner.getAccountingSituationList())
        && partner.getAccountingSituationList().stream()
            .noneMatch(as -> Objects.equals(finalCompany, as.getCompany()))) {
      accountingSituation.setCompany(finalCompany);
    }
  }
}
