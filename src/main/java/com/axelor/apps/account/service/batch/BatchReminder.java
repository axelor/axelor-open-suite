/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.account.service.batch;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MailService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;

public class BatchReminder extends BatchStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(BatchReminder.class);

	protected int mailDone = 0;
	protected int mailAnomaly = 0;
	
	@Inject
	public BatchReminder(ReminderService reminderService, MailService mailService) {
		
		super(reminderService, mailService);
	}


	@Override
	protected void process() {
		
		this.reminderPartner();
		
		this.generateMail();
		
	}
	
	
	public void reminderPartner()  {
		
		int i = 0;
		List<Partner> partnerList = Partner.all().filter("self.reminderClosedOk = false AND ?1 IN self.companySet", batch.getAccountingBatch().getCompany()).fetch();

		for (Partner partner : partnerList) {

			try {
				
				boolean remindedOk = reminderService.reminderGenerate(Partner.find(partner.getId()), batch.getAccountingBatch().getCompany());
				
				if(remindedOk == true)  {  updatePartner(partner); i++; }

				LOG.debug("Tiers traité : {}", partner.getName());	

			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Tiers %s", partner.getName()), e, e.getcategory()), IException.REMINDER, batch.getId());
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Tiers %s", partner.getName()), e), IException.REMINDER, batch.getId());
				
				incrementAnomaly();
				
				LOG.error("Bug(Anomalie) généré(e) pour le tiers {}", partner.getName());
				
			} finally {
				
				if (i % 10 == 0) { JPA.clear(); }
	
			}

		}
		
	}
	
	
	
	public void generateMail()  {
		
		List<Mail> mailList = Mail.all().filter("(self.pdfFilePath IS NULL or self.pdfFilePath = '') AND self.sendRealDate IS NULL AND self.mailModel.pdfModelPath IS NOT NULL").fetch();
		
		LOG.debug("Nombre de fichiers à générer : {}",mailList.size());
		for(Mail mail : mailList)  {
			try {
				
				mailService.generatePdfMail(Mail.find(mail.getId()));
				mailDone++;
				
			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format("Courrier/Email %s", mail.getName()), e, e.getcategory()), IException.REMINDER, batch.getId());
				mailAnomaly++;
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format("Courrier/Mail %s", mail.getName()), e), IException.REMINDER, batch.getId());
				
				mailAnomaly++;
				
				LOG.error("Bug(Anomalie) généré(e) pour l'email/courrier {}", mail.getName());
				
			}
		}
		
	}
	
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = "Compte rendu de relance :\n";
		comment += String.format("\t* %s contrat(s) traité(s)\n", batch.getDone());
		comment += String.format("\t* %s anomalie(s)", batch.getAnomaly());
		
		comment += String.format("\t* %s Courrier(s) et email(s) traité(s)\n", mailDone);
		comment += String.format("\t* %s anomalie(s)", mailAnomaly);

		super.stop();
		addComment(comment);
		
	}

}
