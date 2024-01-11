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
package com.axelor.apps.bankpayment.service.batch;

import com.axelor.apps.bankpayment.db.BankPaymentBatch;
import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBatchRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BatchEbicsCertificate extends AbstractBatch {

  protected BankPaymentBatch bankPaymentBatch;

  @Inject private TemplateRepository templateRepo;

  @Inject private TemplateMessageService templateMessageService;

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    bankPaymentBatch =
        Beans.get(BankPaymentBatchRepository.class).find(batch.getBankPaymentBatch().getId());
  }

  @Override
  protected void process() {

    Template template = templateRepo.find(bankPaymentBatch.getTemplate().getId());

    List<EbicsUser> users =
        Beans.get(EbicsUserRepository.class)
            .all()
            .filter(
                "self.a005Certificate != null OR self.e002Certificate != null OR self.x002Certificate != null")
            .fetch();

    Set<EbicsCertificate> certificatesSet = new HashSet<>();

    LocalDate today = Beans.get(AppBaseService.class).getTodayDate(bankPaymentBatch.getCompany());
    LocalDate commingDay = today.plusDays(bankPaymentBatch.getDaysNbr());

    for (EbicsUser user : users) {
      if (user.getA005Certificate() != null
          && user.getA005Certificate().getValidTo().isBefore(commingDay)) {
        certificatesSet.add(user.getA005Certificate());
      }

      if (user.getE002Certificate() != null
          && user.getE002Certificate().getValidTo().isBefore(commingDay)) {
        certificatesSet.add(user.getE002Certificate());
      }

      if (user.getX002Certificate() != null
          && user.getX002Certificate().getValidTo().isBefore(commingDay)) {
        certificatesSet.add(user.getX002Certificate());
      }
    }

    certificatesSet.addAll(
        Beans.get(EbicsCertificateRepository.class)
            .all()
            .filter("self.ebicsBank != null AND self.validTo <= ?1", commingDay)
            .fetch());

    for (EbicsCertificate certificate : certificatesSet) {

      certificate.addBatchSetItem(batchRepo.find(batch.getId()));

      try {
        templateMessageService.generateMessage(certificate, template);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public Batch ebicsCertificate(BankPaymentBatch bankPaymentBatch) {

    return Beans.get(BatchEbicsCertificate.class).run(bankPaymentBatch);
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_BANK_PAYMENT_BATCH);
  }
}
