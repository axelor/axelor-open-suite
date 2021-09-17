package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MoveLineTaxServiceImpl implements MoveLineTaxService {
  protected MoveLineRepository moveLineRepository;
  protected TaxPaymentMoveLineService taxPaymentMoveLineService;
  protected AppBaseService appBaseService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveRepository moveRepository;
  protected TaxAccountService taxAccountService;
  protected MoveLineToolService moveLineToolService;
  protected FiscalPositionAccountService fiscalPositionAccountService;

  @Inject
  public MoveLineTaxServiceImpl(
      MoveLineRepository moveLineRepository,
      TaxPaymentMoveLineService taxPaymentMoveLineService,
      AppBaseService appBaseService,
      MoveLineCreateService moveLineCreateService,
      MoveRepository moveRepository,
      TaxAccountService taxAccountService,
      MoveLineToolService moveLineToolService,
      FiscalPositionAccountService fiscalPositionAccountService) {
    this.moveLineRepository = moveLineRepository;
    this.taxPaymentMoveLineService = taxPaymentMoveLineService;
    this.appBaseService = appBaseService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveRepository = moveRepository;
    this.taxAccountService = taxAccountService;
    this.moveLineToolService = moveLineToolService;
    this.fiscalPositionAccountService = fiscalPositionAccountService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine generateTaxPaymentMoveLineList(
      MoveLine customerMoveLine, Invoice invoice, Reconcile reconcile) throws AxelorException {
    BigDecimal paymentAmount = reconcile.getAmount();
    BigDecimal invoiceTotalAmount = invoice.getCompanyInTaxTotal();
    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {

      TaxLine taxLine = invoiceLineTax.getTaxLine();
      BigDecimal vatRate = taxLine.getValue();
      BigDecimal baseAmount = invoiceLineTax.getCompanyExTaxBase();
      BigDecimal detailPaymentAmount =
          baseAmount
              .multiply(paymentAmount)
              .divide(invoiceTotalAmount, 6, RoundingMode.HALF_UP)
              .setScale(2, RoundingMode.HALF_UP);

      TaxPaymentMoveLine taxPaymentMoveLine =
          new TaxPaymentMoveLine(
              customerMoveLine,
              taxLine,
              reconcile,
              vatRate,
              detailPaymentAmount,
              appBaseService.getTodayDate(reconcile.getCompany()));

      taxPaymentMoveLine = taxPaymentMoveLineService.computeTaxAmount(taxPaymentMoveLine);

      customerMoveLine.addTaxPaymentMoveLineListItem(taxPaymentMoveLine);
    }
    this.computeTaxAmount(customerMoveLine);
    return moveLineRepository.save(customerMoveLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine reverseTaxPaymentMoveLines(MoveLine customerMoveLine, Reconcile reconcile)
      throws AxelorException {
    List<TaxPaymentMoveLine> reverseTaxPaymentMoveLines = new ArrayList<TaxPaymentMoveLine>();
    for (TaxPaymentMoveLine taxPaymentMoveLine : customerMoveLine.getTaxPaymentMoveLineList()) {
      if (!taxPaymentMoveLine.getIsAlreadyReverse()
          && taxPaymentMoveLine.getReconcile().equals(reconcile)) {
        TaxPaymentMoveLine reverseTaxPaymentMoveLine =
            taxPaymentMoveLineService.getReverseTaxPaymentMoveLine(taxPaymentMoveLine);

        reverseTaxPaymentMoveLines.add(reverseTaxPaymentMoveLine);
      }
    }
    for (TaxPaymentMoveLine reverseTaxPaymentMoveLine : reverseTaxPaymentMoveLines) {
      customerMoveLine.addTaxPaymentMoveLineListItem(reverseTaxPaymentMoveLine);
    }
    this.computeTaxAmount(customerMoveLine);
    return moveLineRepository.save(customerMoveLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine computeTaxAmount(MoveLine moveLine) throws AxelorException {
    moveLine.setTaxAmount(BigDecimal.ZERO);
    if (!ObjectUtils.isEmpty(moveLine.getTaxPaymentMoveLineList())) {
      for (TaxPaymentMoveLine taxPaymentMoveLine : moveLine.getTaxPaymentMoveLineList()) {
        moveLine.setTaxAmount(moveLine.getTaxAmount().add(taxPaymentMoveLine.getTaxAmount()));
      }
    }
    return moveLine;
  }

  @Override
  @Transactional
  public void autoTaxLineGenerate(Move move) throws AxelorException {

	    List<MoveLine> moveLineList = move.getMoveLineList();

	    moveLineList.sort(
	        new Comparator<MoveLine>() {
	          @Override
	          public int compare(MoveLine o1, MoveLine o2) {
	            if (o2.getSourceTaxLine() != null) {
	              return 0;
	            }
	            return -1;
	          }
	        });

	    Iterator<MoveLine> moveLineItr = moveLineList.iterator();

	    Map<String, MoveLine> map = new HashMap<>();
	    Map<String, MoveLine> newMap = new HashMap<>();

	    while (moveLineItr.hasNext()) {

	      MoveLine moveLine = moveLineItr.next();

	      TaxLine taxLine = moveLine.getTaxLine();
	      TaxLine sourceTaxLine = moveLine.getSourceTaxLine();

	      if (sourceTaxLine != null) {

	        String sourceTaxLineKey = moveLine.getAccount().getCode() + sourceTaxLine.getId();

	        moveLine.setCredit(BigDecimal.ZERO);
	        moveLine.setDebit(BigDecimal.ZERO);
	        map.put(sourceTaxLineKey, moveLine);
	        moveLineItr.remove();
	        continue;
	      }

	      if (taxLine != null) {

	        String accountType = moveLine.getAccount().getAccountType().getTechnicalTypeSelect();

	        if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
	            || accountType.equals(AccountTypeRepository.TYPE_CHARGE)
	            || accountType.equals(AccountTypeRepository.TYPE_INCOME)
	            || accountType.equals(AccountTypeRepository.TYPE_ASSET)) {

	          BigDecimal debit = moveLine.getDebit();
	          BigDecimal credit = moveLine.getCredit();
	          BigDecimal currencyRate = moveLine.getCurrencyRate();
	          BigDecimal currencyAmount;
	          LocalDate date = moveLine.getDate();
	          Company company = move.getCompany();

	          MoveLine newOrUpdatedMoveLine = new MoveLine();

	          if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
	              || accountType.equals(AccountTypeRepository.TYPE_CHARGE)) {
	            newOrUpdatedMoveLine.setAccount(
	                taxAccountService.getAccount(taxLine.getTax(), company, true, false));
	          } else if (accountType.equals(AccountTypeRepository.TYPE_INCOME)) {
	            newOrUpdatedMoveLine.setAccount(
	                taxAccountService.getAccount(taxLine.getTax(), company, false, false));
	          } else if (accountType.equals(AccountTypeRepository.TYPE_ASSET)) {
	            newOrUpdatedMoveLine.setAccount(
	                taxAccountService.getAccount(taxLine.getTax(), company, true, true));
	          }

	          Account newAccount = newOrUpdatedMoveLine.getAccount();
	          if (newAccount == null) {
	            throw new AxelorException(
	                move,
	                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
	                I18n.get(IExceptionMessage.MOVE_LINE_6),
	                taxLine.getName(),
	                company.getName());
	          }
	          if (move.getPartner().getFiscalPosition() != null) {
	            newAccount =
	                fiscalPositionAccountService.getAccount(
	                    move.getPartner().getFiscalPosition(), newAccount);
	          }

	          String newSourceTaxLineKey = newAccount.getCode() + taxLine.getId();

	          if (!map.containsKey(newSourceTaxLineKey) && !newMap.containsKey(newSourceTaxLineKey)) {

	            newOrUpdatedMoveLine =
	                moveLineCreateService.createNewMoveLine(
	                    debit, credit, date, accountType, taxLine, newOrUpdatedMoveLine);
	          } else {

	            if (newMap.containsKey(newSourceTaxLineKey)) {
	              newOrUpdatedMoveLine = newMap.get(newSourceTaxLineKey);
	            } else if (!newMap.containsKey(newSourceTaxLineKey)
	                && map.containsKey(newSourceTaxLineKey)) {
	              newOrUpdatedMoveLine = map.get(newSourceTaxLineKey);
	            }
	            newOrUpdatedMoveLine.setDebit(
	                newOrUpdatedMoveLine.getDebit().add(debit.multiply(taxLine.getValue())));
	            newOrUpdatedMoveLine.setCredit(
	                newOrUpdatedMoveLine.getCredit().add(credit.multiply(taxLine.getValue())));
	          }
	          newOrUpdatedMoveLine.setMove(move);
	          newOrUpdatedMoveLine = moveLineToolService.setCurrencyAmount(newOrUpdatedMoveLine);
	          newOrUpdatedMoveLine.setOrigin(move.getOrigin());
	          newOrUpdatedMoveLine.setDescription(move.getDescription());
	          newOrUpdatedMoveLine.setOriginDate(move.getOriginDate());
	          newMap.put(newSourceTaxLineKey, newOrUpdatedMoveLine);
	        }
	      }
	    }

	    moveLineList.addAll(newMap.values());
	    Beans.get(MoveRepository.class).save(move);
	  }
}
