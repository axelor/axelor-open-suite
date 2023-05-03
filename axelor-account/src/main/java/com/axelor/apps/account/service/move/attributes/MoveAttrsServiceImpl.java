package com.axelor.apps.account.service.move.attributes;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveInvoiceTermService;
import com.axelor.apps.account.service.move.MovePfpService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MoveAttrsServiceImpl implements MoveAttrsService {

  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected AppAccountService appAccountService;
  protected MoveInvoiceTermService moveInvoiceTermService;
  protected MoveRepository moveRepository;
  protected MovePfpService movePfpService;

  @Inject
  public MoveAttrsServiceImpl(
      AppBaseService appBaseService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      MoveInvoiceTermService moveInvoiceTermService,
      MoveRepository moveRepository,
      MovePfpService movePfpService) {
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.appAccountService = appAccountService;
    this.moveInvoiceTermService = moveInvoiceTermService;
    this.moveRepository = moveRepository;
    this.movePfpService = movePfpService;
  }

  @Override
  public Map<String, Map<String, Object>> getHiddenAttributeValues(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();

    mapResult.put("moveLineList.counter", new HashMap<>());
    mapResult.put("moveLineList.amountRemaining", new HashMap<>());
    mapResult.put("moveLineList.reconcileGroup", new HashMap<>());
    mapResult.put("moveLineList.partner", new HashMap<>());

    mapResult.get("moveLineList.partner").put("hidden", move.getPartner() != null);
    mapResult
        .get("moveLineList.counter")
        .put(
            "hidden",
            move.getStatusSelect() == null || move.getStatusSelect() == MoveRepository.STATUS_NEW);
    mapResult
        .get("moveLineList.amountRemaining")
        .put(
            "hidden",
            move.getStatusSelect() == null
                || move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED
                || move.getStatusSelect() == MoveRepository.STATUS_SIMULATED);
    mapResult
        .get("moveLineList.reconcileGroup")
        .put(
            "hidden",
            move.getStatusSelect() == MoveRepository.STATUS_NEW
                || move.getStatusSelect() == MoveRepository.STATUS_CANCELED);
    return mapResult;
  }

  @Override
  public boolean isHiddenMoveLineListViewer(Move move) {
    boolean isHidden = true;
    if (move.getMoveLineList() != null
        && move.getStatusSelect() < MoveRepository.STATUS_ACCOUNTED) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLine.getAmountPaid().compareTo(BigDecimal.ZERO) > 0
            || moveLine.getReconcileGroup() != null) {
          isHidden = false;
        }
      }
    }
    return isHidden;
  }

  @Override
  public Map<String, Map<String, Object>> getFunctionalOriginSelectDomain(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();
    mapResult.put("functionalOriginSelect", new HashMap<>());

    String selectionValue = null;

    if (move.getJournal() != null) {
      selectionValue =
          Optional.ofNullable(move.getJournal().getAuthorizedFunctionalOriginSelect()).orElse("0");
    }
    mapResult.get("functionalOriginSelect").put("selection-in", selectionValue);

    return mapResult;
  }

  @Override
  public Map<String, Map<String, Object>> getMoveLineAnalyticAttrs(Move move)
      throws AxelorException {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> resultMap = new HashMap<>();

    if (move.getCompany() != null) {
      AccountConfig accountConfig = accountConfigService.getAccountConfig(move.getCompany());
      if (accountConfig != null
          && appAccountService.getAppAccount().getManageAnalyticAccounting()
          && accountConfig.getManageAnalyticAccounting()) {
        AnalyticAxis analyticAxis = null;
        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = "moveLineList.axis" + i + "AnalyticAccount";
          resultMap.put(analyticAxisKey, new HashMap<>());
          resultMap
              .get(analyticAxisKey)
              .put("hidden", !(i <= accountConfig.getNbrOfAnalyticAxisSelect()));
          for (AnalyticAxisByCompany analyticAxisByCompany :
              accountConfig.getAnalyticAxisByCompanyList()) {
            if (analyticAxisByCompany.getSequence() + 1 == i) {
              analyticAxis = analyticAxisByCompany.getAnalyticAxis();
            }
          }
          if (analyticAxis != null) {
            resultMap.get(analyticAxisKey).put("title", analyticAxis.getName());
            analyticAxis = null;
          }
        }
      } else {
        resultMap.put("moveLineList.analyticDistributionTemplate", new HashMap<>());
        resultMap.get("moveLineList.analyticDistributionTemplate").put("hidden", true);
        resultMap.put("moveLineList.analyticMoveLineList", new HashMap<>());
        resultMap.get("moveLineList.analyticMoveLineList").put("hidden", true);
        for (int i = 1; i <= 5; i++) {
          String analyticAxisKey = "moveLineList.axis" + i + "AnalyticAccount";
          resultMap.put(analyticAxisKey, new HashMap<>());
          resultMap.get(analyticAxisKey).put("hidden", true);
        }
      }
    }
    return resultMap;
  }

  @Override
  public boolean isHiddenDueDate(Move move) {

    return !moveInvoiceTermService.displayDueDate(move);
  }

  @Override
  public Map<String, Map<String, Object>> getPfpAttrs(Move move, User user) throws AxelorException {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> resultMap = new HashMap<>();

    resultMap.put("passedForPaymentValidationBtn", new HashMap<>());
    resultMap.put("refusalToPayBtn", new HashMap<>());
    resultMap.put("pfpValidatorUser", new HashMap<>());

    resultMap
        .get("passedForPaymentValidationBtn")
        .put("hidden", !movePfpService.isPfpButtonVisible(move, user, true));
    resultMap
        .get("refusalToPayBtn")
        .put("hidden", !movePfpService.isPfpButtonVisible(move, user, false));
    resultMap.get("pfpValidatorUser").put("hidden", !movePfpService.isValidatorUserVisible(move));

    return resultMap;
  }

  @Override
  public Map<String, Map<String, Object>> getMassEntryHiddenAttributeValues(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();
    boolean technicalTypeSelectIsNotNull =
        move.getJournal() != null
            && move.getJournal().getJournalType() != null
            && move.getJournal().getJournalType().getTechnicalTypeSelect() != null;
    boolean isSameCurrency =
        move.getCompany() != null && move.getCompany().getCurrency() == move.getCurrency();

    mapResult.put("moveLineMassEntryList.originDate", new HashMap<>());
    mapResult.put("moveLineMassEntryList.origin", new HashMap<>());
    mapResult.put("moveLineMassEntryList.movePaymentMode", new HashMap<>());
    mapResult.put("moveLineMassEntryList.currencyRate", new HashMap<>());
    mapResult.put("moveLineMassEntryList.currencyAmount", new HashMap<>());
    mapResult.put("moveLineMassEntryList.pfpValidatorUser", new HashMap<>());
    mapResult.put("moveLineMassEntryList.cutOffStartDate", new HashMap<>());
    mapResult.put("moveLineMassEntryList.cutOffEndDate", new HashMap<>());
    mapResult.put("moveLineMassEntryList.deliveryDate", new HashMap<>());

    mapResult
        .get("moveLineMassEntryList.originDate")
        .put(
            "hidden",
            technicalTypeSelectIsNotNull
                && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                    || move.getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER));
    mapResult
        .get("moveLineMassEntryList.origin")
        .put(
            "hidden",
            technicalTypeSelectIsNotNull
                && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                    || move.getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER));
    mapResult
        .get("moveLineMassEntryList.movePaymentMode")
        .put(
            "hidden",
            technicalTypeSelectIsNotNull
                && move.getJournal().getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER);
    mapResult.get("moveLineMassEntryList.currencyRate").put("hidden", isSameCurrency);
    mapResult.get("moveLineMassEntryList.currencyAmount").put("hidden", isSameCurrency);
    mapResult
        .get("moveLineMassEntryList.pfpValidatorUser")
        .put(
            "hidden",
            technicalTypeSelectIsNotNull
                && move.getJournal().getJournalType().getTechnicalTypeSelect()
                    != JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE);
    mapResult
        .get("moveLineMassEntryList.cutOffStartDate")
        .put("hidden", !move.getMassEntryManageCutOff());
    mapResult
        .get("moveLineMassEntryList.cutOffEndDate")
        .put("hidden", !move.getMassEntryManageCutOff());
    mapResult
        .get("moveLineMassEntryList.deliveryDate")
        .put(
            "hidden",
            !move.getMassEntryManageCutOff()
                && technicalTypeSelectIsNotNull
                && (move.getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY
                    || move.getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER));

    return mapResult;
  }

  @Override
  public Map<String, Map<String, Object>> getMassEntryRequiredAttributeValues(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();

    mapResult.put("moveLineMassEntryList.movePaymentCondition", new HashMap<>());
    mapResult
        .get("moveLineMassEntryList.movePaymentCondition")
        .put(
            "required",
            move.getJournal() != null
                && move.getJournal().getJournalType() != null
                && move.getJournal().getJournalType().getTechnicalTypeSelect() != null
                && move.getJournal().getJournalType().getTechnicalTypeSelect()
                    < JournalTypeRepository.TECHNICAL_TYPE_SELECT_OTHER);

    return mapResult;
  }

  @Override
  public Map<String, Map<String, Object>> getMassEntryBtnHiddenAttributeValues(Move move) {
    Objects.requireNonNull(move);
    Map<String, Map<String, Object>> mapResult = new HashMap<>();

    mapResult.put("controlMassEntryMoves", new HashMap<>());
    mapResult.put("validateMassEntryMoves", new HashMap<>());

    mapResult.get("controlMassEntryMoves").put("hidden", false);
    mapResult.get("validateMassEntryMoves").put("hidden", true);

    return mapResult;
  }
}
