package com.axelor.apps.account.service.move;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class MoveCreateServiceImpl implements MoveCreateService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected PeriodService periodService;
  protected MoveRepository moveRepository;
  protected CompanyConfigService companyConfigService;

  protected AppAccountService appAccountService;

  @Inject
  public MoveCreateServiceImpl(
      AppAccountService appAccountService,
      PeriodService periodService,
      MoveRepository moveRepository,
      CompanyConfigService companyConfigService) {

    this.periodService = periodService;
    this.moveRepository = moveRepository;
    this.companyConfigService = companyConfigService;

    this.appAccountService = appAccountService;
  }

  /**
   * Créer une écriture comptable à la date du jour impactant la compta.
   *
   * @param journal
   * @param period
   * @param company
   * @param invoice
   * @param partner
   * @param isReject <code>true = écriture de rejet avec séquence spécifique</code>
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMove(
	      Journal journal,
	      Company company,
	      Currency currency,
	      Partner partner,
	      PaymentMode paymentMode,
	      int technicalOriginSelect,
	      int functionalOriginSelect,
	      String origin,
	      String description)
	      throws AxelorException {
	    return this.createMove(
	        journal,
	        company,
	        currency,
	        partner,
	        appAccountService.getTodayDate(company),
	        paymentMode,
	        technicalOriginSelect,
	        functionalOriginSelect,
	        origin,
	        description);
	  }

  /**
   * create an account move
   *
   * @param journal
   * @param company
   * @param currency
   * @param partner
   * @param date
   * @param paymentMode
   * @param technicalOriginSelect
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMove(
	      Journal journal,
	      Company company,
	      Currency currency,
	      Partner partner,
	      LocalDate date,
	      PaymentMode paymentMode,
	      int technicalOriginSelect,
	      int functionalOriginSelect,
	      String origin,
	      String description)
	      throws AxelorException {
	    return this.createMove(
	        journal,
	        company,
	        currency,
	        partner,
	        date,
	        paymentMode,
	        technicalOriginSelect,
	        functionalOriginSelect,
	        false,
	        false,
	        false,
	        origin,
	        description);
	  }

  /**
   * Creating a new generic accounting move
   *
   * @param journal
   * @param company
   * @param currency
   * @param partner
   * @param date
   * @param paymentMode
   * @param technicalOriginSelect
   * @param ignoreInDebtRecoveryOk
   * @param ignoreInAccountingOk
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMove(
	      Journal journal,
	      Company company,
	      Currency currency,
	      Partner partner,
	      LocalDate date,
	      PaymentMode paymentMode,
	      int technicalOriginSelect,
	      int functionalOriginSelect,
	      boolean ignoreInDebtRecoveryOk,
	      boolean ignoreInAccountingOk,
	      boolean autoYearClosureMove,
	      String origin,
	      String description)
	      throws AxelorException {
	    log.debug(
	        "Creating a new generic accounting move (journal : {}, company : {}",
	        new Object[] {journal.getName(), company.getName()});

	    Move move = new Move();

	    move.setJournal(journal);
	    move.setCompany(company);

	    move.setIgnoreInDebtRecoveryOk(ignoreInDebtRecoveryOk);
	    move.setIgnoreInAccountingOk(ignoreInAccountingOk);
	    move.setAutoYearClosureMove(autoYearClosureMove);

	    if (autoYearClosureMove) {
	      move.setPeriod(periodService.getPeriod(date, company, YearRepository.TYPE_FISCAL));
	      if (move.getPeriod() == null) {
	        throw new AxelorException(
	            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
	            I18n.get(IExceptionMessage.PERIOD_1),
	            company.getName(),
	            date.toString());
	      }
	    } else {
	      move.setPeriod(periodService.getActivePeriod(date, company, YearRepository.TYPE_FISCAL));
	    }

	    move.setDate(date);
	    move.setMoveLineList(new ArrayList<MoveLine>());

	    Currency companyCurrency = companyConfigService.getCompanyCurrency(company);

	    if (companyCurrency != null) {
	      move.setCompanyCurrency(companyCurrency);
	      move.setCompanyCurrencyCode(companyCurrency.getCode());
	    }

	    if (currency == null) {
	      currency = move.getCompanyCurrency();
	    }
	    if (currency != null) {
	      move.setCurrency(currency);
	      move.setCurrencyCode(currency.getCode());
	    }
	    move.setOrigin(origin);
	    move.setDescription(description);
	    move.setPartner(partner);
	    move.setPaymentMode(paymentMode);
	    move.setTechnicalOriginSelect(technicalOriginSelect);
	    move.setFunctionalOriginSelect(functionalOriginSelect);
	    moveRepository.save(move);
	    move.setReference(Beans.get(SequenceService.class).getDraftSequenceNumber(move));

	    return move;
	  }

  /**
   * Créer une écriture comptable de toute pièce spécifique à une saisie paiement.
   *
   * @param journal
   * @param period
   * @param company
   * @param invoice
   * @param partner
   * @param isReject <code>true = écriture de rejet avec séquence spécifique</code>
   * @param agency L'agence dans laquelle s'effectue le paiement
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMoveWithPaymentVoucher(
	      Journal journal,
	      Company company,
	      PaymentVoucher paymentVoucher,
	      Partner partner,
	      LocalDate date,
	      PaymentMode paymentMode,
	      int technicalOriginSelect,
	      int functionalOriginSelect,
	      String origin,
	      String description)
	      throws AxelorException {
	    Move move =
	        this.createMove(
	            journal,
	            company,
	            paymentVoucher.getCurrency(),
	            partner,
	            date,
	            paymentMode,
	            technicalOriginSelect,
	            functionalOriginSelect,
	            origin,
	            description);
	    move.setPaymentVoucher(paymentVoucher);
	    return move;
	  }
}
