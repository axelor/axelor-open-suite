/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JournalService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /** Debit balance = debit - credit */
  public static final Integer BALANCE_TYPE_DEBIT_BALANCE = 1;

  /** Credit balance = credit - debit */
  public static final Integer BALANCE_TYPE_CREDIT_BALANCE = 1;

  private AccountConfigService accountConfigService;

  public JournalService() {

    this.accountConfigService = new AccountConfigService();
  }

  /**
   * @param invoice
   *     <p>OperationTypeSelect 1 : Achat fournisseur 2 : Avoir fournisseur 3 : Vente client 4 :
   *     Avoir client
   * @return
   * @throws AxelorException
   */
  public Journal getJournal(Invoice invoice) throws AxelorException {

    Company company = invoice.getCompany();

    if (company != null) {

      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      switch (invoice.getOperationTypeSelect()) {
        case 1:
          // Si le montant est négatif, alors c'est un avoir
          if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1) {
            return accountConfigService.getSupplierCreditNoteJournal(accountConfig);
          } else {
            return accountConfigService.getSupplierPurchaseJournal(accountConfig);
          }

        case 2:
          // Si le montant est négatif, alors c'est une facture
          if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1) {
            return accountConfigService.getSupplierPurchaseJournal(accountConfig);
          } else {
            return accountConfigService.getSupplierCreditNoteJournal(accountConfig);
          }
        case 3:
          // Si le montant est négatif, alors c'est un avoir
          if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1) {
            return accountConfigService.getCustomerCreditNoteJournal(accountConfig);
          } else {
            return accountConfigService.getCustomerSalesJournal(accountConfig);
          }
        case 4:
          // Si le montant est négatif, alors c'est une facture
          if (invoice.getInTaxTotal().compareTo(BigDecimal.ZERO) == -1) {
            return accountConfigService.getCustomerSalesJournal(accountConfig);
          } else {
            return accountConfigService.getCustomerCreditNoteJournal(accountConfig);
          }

        default:
          throw new AxelorException(
              invoice,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.JOURNAL_1),
              invoice.getInvoiceId());
      }
    }

    return null;
  }

  /**
   * Compute the balance of the journal, depending of the account type and balance type
   *
   * @param journal Journal
   * @param accountType Technical type select of AccountType
   * @param balanceType
   *     <p>1 : debit balance = debit - credit
   *     <p>2 : credit balance = credit - debit
   * @return The balance (debit balance or credit balance)
   */
  public BigDecimal computeBalance(Journal journal, int accountType, int balanceType) {

    Query balanceQuery =
        JPA.em()
            .createQuery(
                "select sum(self.debit - self.credit) from MoveLine self where self.move.journal = :journal "
                    + "and self.move.ignoreInAccountingOk IN ('false', null) and self.move.statusSelect IN (2, 3) and self.account.accountType.technicalTypeSelect = :accountType");

    balanceQuery.setParameter("journal", journal);
    balanceQuery.setParameter("accountType", accountType);

    BigDecimal balance = (BigDecimal) balanceQuery.getSingleResult();

    if (balance != null) {
      if (balanceType == BALANCE_TYPE_CREDIT_BALANCE) {
        balance = balance.negate();
      }
      log.debug("Account balance : {}", balance);

      return balance;
    } else {
      return BigDecimal.ZERO;
    }
  }
}
