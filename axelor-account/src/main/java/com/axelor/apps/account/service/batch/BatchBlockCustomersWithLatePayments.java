/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchBlockCustomersWithLatePayments extends BatchStrategy {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  private AccountConfigService accountConfigService;
  private AppBaseService appBaseService;
  private DebtRecoveryRepository debtRecoveryRepository;

  @Inject
  public BatchBlockCustomersWithLatePayments(
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      DebtRecoveryRepository debtRecoveryRepository) {
    this.accountConfigService = accountConfigService;
    this.appBaseService = appBaseService;
    this.debtRecoveryRepository = debtRecoveryRepository;
  }

  @Override
  protected void process() {
    try {
      String result = blockCustomersWithLatePayments();
      if (StringUtils.isEmpty(result)) {
        result = I18n.get(AccountExceptionMessage.BATCH_BLOCK_CUSTOMER_RESULT_EMPTY);
      }
      addComment(result);
    } catch (Exception e) {
      TraceBackService.trace(e, ExceptionOriginRepository.IMPORT, batch.getId());
      incrementAnomaly();
    }
  }

  protected String blockCustomersWithLatePayments() {
    StringBuilder result = new StringBuilder();
    List<DebtRecovery> debtRecoveries;
    List<Long> customersToBlock = new ArrayList<Long>();
    List<Long> customerToUnblock = new ArrayList<Long>();
    int offset = 0;
    Query<DebtRecovery> query =
        debtRecoveryRepository.all().filter("self.archived = false or self.archived is null");
    while (!(debtRecoveries = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      for (DebtRecovery debtRecovery : debtRecoveries) {
        ++offset;
        if (debtRecovery.getRespiteDateBeforeAccountBlocking() != null
            && debtRecovery
                    .getRespiteDateBeforeAccountBlocking()
                    .compareTo(appBaseService.getTodayDate(debtRecovery.getCompany()))
                >= 0) {
          for (Invoice invoice : debtRecovery.getInvoiceDebtRecoverySet()) {
            if (!customerToUnblock.contains(invoice.getPartner().getId())) {
              log.debug("Unblocking {}", invoice.getPartner());
              result
                  .append(I18n.get("Unblocking"))
                  .append(" ")
                  .append(invoice.getPartner().getFullName())
                  .append("</br>");
              customerToUnblock.add(invoice.getPartner().getId());
              incrementDone();
            }
          }
          continue;
        }
        for (Invoice invoice : debtRecovery.getInvoiceDebtRecoverySet()) {
          try {
            Partner partner = processInvoice(invoice);
            if (partner != null && !customersToBlock.contains(partner.getId())) {
              log.debug("Blocking {}", partner.getFullName());
              result
                  .append(I18n.get("Blocking"))
                  .append(" ")
                  .append(partner.getFullName())
                  .append("</br>");
              customersToBlock.add(partner.getId());
              customersToBlock.addAll(
                  Query.of(Partner.class)
                      .filter("self.parentPartner = :parentPartner")
                      .bind("parentPartner", partner.getId())
                      .fetchStream()
                      .map(parentPartner -> parentPartner.getId())
                      .collect(Collectors.toList()));
              incrementDone();
            }
          } catch (Exception e) {
            TraceBackService.trace(
                new Exception(String.format("%s %s", "Invoice", invoice.getInvoiceId()), e),
                null,
                batch.getId());
            log.error("Error for invoice {}", invoice.getInvoiceId());
            incrementAnomaly();
          }
        }
      }
      JPA.clear();
    }
    blockCustomers(customersToBlock);
    unblockCustomers(customerToUnblock);
    return result.toString();
  }

  @Transactional
  protected void blockCustomers(List<Long> customersToBlock) {
    if (CollectionUtils.isNotEmpty(customersToBlock)) {
      Query.of(Partner.class)
          .filter("self.id in :ids")
          .bind("ids", customersToBlock)
          .update("hasBlockedAccount", true);
    }
  }

  @Transactional
  protected void unblockCustomers(List<Long> customersToUnblock) {
    if (CollectionUtils.isNotEmpty(customersToUnblock)) {
      Query.of(Partner.class)
          .filter("self.id in :ids")
          .bind("ids", customersToUnblock)
          .update("hasBlockedAccount", false);
      Query.of(Partner.class)
          .filter("self.id in :ids")
          .bind("ids", customersToUnblock)
          .update("hasManuallyBlockedAccount", false);
    }
  }

  protected Partner processInvoice(Invoice invoice) throws AxelorException {
    AccountConfig config = accountConfigService.getAccountConfig(invoice.getCompany());
    if (!config.getHasLatePaymentAccountBlocking()
        || invoice.getAmountRemaining().compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    if (invoice
            .getDueDate()
            .plusDays(config.getNumberOfDaysBeforeAccountBlocking())
            .compareTo(appBaseService.getTodayDate(invoice.getCompany()))
        <= 0) {
      return invoice.getPartner();
    }
    return null;
  }
}
