package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.AnalyticDistributionTemplateRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
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
  public MoveLine fillAnalyticOnMoveLine(
      MoveLine moveLine, Move move, Map<String, Object> values, String csvReference)
      throws AxelorException {
    if (moveLine == null
        || move == null
        || move.getCompany() == null
        || moveLine.getAccount() == null
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
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.FEC_IMPORT_ANALYTIC_DISTRIBUTION_EMPTY),
            values.get("RepartAnalytique").toString(),
            move.getCompany().getCode(),
            csvReference);
      }
    } else {
      fillAnalyticAccountOnMoveLine(moveLine, move, values, csvReference);
      moveLineComputeAnalyticService.analyzeMoveLine(moveLine, move.getCompany());
    }

    verifyNoAnalyticOnNonAuthorizedAccount(moveLine, csvReference);
    verifyMissingAnalyticOnMoveLine(moveLine, move.getCompany(), csvReference);

    return moveLine;
  }

  protected void fillAnalyticAccountOnMoveLine(
      MoveLine moveLine, Move move, Map<String, Object> values, String csvReference)
      throws AxelorException {
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
        getAnalyticAccountFromValues(
            analyticAxisByCompanyList, values, move.getCompany(), 1, csvReference));
    moveLine.setAxis2AnalyticAccount(
        getAnalyticAccountFromValues(
            analyticAxisByCompanyList, values, move.getCompany(), 2, csvReference));
    moveLine.setAxis3AnalyticAccount(
        getAnalyticAccountFromValues(
            analyticAxisByCompanyList, values, move.getCompany(), 3, csvReference));
    moveLine.setAxis4AnalyticAccount(
        getAnalyticAccountFromValues(
            analyticAxisByCompanyList, values, move.getCompany(), 4, csvReference));
    moveLine.setAxis5AnalyticAccount(
        getAnalyticAccountFromValues(
            analyticAxisByCompanyList, values, move.getCompany(), 5, csvReference));
  }

  protected AnalyticAccount getAnalyticAccountFromValues(
      List<AnalyticAxisByCompany> analyticAxisByCompanyList,
      Map<String, Object> values,
      Company company,
      Integer i,
      String csvReference)
      throws AxelorException {
    Object analyticAccountObj = values.get(String.format("CompteAnalytique%s", i));
    if (analyticAxisByCompanyList.size() >= i
        && analyticAxisByCompanyList.get(i - 1) != null
        && analyticAccountObj != null
        && !ObjectUtils.isEmpty(analyticAccountObj)) {
      AnalyticAccount analyticAccount =
          analyticAccountRepository
              .all()
              .filter(
                  "self.code = ?1 AND self.analyticAxis.id = ?2 AND self.company.id = ?3",
                  analyticAccountObj.toString(),
                  Optional.of(analyticAxisByCompanyList.get(i - 1).getAnalyticAxis())
                      .map(AnalyticAxis::getId)
                      .orElse(0L),
                  company.getId())
              .fetchOne();
      if (analyticAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.FEC_IMPORT_ANALYTIC_ACCOUNT_EMPTY),
            analyticAccountObj.toString(),
            company.getCode(),
            analyticAxisByCompanyList.get(i - 1).getAnalyticAxis().getCode(),
            csvReference);
      }
      return analyticAccount;
    }
    return null;
  }

  protected void verifyMissingAnalyticOnMoveLine(
      MoveLine moveLine, Company company, String csvReference) throws AxelorException {
    Account account = moveLine.getAccount();
    if (account == null
        || !account.getAnalyticDistributionAuthorized()
        || !account.getAnalyticDistributionRequiredOnMoveLines()) {
      return;
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    List<AnalyticAxis> configAnalyticAxisList =
        accountConfig.getAnalyticAxisByCompanyList().stream()
            .map(AnalyticAxisByCompany::getAnalyticAxis)
            .collect(Collectors.toList());
    if (ObjectUtils.isEmpty(configAnalyticAxisList)) {
      return;
    }

    List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();
    for (AnalyticAxis analyticAxis : configAnalyticAxisList) {
      if (ObjectUtils.isEmpty(analyticMoveLineList)
          || analyticMoveLineList.stream()
                  .filter(aml -> aml.getAnalyticAxis().equals(analyticAxis))
                  .map(AnalyticMoveLine::getPercentage)
                  .reduce(BigDecimal.ZERO, BigDecimal::add)
                  .compareTo(new BigDecimal(100))
              != 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.FEC_IMPORT_ANALYTIC_MISSING_REQUIRED),
            moveLine.getAccount().getCode(),
            csvReference);
      }
    }
  }

  protected void verifyNoAnalyticOnNonAuthorizedAccount(MoveLine moveLine, String csvReference)
      throws AxelorException {
    Account account = moveLine.getAccount();
    if (account == null || account.getAnalyticDistributionAuthorized()) {
      return;
    }

    if (!ObjectUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.FEC_IMPORT_ANALYTIC_NOT_AUTHORIZED),
          moveLine.getAccount().getCode(),
          csvReference);
    }
  }
}
