package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

  @Inject
  public MassEntryVerificationServiceImpl(
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      PeriodService periodService,
      MoveLineToolService moveLineToolService,
      MoveToolService moveToolService,
      MoveLineControlService moveLineControlService) {
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.periodService = periodService;
    this.moveLineToolService = moveLineToolService;
    this.moveToolService = moveToolService;
    this.moveLineControlService = moveLineControlService;
  }

  public void checkAndReplaceDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate, Move move) throws AxelorException {
    if (!moveLineMassEntry.getDate().equals(newDate)) {
      moveLineMassEntry.setDate(newDate);

      if (moveLineComputeAnalyticService.checkManageAnalytic(move.getCompany())) {
        moveLineMassEntry.setAnalyticMoveLineList(
            moveLineComputeAnalyticService
                .computeAnalyticDistribution(moveLineMassEntry)
                .getAnalyticMoveLineList());
        Period period = null;
        if (move.getDate() != null && move.getCompany() != null) {
          period =
              periodService.getActivePeriod(
                  move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL);
          move.setPeriod(period);
        }
        moveLineToolService.checkDateInPeriod(move, moveLineMassEntry);
      }
    }
  }

  public void checkAndReplaceOriginDateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, LocalDate newDate) {
    if (newDate != null && !newDate.equals(moveLineMassEntry.getOriginDate())) {
      moveLineMassEntry.setOriginDate(newDate);
      moveLineMassEntry.setCutOffStartDate(newDate);
      moveLineMassEntry.setCutOffEndDate(null);
    }
  }

  public void checkAndReplaceOriginInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newOrigin) {
    if (!newOrigin.equals(moveLineMassEntry.getOrigin())) {
      moveLineMassEntry.setOrigin(newOrigin);
    }
  }

  public void checkAndReplaceMoveDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newMoveDescription) {
    if (!newMoveDescription.equals(moveLineMassEntry.getMoveDescription())) {
      this.checkAndReplaceDescriptionInMoveLineMassEntry(moveLineMassEntry, newMoveDescription);
      moveLineMassEntry.setMoveDescription(newMoveDescription);
    }
  }

  public void checkAndReplaceDescriptionInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, String newDescription) {
    if (moveLineMassEntry.getMoveDescription().equals(moveLineMassEntry.getDescription())) {
      moveLineMassEntry.setDescription(newDescription);
    }
  }

  public void checkAndReplaceMovePaymentModeInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, PaymentMode newMovePaymentMode) {
    if (!newMovePaymentMode.equals(moveLineMassEntry.getMovePaymentMode())) {
      moveLineMassEntry.setMovePaymentMode(newMovePaymentMode);
    }
  }

  public void checkAndReplaceCurrencyRateInMoveLineMassEntry(
      MoveLineMassEntry moveLineMassEntry, BigDecimal newCurrencyRate) {
    if (!newCurrencyRate.equals(moveLineMassEntry.getCurrencyRate())) {
      moveLineMassEntry.setCurrencyRate(newCurrencyRate);
    }
  }

  public void checkDateInAllMoveLineMassEntry(Move move) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();
    boolean hasDateError;

    MoveLineMassEntry firstMoveLineMassEntry = move.getMoveLineMassEntryList().get(0);

    for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
      hasDateError = false;
      if (move.getPeriod() == null) {
        hasDateError = true;
        // TODO Set an error message
        this.setMassEntryErrorMessage(move, "Period does not exist in move : ", true);
      } else {
        if (move.getPeriod().getStatusSelect() == PeriodRepository.STATUS_CLOSED) {
          hasDateError = true;
          // TODO Set an error message
          this.setMassEntryErrorMessage(move, "Period closed in move : ", true);
        } else if (move.getPeriod().getStatusSelect()
            == PeriodRepository.STATUS_CLOSURE_IN_PROGRESS) {
          hasDateError = true;
          // TODO Set an error message
          this.setMassEntryErrorMessage(move, "Period in closure progress in move : ", true);
        }
      }

      if (!firstMoveLineMassEntry.getDate().equals(moveLineMassEntry.getDate())) {
        hasDateError = true;
        // TODO Set an error message
        this.setMassEntryErrorMessage(
            move, "Different dates in move : ", ObjectUtils.notEmpty(differentElements));
      }

      if (hasDateError) {
        this.setFieldsErrorListMessage(moveLineMassEntry, differentElements, "date");
      }
    }
  }

  public void checkCurrencyRateInAllMoveLineMassEntry(Move move) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();

    for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
      if (BigDecimal.ZERO
          .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS)
          .equals(
              moveLineMassEntry
                  .getCurrencyRate()
                  .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP))) {
        this.setFieldsErrorListMessage(moveLineMassEntry, differentElements, "currencyRate");
      }
    }
    // TODO Set an error message
    this.setMassEntryErrorMessage(
        move, "Currency Rate is 0.00 in move : ", ObjectUtils.notEmpty(differentElements));
  }

  public void checkOriginDateInAllMoveLineMassEntry(Move move) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();

    MoveLineMassEntry firstMoveLineMassEntry = move.getMoveLineMassEntryList().get(0);
    for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
      if (!firstMoveLineMassEntry.getOriginDate().equals(moveLineMassEntry.getOriginDate())) {
        this.setFieldsErrorListMessage(moveLineMassEntry, differentElements, "originDate");
      }
    }
    // TODO Set an error message
    this.setMassEntryErrorMessage(
        move, "Different origin dates in move : ", ObjectUtils.notEmpty(differentElements));
  }

  public void checkOriginInAllMoveLineMassEntry(Move move) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();

    for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
      if (move.getJournal() != null
          && move.getPartner() != null
          && move.getJournal().getHasDuplicateDetectionOnOrigin()) {
        List<Move> moveList = moveToolService.getMovesWithDuplicatedOrigin(move);
        if (ObjectUtils.notEmpty(moveList)) {
          this.setFieldsErrorListMessage(moveLineMassEntry, differentElements, "origin");
        }
      }
    }
    // TODO Set an error message
    this.setMassEntryErrorMessage(
        move, "This origin already exist for the move : ", ObjectUtils.notEmpty(differentElements));
  }

  public void checkPartnerInAllMoveLineMassEntry(Move move) {
    List<MoveLineMassEntry> differentElements = new ArrayList<>();
    int[] technicalTypeSelectArray = {
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE,
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE,
      JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE
    };

    if (move.getJournal() != null
        && ArrayUtils.contains(
            technicalTypeSelectArray,
            move.getJournal().getJournalType().getTechnicalTypeSelect())) {
      for (MoveLineMassEntry moveLineMassEntry : move.getMoveLineMassEntryList()) {
        try {
          moveLineControlService.checkPartner(moveLineMassEntry);
        } catch (AxelorException e) {
          this.setFieldsErrorListMessage(moveLineMassEntry, differentElements, "partner");
        }
      }
      // TODO Set an error message
      this.setMassEntryErrorMessage(
          move, "Multiple partners for the move : ", ObjectUtils.notEmpty(differentElements));
    }
  }

  private void setMassEntryErrorMessage(Move move, String message, boolean toSet) {
    if (toSet) {
      move.setMassEntryErrors(
          move.getMassEntryErrors()
              + message
              + move.getMoveLineMassEntryList().get(0).getTemporaryMoveNumber()
              + '\n');
    }
  }

  private void setFieldsErrorListMessage(
      MoveLineMassEntry moveLineMassEntry,
      List<MoveLineMassEntry> differentElements,
      String fieldName) {
    String message = "";
    if (ObjectUtils.notEmpty(moveLineMassEntry.getFieldsErrorList())) {
      message += moveLineMassEntry.getFieldsErrorList() + ";";
    }
    message += fieldName + ":";

    switch (fieldName) {
      case "date":
        message += moveLineMassEntry.getDate().toString();
        break;
      case "currencyRate":
        message += moveLineMassEntry.getCurrencyRate().toString();
        break;
      case "originDate":
        message += moveLineMassEntry.getOriginDate().toString();
        break;
      case "origin":
        message += moveLineMassEntry.getOrigin();
        break;
      case "partner":
        message += moveLineMassEntry.getPartner().getName();
        break;
      default:
        break;
    }

    moveLineMassEntry.setFieldsErrorList(message);
    differentElements.add(moveLineMassEntry);
  }

  public void setPfpValidatorOnInTaxLines(Move move) {
    // TODO set pfp validator inTax lines using partner.pfpValidatorUser
  }
}
