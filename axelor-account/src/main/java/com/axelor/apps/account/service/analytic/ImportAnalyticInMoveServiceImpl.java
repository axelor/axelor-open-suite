package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticDistributionTemplateRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.common.ObjectUtils;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class ImportAnalyticInMoveServiceImpl implements ImportAnalyticInMoveService {

  protected AnalyticToolService analyticToolService;
  protected AnalyticDistributionTemplateRepository analyticDistributionTemplateRepository;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected AnalyticLineService analyticLineService;
  protected AccountConfigService accountConfigService;

  @Inject
  public ImportAnalyticInMoveServiceImpl(
      AnalyticToolService analyticToolService,
      AnalyticDistributionTemplateRepository analyticDistributionTemplateRepository,
      AnalyticAccountRepository analyticAccountRepository,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      AnalyticLineService analyticLineService,
      AccountConfigService accountConfigService) {
    this.analyticToolService = analyticToolService;
    this.analyticDistributionTemplateRepository = analyticDistributionTemplateRepository;
    this.analyticAccountRepository = analyticAccountRepository;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.analyticLineService = analyticLineService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public MoveLine fillAnalyticOnMoveLine(MoveLine moveLine, Move move, Map<String, Object> values)
      throws AxelorException {
    if (moveLine == null
        || move == null
        || move.getCompany() == null
        || !analyticToolService.isManageAnalytic(move.getCompany())) {
      return moveLine;
    }

    if (values.get("RepartAnalytique") != null && !values.get("RepartAnalytique").equals("")) {
      AnalyticDistributionTemplate analyticDistributionTemplate =
          analyticDistributionTemplateRepository
              .all()
              .filter(
                  "self.name = ?1 AND self.company.id = ?2",
                  values.get("RepartAnalytique").toString(),
                  move.getCompany().getId())
              .fetchOne();
      if (analyticDistributionTemplate != null) {
        moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);
        moveLineComputeAnalyticService.createAnalyticDistributionWithTemplate(moveLine, move);
        analyticLineService.setAnalyticAccount(moveLine, move.getCompany());
      }
    } else {
      fillAnalyticAccountOnMoveLine(moveLine, move, values);
      moveLineComputeAnalyticService.analyzeMoveLine(moveLine, move.getCompany());
    }
    return moveLine;
  }

  protected void fillAnalyticAccountOnMoveLine(
      MoveLine moveLine, Move move, Map<String, Object> values) throws AxelorException {
    if (moveLine == null || move == null || move.getCompany() == null) {
      return;
    }
    AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());
    if (accountConfig == null
        || ObjectUtils.isEmpty(accountConfig.getAnalyticAxisByCompanyList())) {
      return;
    }

    List<AnalyticAxisByCompany> analyticAxisByCompanyList =
        accountConfig.getAnalyticAxisByCompanyList().stream()
            .sorted(Comparator.comparing(AnalyticAxisByCompany::getSequence))
            .collect(Collectors.toList());

    moveLine.setAxis1AnalyticAccount(
        getAnalyticAccountFromValues(analyticAxisByCompanyList, values, move.getCompany(), 1));
    moveLine.setAxis2AnalyticAccount(
        getAnalyticAccountFromValues(analyticAxisByCompanyList, values, move.getCompany(), 2));
    moveLine.setAxis3AnalyticAccount(
        getAnalyticAccountFromValues(analyticAxisByCompanyList, values, move.getCompany(), 3));
    moveLine.setAxis4AnalyticAccount(
        getAnalyticAccountFromValues(analyticAxisByCompanyList, values, move.getCompany(), 4));
    moveLine.setAxis5AnalyticAccount(
        getAnalyticAccountFromValues(analyticAxisByCompanyList, values, move.getCompany(), 5));
  }

  protected AnalyticAccount getAnalyticAccountFromValues(
      List<AnalyticAxisByCompany> analyticAxisByCompanyList,
      Map<String, Object> values,
      Company company,
      Integer i) {
    Object analyticAccount = values.get(String.format("CompteAnalytique%s", i));
    if (analyticAxisByCompanyList.size() >= i
        && analyticAxisByCompanyList.get(i - 1) != null
        && analyticAccount != null
        && !ObjectUtils.isEmpty(analyticAccount)) {
      return analyticAccountRepository
          .all()
          .filter(
              "self.code = ?1 AND self.analyticAxis.id = ?2 AND self.company.id = ?3",
              analyticAccount.toString(),
              Optional.of(analyticAxisByCompanyList.get(i - 1).getAnalyticAxis())
                  .map(AnalyticAxis::getId)
                  .orElse(0L),
              company.getId())
          .fetchOne();
    }
    return null;
  }
}
