/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.DebtRecovery;
import com.axelor.apps.account.db.repo.DebtRecoveryRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryActionService;
import com.axelor.apps.account.service.debtrecovery.DebtRecoveryService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.repo.MessageRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import javax.persistence.Table;

public class BatchDebtRecovery extends BatchStrategy {

  protected boolean stopping = false;
  protected PartnerRepository partnerRepository;
  protected MessageRepository messageRepository;
  protected DebtRecoveryRepository debtRecoveryRepository;
  protected DebtRecoveryActionService debtRecoveryActionService;

  @Inject
  public BatchDebtRecovery(
      DebtRecoveryService debtRecoveryService,
      PartnerRepository partnerRepository,
      DebtRecoveryRepository debtRecoveryRepository,
      DebtRecoveryActionService debtRecoveryActionService,
      MessageRepository messageRepository) {
    super(debtRecoveryService);
    this.partnerRepository = partnerRepository;
    this.debtRecoveryRepository = debtRecoveryRepository;
    this.debtRecoveryActionService = debtRecoveryActionService;
    this.messageRepository = messageRepository;
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    Company company = batch.getAccountingBatch().getCompany();

    try {

      debtRecoveryService.testCompanyField(company);

    } catch (AxelorException e) {

      TraceBackService.trace(
          new AxelorException(e, e.getCategory(), ""),
          ExceptionOriginRepository.DEBT_RECOVERY,
          batch.getId());
      incrementAnomaly();
      stopping = true;
    }

    checkPoint();
  }

  @Override
  protected void process() {

    if (!stopping) {
      this.debtRecoveryPartner();
    }
  }

  public void debtRecoveryPartner() {
    Company company = batch.getAccountingBatch().getCompany();
    Set<TradingName> tradingNameSet =
        null; // Get the trading names for which to operate the debt recovery process

    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && batch.getAccountingBatch().getIsDebtRecoveryByTradingName()) {
      tradingNameSet = batch.getAccountingBatch().getTradingNameSet();
      if (tradingNameSet == null || tradingNameSet.isEmpty()) {
        tradingNameSet = company.getTradingNameSet();
      }
    }

    Query<Partner> query =
        partnerRepository
            .all()
            .filter(
                "self.isContact = false "
                    + "AND :_company MEMBER OF self.companySet "
                    + "AND self.accountingSituationList IS NOT EMPTY "
                    + "AND self.isCustomer = true "
                    + "AND self.id NOT IN ("
                    + Beans.get(BlockingService.class)
                        .listOfBlockedPartner(company, BlockingRepository.REMINDER_BLOCKING)
                    + ")")
            .bind("_company", company)
            .order("id");

    int offset = 0;
    List<Partner> partnerList;

    while (!(partnerList = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      findBatch();

      for (Partner partner : partnerList) {
        ++offset;

        boolean remindedOk;
        // if recovery handled by trading name
        if (tradingNameSet != null && !tradingNameSet.isEmpty()) {
          boolean incrementPartner = false;
          for (TradingName tradingName : tradingNameSet) {
            try {
              remindedOk = debtRecoveryService.debtRecoveryGenerate(partner, company, tradingName);
              if (remindedOk) {
                DebtRecovery debtRecovery =
                    debtRecoveryService.getDebtRecovery(partner, company, tradingName);
                addBatchToModel(debtRecovery);
                incrementPartner = true;
              }
              // Catching exceptions
            } catch (AxelorException e) {
              TraceBackService.trace(
                  new AxelorException(
                      e,
                      e.getCategory(),
                      I18n.get("Partner") + " %s, " + I18n.get("Trading name") + " %s",
                      partner.getName(),
                      tradingName.getName()),
                  ExceptionOriginRepository.DEBT_RECOVERY,
                  batch.getId());
              incrementAnomaly(partner);
              break;
            } catch (Exception e) {
              TraceBackService.trace(
                  new Exception(
                      String.format(
                          I18n.get("Partner") + " %s, " + I18n.get("Trading name") + " %s",
                          partner.getName(),
                          tradingName.getName()),
                      e),
                  ExceptionOriginRepository.DEBT_RECOVERY,
                  batch.getId());
              incrementAnomaly(partner);
              break;
            }
            // \Catching exceptions
          }
          if (incrementPartner) {
            incrementDone(partner);
          }
        } else { // if recovery handled by company
          try {
            remindedOk = debtRecoveryService.debtRecoveryGenerate(partner, company, null);
            if (remindedOk) {
              DebtRecovery debtRecovery = debtRecoveryService.getDebtRecovery(partner, company);
              addBatchToModel(debtRecovery);
              incrementDone(partner);
            }
            // Catching exceptions
          } catch (AxelorException e) {
            TraceBackService.trace(
                new AxelorException(
                    e, e.getCategory(), I18n.get("Partner") + " %s", partner.getName()),
                ExceptionOriginRepository.DEBT_RECOVERY,
                batch.getId());
            incrementAnomaly(partner);
            break;
          } catch (Exception e) {
            TraceBackService.trace(
                new Exception(String.format(I18n.get("Partner") + " %s", partner.getName()), e),
                ExceptionOriginRepository.DEBT_RECOVERY,
                batch.getId());
            incrementAnomaly(partner);
            break;
          }
          // \Catching exceptions
        }
      }

      JPA.clear();
    }
  }

  protected void incrementDone(Partner partner) {
    addBatchToModel(partner);
    _incrementDone();
  }

  protected void incrementAnomaly(Partner partner) {
    findBatch();
    partner = partnerRepository.find(partner.getId());
    // addBatchToModel(partner);
    _incrementAnomaly();
  }

  protected void addBatchToModel(Model model) {
    String tableName = getBatchSetTableName(model);

    // Insert using native query for performance reasons in case of big batch set.
    String sqlString = String.format("INSERT INTO %s VALUES (:modelId, :batchId)", tableName);
    javax.persistence.Query query = JPA.em().createNativeQuery(sqlString);
    query.setParameter("modelId", model.getId());
    query.setParameter("batchId", batch.getId());
    JPA.runInTransaction(query::executeUpdate);
  }

  protected String getBatchSetTableName(Model model) {
    String modelTableName = EntityHelper.getEntityClass(model).getAnnotation(Table.class).name();
    return modelTableName + "_BATCH_SET";
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    String comment = I18n.get(AccountExceptionMessage.BATCH_DEBT_RECOVERY_1);
    comment +=
        String.format(
            "\t* %s " + I18n.get(AccountExceptionMessage.BATCH_DEBT_RECOVERY_2) + "\n",
            batch.getDone());
    comment +=
        String.format(I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
