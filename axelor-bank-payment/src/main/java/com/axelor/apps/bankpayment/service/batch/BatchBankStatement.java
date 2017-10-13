/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.batch;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsPartnerService;
import com.axelor.apps.bankpayment.exception.IExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class BatchBankStatement extends AbstractBatch {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private int bankStatementCount;

    @Override
    protected void process() {
        AccountingBatch accountingBatch = batch.getAccountingBatch();
        Collection<EbicsPartner> ebicsPartners = accountingBatch.getEbicsPartnerSet();

        // Retrieve all active EBICS partners if there is no configured EBICS partners
        // on the batch.
        if (ebicsPartners == null || ebicsPartners.isEmpty()) {
            ebicsPartners = getAllActiveEbicsPartners();
        }

        EbicsPartnerService ebicsPartnerService = Beans.get(EbicsPartnerService.class);

        for (EbicsPartner ebicsPartner : ebicsPartners) {
            try {
                bankStatementCount += ebicsPartnerService.getBankStatements(ebicsPartner);
                incrementDone();
            } catch (AxelorException | IOException e) {
                incrementAnomaly();
                log.error(e.getMessage());
                TraceBackService.trace(e);
            }
        }
    }

    @Override
    protected void stop() {
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_REPORT));
        sb.append(String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR,
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_DONE_SINGULAR, batch.getDone()),
                batch.getDone()));
        sb.append(String
                .format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                        com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                        batch.getAnomaly()), batch.getAnomaly()));
        sb.append("\n");
        sb.append(String.format(I18n.get(IExceptionMessage.BATCH_BANK_STATEMENT_RETRIEVED_BANK_STATEMENT_COUNT),
                bankStatementCount));
        addComment(sb.toString());
        super.stop();
    }

    private Collection<EbicsPartner> getAllActiveEbicsPartners() {
        return Beans.get(EbicsPartnerRepository.class).all()
                .filter("self.transportEbicsUser.statusSelect = :statusSelect")
                .bind("statusSelect", EbicsUserRepository.STATUS_ACTIVE_CONNECTION).fetch();
    }

}
