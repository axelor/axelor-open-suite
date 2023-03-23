package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import java.util.Objects;
import java.util.Optional;

public class MoveRecordServiceImpl implements MoveRecordService {

  @Override
  public Move setPaymentMode(Move move) {
    Objects.requireNonNull(move);

    Partner partner = move.getPartner();
    JournalType journalType =
        Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);

    if (partner != null && journalType != null) {
      if (journalType
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE)) {
        move.setPaymentMode(partner.getOutPaymentMode());
      } else if (journalType
          .getTechnicalTypeSelect()
          .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)) {
        move.setPaymentMode(partner.getInPaymentMode());
      } else {
        move.setPaymentMode(null);
      }
    } else {
      move.setPaymentMode(null);
    }
    return move;
  }

  @Override
  public Move setPaymentCondition(Move move) {
    Objects.requireNonNull(move);

    Partner partner = move.getPartner();
    JournalType journalType =
        Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);

    if (partner != null
        && journalType != null
        && journalType
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)) {
      move.setPaymentCondition(partner.getPaymentCondition());
    } else {
      move.setPaymentCondition(null);
    }

    return move;
  }

  @Override
  public Move setPartnerBankDetails(Move move) {
    Objects.requireNonNull(move);

    Partner partner = move.getPartner();

    if (partner != null) {
      move.setPartnerBankDetails(
          partner.getBankDetailsList().stream()
              .filter(bankDetails -> bankDetails.getIsDefault() && bankDetails.getActive())
              .findFirst()
              .orElse(null));
    } else {
      move.setPartnerBankDetails(null);
    }
    return move;
  }

  @Override
  public Move setCurrencyByPartner(Move move) {
    Objects.requireNonNull(move);

    Partner partner = move.getPartner();

    if (partner != null) {
      move.setCurrency(partner.getCurrency());
      move.setCurrencyCode(
          Optional.ofNullable(partner.getCurrency()).map(Currency::getCodeISO).orElse(null));
      move.setFiscalPosition(partner.getFiscalPosition());
    }

    return move;
  }

  @Override
  public Move setCurrencyCode(Move move) {
    Objects.requireNonNull(move);

    if (move.getCurrency() != null) {
      move.setCurrencyCode(move.getCurrency().getCodeISO());
    } else {
      move.setCurrencyCode(null);
    }

    return move;
  }

  @Override
  public Move setJournal(Move move) {
    Objects.requireNonNull(move);

    move.setJournal(
        Optional.ofNullable(move.getCompany())
            .map(Company::getAccountConfig)
            .map(AccountConfig::getManualMiscOpeJournal)
            .orElse(null));
    return move;
  }

  @Override
  public Move setFunctionalOriginSelect(Move move) {
    Objects.requireNonNull(move);

    if (move.getJournal() != null
        && move.getJournal().getAuthorizedFunctionalOriginSelect() != null) {
      if (move.getJournal().getAuthorizedFunctionalOriginSelect().split(",").length == 1) {
        move.setFunctionalOriginSelect(
            Integer.valueOf(move.getJournal().getAuthorizedFunctionalOriginSelect()));
      } else {
        move.setFunctionalOriginSelect(null);
      }
    }

    return move;
  }
}
