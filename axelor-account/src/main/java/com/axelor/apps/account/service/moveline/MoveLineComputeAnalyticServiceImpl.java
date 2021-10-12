package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.tool.service.ListToolService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoveLineComputeAnalyticServiceImpl implements MoveLineComputeAnalyticService {

  protected AnalyticMoveLineService analyticMoveLineService;
  protected AccountConfigService accountConfigService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected ListToolService listToolService;

  private final int RETURN_SCALE = 2;
  private final int CALCULATION_SCALE = 10;

  @Inject
  public MoveLineComputeAnalyticServiceImpl(
      AnalyticMoveLineService analyticMoveLineService,
      AccountConfigService accountConfigService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      ListToolService listToolService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.accountConfigService = accountConfigService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.listToolService = listToolService;
  }

  @Override
  public MoveLine computeAnalyticDistribution(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      createAnalyticDistributionWithTemplate(moveLine);
    } else {
      LocalDate date = moveLine.getDate();
      BigDecimal amount = moveLine.getDebit().add(moveLine.getCredit());
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(analyticMoveLine, amount, date);
      }
    }
    updateAccountTypeOnAnalytic(moveLine, analyticMoveLineList);

    return moveLine;
  }

  @Override
  public void updateAccountTypeOnAnalytic(
      MoveLine moveLine, List<AnalyticMoveLine> analyticMoveLineList) {

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      return;
    }

    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      if (moveLine.getAccount() != null) {
        analyticMoveLine.setAccount(moveLine.getAccount());
        analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
      }
    }
  }

  @Override
  public void generateAnalyticMoveLines(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            moveLine.getAnalyticDistributionTemplate(),
            moveLine.getDebit().add(moveLine.getCredit()),
            AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
            moveLine.getDate());

    analyticMoveLineList.stream().forEach(moveLine::addAnalyticMoveLineListItem);
  }

  @Override
  public MoveLine createAnalyticDistributionWithTemplate(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            moveLine.getAnalyticDistributionTemplate(),
            moveLine.getDebit().add(moveLine.getCredit()),
            AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
            moveLine.getDate());

    if (moveLine.getAnalyticMoveLineList() == null) {
      moveLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      moveLine.getAnalyticMoveLineList().clear();
    }
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }
    return moveLine;
  }

  @Override
  public MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine != null && moveLine.getAccount() != null) {
      if (moveLine.getAccount().getAnalyticDistributionAuthorized()
          && moveLine.getAccount().getAnalyticDistributionTemplate() != null
          && accountConfigService
                  .getAccountConfig(moveLine.getAccount().getCompany())
                  .getAnalyticDistributionTypeSelect()
              == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
        moveLine.setAnalyticDistributionTemplate(
            moveLine.getAccount().getAnalyticDistributionTemplate());
      }
    } else {
      moveLine.setAnalyticDistributionTemplate(null);
    }
    moveLine.getAnalyticMoveLineList().clear();
    moveLine = computeAnalyticDistribution(moveLine);
    return moveLine;
  }

  @Override
  public boolean compareNbrOfAnalyticAxisSelect(int position, MoveLine moveLine)
      throws AxelorException {
    return moveLine != null
        && moveLine.getMove() != null
        && moveLine.getMove().getCompany() != null
        && position
            <= accountConfigService
                .getAccountConfig(moveLine.getMove().getCompany())
                .getNbrOfAnalyticAxisSelect();
  }

  @Override
  public List<Long> setAxisDomains(MoveLine moveLine, int position) throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<Long>();
    List<Long> analyticAccountListByRules = new ArrayList<Long>();

    AnalyticAxis analyticAxis = new AnalyticAxis();

    if (compareNbrOfAnalyticAxisSelect(position, moveLine)) {

      for (AnalyticAxisByCompany axis :
          accountConfigService
              .getAccountConfig(moveLine.getMove().getCompany())
              .getAnalyticAxisByCompanyList()) {
        if (axis.getOrderSelect() == position) {
          analyticAxis = axis.getAnalyticAxis();
        }
      }

      for (AnalyticAccount analyticAccount :
          analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch()) {
        analyticAccountListByAxis.add(analyticAccount.getId());
      }
      if (moveLine.getAccount() != null) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(moveLine.getAccount());
        if (!analyticAccountList.isEmpty()) {
          for (AnalyticAccount analyticAccount : analyticAccountList) {
            analyticAccountListByRules.add(analyticAccount.getId());
          }
          analyticAccountListByAxis =
              listToolService.intersection(analyticAccountListByAxis, analyticAccountListByRules);
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public MoveLine analyzeMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine != null) {

      if (moveLine.getAnalyticMoveLineList() == null) {
        moveLine.setAnalyticMoveLineList(new ArrayList<>());
      } else {
        moveLine.getAnalyticMoveLineList().clear();
      }

      AnalyticMoveLine analyticMoveLine = null;

      if (moveLine.getAxis1AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis1AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis2AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis2AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis3AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis3AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis4AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis4AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis5AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis5AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
    return moveLine;
  }

  @Override
  public MoveLine removeAnalyticOnRemoveAccount(MoveLine moveLine) {
    if (moveLine != null && moveLine.getAccount() == null) {
      moveLine = removeAnalytic(moveLine);
    }
    return moveLine;
  }

  public MoveLine removeAnalytic(MoveLine moveLine) {
    moveLine.setAnalyticDistributionTemplate(null);
    return clearAnalyticAccounting(moveLine);
  }

  @Override
  public MoveLine clearAnalyticAccounting(MoveLine moveLine) {
    moveLine.setAxis1AnalyticAccount(null);
    moveLine.setAxis2AnalyticAccount(null);
    moveLine.setAxis3AnalyticAccount(null);
    moveLine.setAxis4AnalyticAccount(null);
    moveLine.setAxis5AnalyticAccount(null);
    moveLine
        .getAnalyticMoveLineList()
        .forEach(analyticMoveLine -> analyticMoveLine.setMoveLine(null));
    moveLine.getAnalyticMoveLineList().clear();
    return moveLine;
  }

  @Override
  public MoveLine printAnalyticAccount(MoveLine moveLine) throws AxelorException {
    if (moveLine.getAnalyticMoveLineList() != null
        && !moveLine.getAnalyticMoveLineList().isEmpty()
        && moveLine.getMove() != null
        && moveLine.getMove().getCompany() != null) {
      List<AnalyticMoveLine> analyticMoveLineList = new ArrayList();
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfigService
              .getAccountConfig(moveLine.getMove().getCompany())
              .getAnalyticAxisByCompanyList()) {
        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          if (analyticMoveLine.getAnalyticAxis() == analyticAxisByCompany.getAnalyticAxis()) {
            analyticMoveLineList.add(analyticMoveLine);
          }
        }
        if (analyticMoveLineList.size() == 1
            && analyticMoveLineList.get(0).getPercentage().compareTo(new BigDecimal(100)) == 0) {
          switch (analyticAxisByCompany.getOrderSelect()) {
            case 1:
              moveLine.setAxis1AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 2:
              moveLine.setAxis2AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 3:
              moveLine.setAxis3AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 4:
              moveLine.setAxis4AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            case 5:
              moveLine.setAxis5AnalyticAccount(analyticMoveLineList.get(0).getAnalyticAccount());
              break;
            default:
              break;
          }
        }
        analyticMoveLineList.clear();
      }
    }
    return moveLine;
  }

  public boolean checkAxisAccount(MoveLine moveLine, AnalyticAxis analyticAxis) {
    BigDecimal sum = BigDecimal.ZERO;
    for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
      if (analyticMoveLine.getAnalyticAxis() == analyticAxis) {
        sum = sum.add(analyticMoveLine.getPercentage());
      }
    }

    if (sum.compareTo(new BigDecimal(100)) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public MoveLine checkAnalyticMoveLineForAxis(MoveLine moveLine) {
    if (moveLine.getAxis1AnalyticAccount() != null) {
      if (!checkAxisAccount(moveLine, moveLine.getAxis1AnalyticAccount().getAnalyticAxis())) {
        moveLine.setAxis1AnalyticAccount(null);
      }
    }
    if (moveLine.getAxis2AnalyticAccount() != null) {
      if (!checkAxisAccount(moveLine, moveLine.getAxis2AnalyticAccount().getAnalyticAxis())) {
        moveLine.setAxis2AnalyticAccount(null);
      }
    }
    if (moveLine.getAxis3AnalyticAccount() != null) {
      if (!checkAxisAccount(moveLine, moveLine.getAxis3AnalyticAccount().getAnalyticAxis())) {
        moveLine.setAxis3AnalyticAccount(null);
      }
    }
    if (moveLine.getAxis4AnalyticAccount() != null) {
      if (!checkAxisAccount(moveLine, moveLine.getAxis4AnalyticAccount().getAnalyticAxis())) {
        moveLine.setAxis4AnalyticAccount(null);
      }
    }
    if (moveLine.getAxis5AnalyticAccount() != null) {
      if (!checkAxisAccount(moveLine, moveLine.getAxis5AnalyticAccount().getAnalyticAxis())) {
        moveLine.setAxis5AnalyticAccount(null);
      }
    }
    return moveLine;
  }

  @Override
  public BigDecimal getAnalyticAmount(MoveLine moveLine, AnalyticMoveLine analyticMoveLine) {
    if (moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(moveLine.getCredit())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    } else if (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
      return analyticMoveLine
          .getPercentage()
          .multiply(moveLine.getDebit())
          .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
    }
    return BigDecimal.ZERO;
  }

  @Override
  public BigDecimal getAnalyticAmount(InvoiceLine invoiceLine, AnalyticMoveLine analyticMoveLine) {
    return analyticMoveLine
        .getPercentage()
        .multiply(invoiceLine.getCompanyExTaxTotal())
        .divide(new BigDecimal(100), RETURN_SCALE, RoundingMode.HALF_UP);
  }
}
