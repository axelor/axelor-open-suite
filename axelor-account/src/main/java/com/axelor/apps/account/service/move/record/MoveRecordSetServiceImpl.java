package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MoveRecordSetServiceImpl implements MoveRecordSetService {

  protected MoveLineControlService moveLineControlService;
  protected PartnerRepository partnerRepository;
  protected BankDetailsService bankDetailsService;
  protected MoveToolService moveToolService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public MoveRecordSetServiceImpl(
      MoveLineControlService moveLineControlService,
      PartnerRepository partnerRepository,
      BankDetailsService bankDetailsService,
      MoveToolService moveToolService,
      InvoiceTermService invoiceTermService) {
    this.moveLineControlService = moveLineControlService;
    this.partnerRepository = partnerRepository;
    this.bankDetailsService = bankDetailsService;
    this.moveToolService = moveToolService;
    this.invoiceTermService = invoiceTermService;
  }

  @Override
  public Map<String, Object> setPeriod(Move move) throws AxelorException {
    Objects.requireNonNull(move);

    HashMap<String, Object> resultMap = new HashMap<>();
    if (move.getDate() != null && move.getCompany() != null) {
      move.setPeriod(
          Beans.get(PeriodService.class)
              .getActivePeriod(move.getDate(), move.getCompany(), YearRepository.TYPE_FISCAL));
    }
    resultMap.put("period", move.getPeriod());
    return resultMap;
  }

  @Override
  public Map<String, Object> setPaymentMode(Move move) {
    Objects.requireNonNull(move);

    HashMap<String, Object> resultMap = new HashMap<>();

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
    resultMap.put("paymentMode", move.getPaymentMode());
    return resultMap;
  }

  @Override
  public Map<String, Object> setPaymentCondition(Move move) {
    Objects.requireNonNull(move);

    HashMap<String, Object> resultMap = new HashMap<>();

    Partner partner = move.getPartner();
    JournalType journalType =
        Optional.ofNullable(move.getJournal()).map(Journal::getJournalType).orElse(null);

    if (partner != null
        && journalType != null
        && !journalType
            .getTechnicalTypeSelect()
            .equals(JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)) {
      move.setPaymentCondition(partner.getPaymentCondition());
    } else {
      move.setPaymentCondition(null);
    }

    resultMap.put("paymentCondition", move.getPaymentCondition());
    return resultMap;
  }

  @Override
  public Map<String, Object> setPartnerBankDetails(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();
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
    resultMap.put("partnerBankDetails", move.getPartnerBankDetails());
    return resultMap;
  }

  @Override
  public Map<String, Object> setCurrencyByPartner(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    Partner partner = move.getPartner();

    if (partner != null) {
      move.setCurrency(partner.getCurrency());
      move.setCurrencyCode(
          Optional.ofNullable(partner.getCurrency()).map(Currency::getCodeISO).orElse(null));
      move.setFiscalPosition(partner.getFiscalPosition());
    }
    resultMap.put("currency", move.getCurrency());
    resultMap.put("currencyCode", move.getCurrencyCode());
    resultMap.put("fiscalPosition", move.getFiscalPosition());
    return resultMap;
  }

  @Override
  public Map<String, Object> setCurrencyCode(Move move) {
    Objects.requireNonNull(move);

    HashMap<String, Object> resultMap = new HashMap<>();
    if (move.getCurrency() != null) {
      move.setCurrencyCode(move.getCurrency().getCodeISO());
    } else {
      move.setCurrencyCode(null);
    }

    resultMap.put("currencyCode", move.getCurrencyCode());

    return resultMap;
  }

  @Override
  public Map<String, Object> setJournal(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();
    move.setJournal(
        Optional.ofNullable(move.getCompany())
            .map(Company::getAccountConfig)
            .map(AccountConfig::getManualMiscOpeJournal)
            .orElse(null));
    resultMap.put("journal", move.getJournal());
    return resultMap;
  }

  @Override
  public Map<String, Object> setFunctionalOriginSelect(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();
    if (move.getJournal() != null
        && move.getJournal().getAuthorizedFunctionalOriginSelect() != null) {
      if (move.getJournal().getAuthorizedFunctionalOriginSelect().split(",").length == 1) {
        move.setFunctionalOriginSelect(
            Integer.valueOf(move.getJournal().getAuthorizedFunctionalOriginSelect()));
      } else {
        if (move.getMassEntryStatusSelect() != MoveRepository.MASS_ENTRY_STATUS_NULL
            && Arrays.stream(move.getJournal().getAuthorizedFunctionalOriginSelect().split(","))
                .findFirst()
                .isPresent()) {
          move.setFunctionalOriginSelect(
              Integer.valueOf(
                  Arrays.stream(move.getJournal().getAuthorizedFunctionalOriginSelect().split(","))
                      .findFirst()
                      .get()));
        } else {
          move.setFunctionalOriginSelect(null);
        }
      }
    }
    resultMap.put("functionalOriginSelect", move.getFunctionalOriginSelect());
    return resultMap;
  }

  @Override
  public Map<String, Object> setMoveLineDates(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    moveLineControlService.setMoveLineDates(move);
    resultMap.put("moveLineList", move.getMoveLineList());

    return resultMap;
  }

  @Override
  public Map<String, Object> setCompanyBankDetails(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    PaymentMode paymentMode = move.getPaymentMode();
    Company company = move.getCompany();
    Partner partner = move.getPartner();
    if (company == null) {
      move.setCompanyBankDetails(null);
      resultMap.put("companyBankDetails", null);
      return resultMap;
    }
    if (partner != null) {
      partner = partnerRepository.find(partner.getId());
    }
    BankDetails defaultBankDetails =
        bankDetailsService.getDefaultCompanyBankDetails(company, paymentMode, partner, null);
    move.setCompanyBankDetails(defaultBankDetails);
    resultMap.put("companyBankDetails", defaultBankDetails);

    return resultMap;
  }

  @Override
  public Map<String, Object> setMoveLineOriginDates(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    moveLineControlService.setMoveLineOriginDates(move);
    resultMap.put("moveLineList", move.getMoveLineList());

    return resultMap;
  }

  @Override
  public Map<String, Object> setOriginOnMoveLineList(Move move) throws AxelorException {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    moveToolService.setOriginOnMoveLineList(move);
    resultMap.put("moveLineList", move.getMoveLineList());

    return resultMap;
  }

  @Override
  public Map<String, Object> setOriginDate(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();
    if (move.getDate() != null
        && move.getJournal() != null
        && move.getJournal().getIsFillOriginDate()) {
      resultMap.put("originDate", move.getDate());
    }
    return resultMap;
  }

  public Map<String, Object> setPfpStatus(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();
    JournalType journalType = move.getJournal().getJournalType();

    if (move.getCompany() != null
        && move.getCompany().getAccountConfig() != null
        && move.getCompany().getAccountConfig().getIsManagePassedForPayment()
        && move.getCompany().getAccountConfig().getIsManagePFPInRefund()
        && (journalType.getTechnicalTypeSelect()
                == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
            || journalType.getTechnicalTypeSelect()
                == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE)) {
      move.setPfpValidateStatusSelect(MoveRepository.PFP_STATUS_AWAITING);
    }

    resultMap.put("pfpValidateStatusSelect", move.getPfpValidateStatusSelect());

    return resultMap;
  }

  public Map<String, Object> setPfpValidatorUser(Move move) {
    Objects.requireNonNull(move);
    HashMap<String, Object> resultMap = new HashMap<>();

    move.setPfpValidatorUser(
        Beans.get(InvoiceTermService.class)
            .getPfpValidatorUser(move.getPartner(), move.getCompany()));

    resultMap.put("pfpValidatorUser", move.getPfpValidatorUser());

    return resultMap;
  }
}
