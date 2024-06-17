package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdvancePaymentMoveLineCreateServiceImpl
    implements AdvancePaymentMoveLineCreateService {

  protected InvoicePaymentRepository invoicePaymentRepository;
  protected MoveLineCreateService moveLineCreateService;

  @Inject
  public AdvancePaymentMoveLineCreateServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      MoveLineCreateService moveLineCreateService) {
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.moveLineCreateService = moveLineCreateService;
  }

  @Override
  public void manageAdvancePaymentInvoiceTaxMoveLines(
      Move move, MoveLine defaultMoveLine, BigDecimal prorata, LocalDate paymentDate)
      throws AxelorException {
    if (defaultMoveLine.getMove() == null
        || invoicePaymentRepository
                .all()
                .filter("self.typeSelect = :typeSelect AND self.move = :move")
                .bind("typeSelect", InvoicePaymentRepository.TYPE_ADVANCEPAYMENT)
                .bind("move", defaultMoveLine.getMove())
                .count()
            == 0) {
      return;
    }

    List<MoveLine> taxMoveLineList =
        defaultMoveLine.getMove().getMoveLineList().stream()
            .filter(
                ml ->
                    AccountTypeRepository.TYPE_TAX.equals(
                            Optional.of(ml)
                                .map(MoveLine::getAccount)
                                .map(Account::getAccountType)
                                .map(AccountType::getTechnicalTypeSelect)
                                .orElse(""))
                        && !ObjectUtils.isEmpty(ml.getTaxLineSet())
                        && ml.getTaxLineSet().size() == 1)
            .collect(Collectors.toList());
    if (ObjectUtils.isEmpty(taxMoveLineList)) {
      return;
    }

    int counter = move.getMoveLineList().size();

    for (MoveLine moveLine : taxMoveLineList) {
      TaxLine taxLine = moveLine.getTaxLineSet().iterator().next();
      TaxConfiguration taxConfiguration =
          new TaxConfiguration(taxLine, moveLine.getAccount(), moveLine.getVatSystemSelect());
      counter++;
      MoveLine taxMoveLine =
          moveLineCreateService.createTaxMoveLine(
              move,
              moveLine.getPartner(),
              moveLine.getCredit().signum() > 0,
              paymentDate,
              counter,
              moveLine.getMove().getOrigin(),
              moveLine.getCurrencyAmount().negate().multiply(prorata),
              moveLine.getDebit().max(moveLine.getCredit()).multiply(prorata),
              taxConfiguration);
      move.addMoveLineListItem(taxMoveLine);
    }
  }
}
