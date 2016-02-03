/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.debtrecovery.ReminderService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

public class BatchReminder extends BatchStrategy {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected int mailDone = 0;
	protected int mailAnomaly = 0;
	
	protected boolean stop = false;
	protected PartnerRepository partnerRepository;
	
	@Inject
	public BatchReminder(ReminderService reminderService, PartnerRepository partnerRepository) {
		
		super(reminderService);
		this.partnerRepository = partnerRepository;
	}


	@Override
	protected void start() throws IllegalArgumentException, IllegalAccessException, AxelorException {
		
		super.start();
		
		Company company = batch.getAccountingBatch().getCompany();
				
		try {
			
			reminderService.testCompanyField(company);
			
		} catch (AxelorException e) {
			
			TraceBackService.trace(new AxelorException("", e, e.getcategory()), IException.REMINDER, batch.getId());
			incrementAnomaly();
			stop = true;
		}
		
		checkPoint();

	}
	
	
	@Override
	protected void process() {
		
		if(!stop)  {
			
			this.reminderPartner();
		
			this.generateMail();
		}
	}
	
	
	public void reminderPartner()  {
		
		int i = 0;
		List<Partner> partnerList = (List<Partner>) partnerRepository.all().filter("self.isContact = false AND ?1 MEMBER OF self.companySet", batch.getAccountingBatch().getCompany()).fetch();
		
		for (Partner partner : partnerList) {

			try {
				
				boolean remindedOk = reminderService.reminderGenerate(partnerRepository.find(partner.getId()), batch.getAccountingBatch().getCompany());
				
				if(remindedOk == true)  {  updatePartner(partner); i++; }

				log.debug("Tiers traité : {}", partner.getName());	

			} catch (AxelorException e) {
				
				TraceBackService.trace(new AxelorException(String.format(I18n.get("Tiers")+" %s", partner.getName()), e, e.getcategory()), IException.REMINDER, batch.getId());
				incrementAnomaly();
				
			} catch (Exception e) {
				
				TraceBackService.trace(new Exception(String.format(I18n.get("Tiers")+" %s", partner.getName()), e), IException.REMINDER, batch.getId());
				
				incrementAnomaly();
				
				log.error("Bug(Anomalie) généré(e) pour le tiers {}", partner.getName());
				
			} finally {
				
				if (i % 10 == 0) { JPA.clear(); }
	
			}
		}
	}
	
	
	
	public void generateMail()  {
		
//		List<Mail> mailList = (List<Mail>) mailService.all().filter("(self.pdfFilePath IS NULL or self.pdfFilePath = '') AND self.sendRealDate IS NULL AND self.mailModel.pdfModelPath IS NOT NULL").fetch();
//		
//		LOG.debug("Nombre de fichiers à générer : {}",mailList.size());
//		for(Mail mail : mailList)  {
//			try {
//				
//				mailService.generatePdfMail(mailService.find(mail.getId()));
//				mailDone++;
//				
//			} catch (AxelorException e) {
//				
//				TraceBackService.trace(new AxelorException(String.format("Courrier/Email %s", mail.getName()), e, e.getcategory()), IException.REMINDER, batch.getId());
//				mailAnomaly++;
//				
//			} catch (Exception e) {
//				
//				TraceBackService.trace(new Exception(String.format("Courrier/Mail %s", mail.getName()), e), IException.REMINDER, batch.getId());
//				
//				mailAnomaly++;
//				
//				LOG.error("Bug(Anomalie) généré(e) pour l'email/courrier {}", mail.getName());
//				
//			}
//		}
		
	}
	
	
	

	/**
	 * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the entity in the persistant context.
	 * Warning : {@code batch} entity have to be saved before.
	 */
	@Override
	protected void stop() {

		String comment = I18n.get(IExceptionMessage.BATCH_REMINDER_1);
		comment += String.format("\t* %s "+I18n.get(IExceptionMessage.BATCH_REMINDER_2)+"\n", batch.getDone());
		comment += String.format(I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());
		
//		comment += String.format("\t* %s email(s) traité(s)\n", mailDone);
//		comment += String.format("\t* %s anomalie(s)", mailAnomaly);

		super.stop();
		addComment(comment);
		
	}

}
