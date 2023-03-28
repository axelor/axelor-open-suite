package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineMassEntryRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveControlService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MassEntryVerificationServiceImpl implements MassEntryVerificationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected PeriodService periodService;
  protected MoveLineToolService moveLineToolService;
  protected MoveToolService moveToolService;
  protected MoveLineControlService moveLineControlService;
  protected MoveValidateService moveValidateService;
  protected MoveControlService moveControlService;

  @Inject
  public MassEntryVerificationServiceImpl(
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodService periodService,
      MoveLineToolService moveLineToolService,
      MoveToolService moveToolService,
      MoveLineControlService moveLineControlService,
      MoveValidateService moveValidateService,
      MoveControlService moveControlService) {
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodService = periodService;
    this.moveLineToolService = moveLineToolService;
    this.moveToolService = moveToolService;
    this.moveLineControlService = moveLineControlService;
    this.moveValidateService = moveValidateService;
    this.moveControlService = moveControlService;
  }

  @Override
  public void checkAndReplaceFieldsInMoveLineMassEntry(
      MoveLineMassEntry moveLine,
      Move parentMove,
      MoveLineMassEntry newMoveLine,
      boolean manageCutOff)
      throws AxelorException {

    // Check move line mass entry date
    LocalDate newDate = newMoveLine.getDate();
    Company company = parentMove.getCompany();
    if (!moveLine.getDate().equals(newDate)) {
      moveLine.setDate(newDate);

      Period period = null;
      if (newDate != null && company != null) {
        period = periodService.getActivePeriod(newDate, company, YearRepository.TYPE_FISCAL);
        parentMove.setPeriod(period);
      }
      moveLineToolService.checkDateInPeriod(parentMove, moveLine);
    }

    // Check move line mass entry originDate
    LocalDate newOriginDate = newMoveLine.getOriginDate();
    if (!newMoveLine.getOriginDate().equals(moveLine.getOriginDate())) {
      moveLine.setOriginDate(newOriginDate);
      if (manageCutOff) {
        moveLine.setCutOffStartDate(newOriginDate);
        moveLine.setCutOffEndDate(newOriginDate);
      }
    }

    // Check move line mass entry origin
    String newOrigin = newMoveLine.getOrigin() != null ? newMoveLine.getOrigin() : "";
    if (!newOrigin.equals(moveLine.getOrigin())) {
      moveLine.setOrigin(newOrigin);
    }

    // Check move line mass entry move description
    String newMoveDescription =
        newMoveLine.getMoveDescription() != null ? newMoveLine.getMoveDescription() : "";
    if (!newMoveDescription.equals(moveLine.getMoveDescription())) {
      if (moveLine.getMoveDescription().equals(moveLine.getDescription())) {
        moveLine.setDescription(newMoveDescription);
      }
      moveLine.setMoveDescription(newMoveDescription);
    }

    // Check move line mass entry payment mode
    if (newMoveLine.getAccount() != null && !newMoveLine.getAccount().getHasInvoiceTerm()) {
      PaymentMode newMovePaymentMode = newMoveLine.getMovePaymentMode();
      if (moveLine.getMovePaymentMode() == null
          || !moveLine.getMovePaymentMode().equals(newMovePaymentMode)) {
        moveLine.setMovePaymentMode(newMovePaymentMode);
      }
    }

    // Check move line mass entry currency rate
    BigDecimal newCurrencyRate = newMoveLine.getCurrencyRate();
    if (!newCurrencyRate.equals(moveLine.getCurrencyRate())) {
      moveLine.setCurrencyRate(newCurrencyRate);
    }

    // Check move line mass entry payment condition
    PaymentCondition newPaymentCondition = newMoveLine.getMovePaymentCondition();
    if (moveLine.getMovePaymentCondition() == null
        || !moveLine.getMovePaymentCondition().equals(newPaymentCondition)) {
      moveLine.setMovePaymentCondition(newPaymentCondition);
    }

    // Check move line mass entry VatSystemSelect
    int newVatSystemSelect = newMoveLine.getVatSystemSelect();
    if (!moveLine.getVatSystemSelect().equals(newVatSystemSelect)
        && moveLine.getIsEdited() == MoveLineMassEntryRepository.MASS_ENTRY_IS_EDITED_ALL) {
      moveLine.setVatSystemSelect(newVatSystemSelect);
    }
  }

  @Override
  public void checkDateInAllMoveLineMassEntry(Move move, int temporaryMoveNumber) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();
    boolean hasDateError;

    MoveLineMassEntry firstMoveLine = move.getMoveLineMassEntryList().get(0);

    for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
      hasDateError = false;
      if (move.getPeriod() == null) {
        hasDateError = true;
        this.setMassEntryErrorMessage(
            move,
            String.format(BaseExceptionMessage.PERIOD_1, move.getCompany(), move.getDate()),
            true,
            temporaryMoveNumber);
      } else {
        if (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSED
            || move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS) {
          hasDateError = true;
          this.setMassEntryErrorMessage(
              move, AccountExceptionMessage.MOVE_PERIOD_IS_CLOSED, true, temporaryMoveNumber);
        }
      }

      if (!firstMoveLine.getDate().equals(moveLine.getDate())) {
        hasDateError = true;
        // TODO Set an error message
        this.setMassEntryErrorMessage(
            move, "Different dates", ObjectUtils.notEmpty(differentElements), temporaryMoveNumber);
      }

      if (hasDateError) {
        this.setFieldsErrorListMessage(moveLine, differentElements, "date");
      }
    }
  }

  @Override
  public void checkCurrencyRateInAllMoveLineMassEntry(Move move, int temporaryMoveNumber) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();

    for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
      if (BigDecimal.ZERO
          .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS)
          .equals(
              moveLine
                  .getCurrencyRate()
                  .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP))) {
        this.setFieldsErrorListMessage(moveLine, differentElements, "currencyRate");
      }
    }
    // TODO Set an error message
    this.setMassEntryErrorMessage(
        move,
        "Currency Rate is 0.00",
        ObjectUtils.notEmpty(differentElements),
        temporaryMoveNumber);
  }

  @Override
  public void checkOriginDateInAllMoveLineMassEntry(Move move, int temporaryMoveNumber) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();

    MoveLineMassEntry firstMoveLine = move.getMoveLineMassEntryList().get(0);
    for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
      if (!firstMoveLine.getOriginDate().equals(moveLine.getOriginDate())) {
        this.setFieldsErrorListMessage(moveLine, differentElements, "originDate");
      }
    }
    // TODO Set an error message
    this.setMassEntryErrorMessage(
        move,
        "Different origin dates",
        ObjectUtils.notEmpty(differentElements),
        temporaryMoveNumber);
  }

  @Override
  public void checkOriginInAllMoveLineMassEntry(Move move, int temporaryMoveNumber) {
    try {
      moveControlService.checkDuplicateOrigin(move);
    } catch (AxelorException e) {
      for (MoveLineMassEntry element : move.getMoveLineMassEntryList()) {
        if (Objects.equals(element.getTemporaryMoveNumber(), temporaryMoveNumber)) {
          this.setFieldsErrorListMessage(element, null, "origin");
        }
      }
      this.setMassEntryErrorMessage(move, e.getMessage(), true, temporaryMoveNumber);
    }
  }

  @Override
  public void checkPartnerInAllMoveLineMassEntry(Move move, int temporaryMoveNumber) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();
    int[] technicalTypeSelectArray = {
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE,
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
    };
    StringBuilder differentPartner = new StringBuilder();

    if (move.getJournal() != null
        && ArrayUtils.contains(
            technicalTypeSelectArray,
            move.getJournal().getJournalType().getTechnicalTypeSelect())) {
      for (MoveLineMassEntry moveLine : move.getMoveLineMassEntryList()) {
        try {
          moveLineControlService.checkPartner(moveLine);
        } catch (AxelorException e) {
          this.setFieldsErrorListMessage(moveLine, differentElements, "partner");
          differentPartner.append(",").append(moveLine.getPartner().getName());
        }
      }
      this.setMassEntryErrorMessage(
          move,
          String.format(
              AccountExceptionMessage.MOVE_LINE_INCONSISTENCY_DETECTED_PARTNER,
              differentPartner,
              move.getPartner()),
          ObjectUtils.notEmpty(differentElements),
          temporaryMoveNumber);
    }
  }

  private void setMassEntryErrorMessage(
      Move move, String message, boolean toSet, int temporaryMoveNumber) {
    String massEntryErrors = move.getMassEntryErrors();
    StringBuilder finalMessage = new StringBuilder();

    if (toSet) {
      if (ObjectUtils.isEmpty(massEntryErrors)) {
        // TODO Set an error message
        finalMessage.append(
            String.format("There is the following errors in move : %s\n", temporaryMoveNumber));
      }
      finalMessage.append(message).append('\n');
      move.setMassEntryErrors(massEntryErrors + finalMessage);
    }
  }

  private void setFieldsErrorListMessage(
      MoveLineMassEntry moveLine, List<MoveLineMassEntry> differentElements, String fieldName) {
    StringBuilder message = new StringBuilder();

    if (ObjectUtils.notEmpty(moveLine.getFieldsErrorList())) {
      message.append(moveLine.getFieldsErrorList()).append(';');
    }
    message.append(fieldName).append(':');

    switch (fieldName) {
      case "date":
        message.append(moveLine.getDate().toString());
        break;
      case "currencyRate":
        message.append(moveLine.getCurrencyRate().toString());
        break;
      case "originDate":
        message.append(moveLine.getOriginDate().toString());
        break;
      case "origin":
        message.append(moveLine.getOrigin());
        break;
      case "partner":
        message.append(moveLine.getPartner().getName());
        break;
    }

    moveLine.setFieldsErrorList(message.toString());
    if (differentElements != null) {
      differentElements.add(moveLine);
    }
  }

  @Override
  public void setPfpValidatorOnInTaxLines(Move move, int temporaryMoveNumber) {
    // TODO set pfp validator inTax lines using partner.pfpValidatorUser
  }

  @Override
  public void checkWellBalancedMove(Move move, int temporaryMoveNumber) {
    try {
      moveValidateService.validateWellBalancedMove(move);
    } catch (AxelorException e) {
      for (MoveLineMassEntry element : move.getMoveLineMassEntryList()) {
        if (Objects.equals(element.getTemporaryMoveNumber(), temporaryMoveNumber)) {
          this.setFieldsErrorListMessage(element, null, "balance");
        }
      }
      this.setMassEntryErrorMessage(move, e.getMessage(), true, temporaryMoveNumber);
    }
  }

  public void checkAccountAnalytic(Move move, int temporaryMoveNumber) {
    String message = "";
    int lineCount = 0;

    for (MoveLine moveLine : move.getMoveLineList()) {
      try {
        moveLineControlService.checkAccountAnalytic(move, moveLine, moveLine.getAccount());
      } catch (AxelorException e) {
        lineCount++;
        for (MoveLineMassEntry element : move.getMoveLineMassEntryList()) {
          if (Objects.equals(element.getTemporaryMoveNumber(), temporaryMoveNumber)
              && Objects.equals(element.getCounter(), moveLine.getCounter())) {
            this.setFieldsErrorListMessage(element, null, "analytic:");
            break;
          }
        }
        if (Objects.equals(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getCategory())) {
          message = String.format(AccountExceptionMessage.MOVE_11, lineCount);
        } else if (Objects.equals(TraceBackRepository.CATEGORY_MISSING_FIELD, e.getCategory())) {
          message =
              String.format(AccountExceptionMessage.MOVE_10, moveLine.getAccount(), lineCount);
        }
        this.setMassEntryErrorMessage(move, message, true, temporaryMoveNumber);
      }
    }
  }
}
