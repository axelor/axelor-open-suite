package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.tool.service.ListToolService;
import com.axelor.exception.AxelorException;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class AnalyticLineServiceImpl implements AnalyticLineService {

  protected AccountConfigService accountConfigService;
  protected AnalyticToolService analyticToolService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected ListToolService listToolService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;

  @Inject
  public AnalyticLineServiceImpl(
      AccountConfigService accountConfigService,
      AnalyticToolService analyticToolService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      ListToolService listToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService) {
    this.accountConfigService = accountConfigService;
    this.analyticToolService = analyticToolService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.listToolService = listToolService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
  }

  @Override
  public List<Long> getAxisDomains(AnalyticLine line, Company company, int position)
      throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<>();
    List<Long> analyticAccountListByRules = new ArrayList<>();

    AnalyticAxis analyticAxis = new AnalyticAxis();

    if (analyticToolService.isPositionUnderAnalyticAxisSelect(company, position)) {

      for (AnalyticAxisByCompany axis :
          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
        if (axis.getOrderSelect() == position) {
          analyticAxis = axis.getAnalyticAxis();
        }
      }

      for (AnalyticAccount analyticAccount :
          analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch()) {
        analyticAccountListByAxis.add(analyticAccount.getId());
      }
      if (line.getAccount() != null) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(line.getAccount());
        if (!analyticAccountList.isEmpty()) {
          for (AnalyticAccount analyticAccount : analyticAccountList) {
            analyticAccountListByRules.add(analyticAccount.getId());
          }
          if (!CollectionUtils.isEmpty(analyticAccountListByRules)) {
            analyticAccountListByAxis =
                listToolService.intersection(analyticAccountListByAxis, analyticAccountListByRules);
          }
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public boolean isAxisRequired(AnalyticLine line, Company company, int position)
      throws AxelorException {
    if (company != null
        && moveLineComputeAnalyticService.checkManageAnalytic(company)
        && line != null
        && line.getAccount() != null
        && line.getAccount().getCompany() != null) {
      Account account = line.getAccount();
      Integer nbrAxis =
          accountConfigService.getAccountConfig(account.getCompany()).getNbrOfAnalyticAxisSelect();
      return account != null
          && account.getAnalyticDistributionAuthorized()
          && account.getAnalyticDistributionRequiredOnMoveLines()
          && line.getAnalyticDistributionTemplate() == null
          && (position <= nbrAxis);
    }
    return false;
  }

  @Override
  public AnalyticLine checkAnalyticLineForAxis(AnalyticLine line) {
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis1AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis1AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis2AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis2AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis3AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis3AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis4AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis4AnalyticAccount(null);
    }
    if (analyticToolService.isAnalyticAxisFilled(
        line.getAxis5AnalyticAccount(), line.getAnalyticMoveLineList())) {
      line.setAxis5AnalyticAccount(null);
    }
    return line;
  }

  @Override
  public AnalyticLine printAnalyticAccount(AnalyticLine line, Company company)
      throws AxelorException {
    if (line.getAnalyticMoveLineList() != null
        && !line.getAnalyticMoveLineList().isEmpty()
        && company != null) {
      List<AnalyticMoveLine> analyticMoveLineList = Lists.newArrayList();
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
        for (AnalyticMoveLine analyticMoveLine : line.getAnalyticMoveLineList()) {
          if (analyticMoveLine.getAnalyticAxis().equals(analyticAxisByCompany.getAnalyticAxis())) {
            analyticMoveLineList.add(analyticMoveLine);
          }
        }
        if (analyticMoveLineList.isEmpty()) {
          return line;
        }
        AnalyticMoveLine analyticMoveLine = analyticMoveLineList.get(0);
        if (analyticMoveLineList.size() == 1
            && analyticMoveLine.getPercentage().compareTo(new BigDecimal(100)) == 0) {
          AnalyticAccount analyticAccount = analyticMoveLine.getAnalyticAccount();
          switch (analyticAxisByCompany.getOrderSelect()) {
            case 1:
              line.setAxis1AnalyticAccount(analyticAccount);
              break;
            case 2:
              line.setAxis2AnalyticAccount(analyticAccount);
              break;
            case 3:
              line.setAxis3AnalyticAccount(analyticAccount);
              break;
            case 4:
              line.setAxis4AnalyticAccount(analyticAccount);
              break;
            case 5:
              line.setAxis5AnalyticAccount(analyticAccount);
              break;
            default:
              break;
          }
        }
        analyticMoveLineList.clear();
      }
    }
    return line;
  }
}
