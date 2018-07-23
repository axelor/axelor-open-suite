/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.ReminderHistory;
import com.axelor.apps.account.db.repo.ReminderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.debtrecovery.ReminderActionService;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchReminder extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private List<Reminder> changedReminders = new ArrayList<>();

  protected boolean stopping = false;
  protected PartnerRepository partnerRepository;

  @Inject
  public BatchReminder(ReminderService reminderService, PartnerRepository partnerRepository) {

    super(reminderService);
    this.partnerRepository = partnerRepository;
  }

  @Override
  protected void start() throws IllegalAccessException, AxelorException {

    super.start();

    Company company = batch.getAccountingBatch().getCompany();

    try {

      reminderService.testCompanyField(company);

    } catch (AxelorException e) {

      TraceBackService.trace(
          new AxelorException("", e, e.getcategory()), IException.REMINDER, batch.getId());
      incrementAnomaly();
      stopping = true;
    }

    checkPoint();
  }

  @Override
  protected void process() {

    if (!stopping) {

      this.reminderPartner();
      this.generateMail();
    }
  }

  public void reminderPartner() {

    int i = 0;
    Company company = batch.getAccountingBatch().getCompany();
    List<Partner> partnerList =
        partnerRepository
            .all()
            .filter("self.isContact = false AND ?1 MEMBER OF self.companySet", company)
            .fetch();

    for (Partner partner : partnerList) {

      try {
        partner = partnerRepository.find(partner.getId());
        boolean remindedOk = reminderService.reminderGenerate(partner, company);

        if (remindedOk) {
          updatePartner(partner);
          changedReminders.add(reminderService.getReminder(partner, company));
          i++;
        }

        log.debug("Tiers traité : {}", partner.getName());

      } catch (AxelorException e) {

        TraceBackService.trace(
            new AxelorException(
                String.format(I18n.get("Tiers") + " %s", partner.getName()), e, e.getcategory()),
            IException.REMINDER,
            batch.getId());
        incrementAnomaly();

      } catch (Exception e) {

        TraceBackService.trace(
            new Exception(String.format(I18n.get("Tiers") + " %s", partner.getName()), e),
            IException.REMINDER,
            batch.getId());

        incrementAnomaly();

        log.error("Bug(Anomalie) généré(e) pour le tiers {}", partner.getName());

      } finally {

        if (i % 10 == 0) {
          JPA.clear();
        }
      }
    }
  }

  void generateMail() {
    for (Reminder reminder : changedReminders) {
      try {
        reminder = Beans.get(ReminderRepository.class).find(reminder.getId());
        ReminderHistory reminderHistory =
            Beans.get(ReminderActionService.class).getReminderHistory(reminder);
        if (reminderHistory == null) {
          continue;
        }
        if (reminderHistory.getReminderMessage() == null) {
          Beans.get(ReminderActionService.class).runMessage(reminder);
        }
      } catch (Exception e) {
        TraceBackService.trace(
            new Exception(
                String.format(
                    I18n.get("Tiers") + " %s",
                    reminder.getAccountingSituation().getPartner().getName()),
                e),
            IException.REMINDER,
            batch.getId());

        incrementAnomaly();
      }
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    String comment = I18n.get(IExceptionMessage.BATCH_REMINDER_1) + "\n";
    comment +=
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_REMINDER_2) + "\n", batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
