/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.CashRegisterLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.ReminderHistory;
import com.axelor.apps.account.db.ReminderMethodLine;
import com.axelor.apps.account.service.administration.GeneralServiceAccount;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Commune;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Department;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.MailModel;
import com.axelor.apps.base.db.MailModelTag;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Template;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.ObjectTool;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MailService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailService.class); 
	
	private LocalDate today;
	
	/**
	 * Le numéro de mail généré
	 */
	private int number = 0;

	@Inject
	public MailService() {

		this.today = GeneralService.getTodayDate();
		
	}
	
	
	/**
	 * Procédure permettant de créer un email spécifique au remboursement
	 * @param contact
	 * 			Un contact
	 * @param company
	 * 			Une société
	 * @throws AxelorException 
	 */
	public Mail createReimbursementMail(Partner partner, Company company) throws AxelorException  {
		
		LOG.debug("In createReimbursementMail");
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		if(accountConfig == null || accountConfig.getReimbursementMailModel() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un model d'email Remboursement pour la société %s", 
					GeneralServiceAccount.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		return this.replaceTag(
				this.createGenericMail(accountConfig.getReimbursementMailModel(), this.today, company, partner));
		
	}
	
	
	/**
	 * Procédure permettant de créer un courrier spécifique aux imports des rejets
	 * @param contact
	 * 			Un contact
	 * @param company
	 * 			Une société
	 * @throws AxelorException 
	 */
	public Mail createImportRejectMail(Partner partner, Company company, MailModel mailModel, MoveLine rejectMoveLine) throws AxelorException  {
		LOG.debug("In createImportRejectMail");
		
		Mail mail = this.createGenericMail(mailModel, this.today, company, partner);
		
		mail.setRejectMoveLine(rejectMoveLine);
		
		return this.replaceTag(mail);
		
	}
	
	
	/**
	 * Procédure permettant de créer un email spécifique aux caisses
	 * @param contact
	 * 			Un contact
	 * @param company
	 * 			Une société
	 * @throws AxelorException 
	 */
	public Mail createCashRegisterLineMail(String address, Company company, CashRegisterLine cashRegisterLine) throws AxelorException  {
		LOG.debug("In createCashRegisterMail");
		
		AccountConfig accountConfig = company.getAccountConfig();
		
		if(accountConfig == null || accountConfig.getCashRegisterMailModel() == null)  {
			throw new AxelorException(String.format("%s :\n Veuillez configurer un model d'email Caisses pour la société %s", 
					GeneralServiceAccount.getExceptionAccountingMsg(),company.getName()), IException.CONFIGURATION_ERROR);
		}
		Mail mail = createGenericMail(accountConfig.getCashRegisterMailModel(), this.today, company, null);
		mail.setCashRegisterLine(cashRegisterLine);
		mail.setAddressEmail(address);
		
		return this.replaceTag(mail);

	}
	
	
	/**
	 * Méthode permettant de créer un email générique d'après un model d'email
	 * @param mailModel
	 * 			Un modèle d'email
	 * @param contact
	 * 			Un destinataire
	 * @param company
	 * 			Une société
	 * @param date
	 * 			Une date d'envoie prévue
	 * @param addressEmail
	 * 			Une addresse email dans le cas ou l'email n'est pas envoyé à un contact
	 * @param address
	 * 			Une adresse postale
	 * @return
	 */
	public Mail createGenericMail(MailModel mailModel, Partner partner, LocalDate date, String addressEmail, Company company)  {
		LOG.debug("In createGenericMail - email with adresse email");
		Mail mail = createGenericMail(mailModel, date, company, partner);
		mail.setAddressEmail(addressEmail);
		return mail;
	}
	
	/**
	 * Méthode permettant de créer un email générique d'après un model d'email
	 * @param mailModel
	 * 			Un modèle d'email
	 * @param contact
	 * 			Un destinataire
	 * @param company
	 * 			Une société
	 * @param date
	 * 			Une date d'envoie prévue
	 * @param addressEmail
	 * 			Une addresse email dans le cas ou l'email n'est pas envoyé à un contact
	 * @param address
	 * 			Une adresse postale
	 * @return
	 */
	public Mail createGenericMail(MailModel mailModel, LocalDate date, Company company, Partner partner)  {
		
		LOG.debug("In createGenericMail - email");
		Mail mail = new Mail();
		mail.setPartner(partner);
		mail.setSendScheduleDate(date);
		mail.setObjectConcernedSelect(mailModel.getObjectConcernedSelect());
		mail.setTypeSelect(mailModel.getTypeSelect());
		mail.setCode(mailModel.getCode());
		mail.setName(mailModel.getName());
		mail.setSubject(mailModel.getSubject());
		mail.setContent(mailModel.getContent());
		mail.setCompany(company);
		
		mail.setMailModel(mailModel);
		return mail;
	}
	
	
	/**
	 * Méthode permettant de créer courrier générique d'après un model de courrier
	 * @param mailModel
	 * 			Un modèle de courrier
	 * @param contact
	 * 			Un destinataire
	 * @param company
	 * 			Une société
	 * @param date
	 * 			Une date d'envoie prévue
	 * @param adresse
	 * 			L'adresse du destinataire
	 * @return
	 * @throws AxelorException 
	 */
	public Mail createGenericMail(MailModel mailModel, Partner partner, LocalDate date, Address adresse, Company company) throws AxelorException  {
		
		LOG.debug("In createGenericMail - courrier");
		
		if(adresse == null)  {
			throw new AxelorException(String.format("%s :\n Erreur : Pas d'adresse de définie pour le contact %s", 
					GeneralServiceAccount.getExceptionAccountingMsg(), partner.getName()), IException.MISSING_FIELD);
		}
		
		Mail mail = createGenericMail(mailModel, date, company, partner);
		
		return mail;
	}
	
	
	
	/**
	 * Méthode permettant de récupérer la commune d'un contrat si elle est paramétrée
	 * @param contractLine
	 * 			Un contrat
	 * @return
	 * 			Une commune
	 * @throws AxelorException
	 */
	public Commune getCommune(Partner partner) throws AxelorException  {
		
		
		if(partner.getCommune() == null)  {
			throw new AxelorException(String.format("%s :\n Tiers %s : Aucune commune selectionnée pour le PCT %s", 
					GeneralServiceAccount.getExceptionReminderMsg(), partner.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return  partner.getCommune();
		
	}
	
	
	/**
	 * Méthode permettant de récupérer le département d'un contrat si il est paramétré
	 * @param contractLine
	 * 			Un contrat
	 * @return
	 * 			Un département
	 * @throws AxelorException
	 */
	public Department getDepartment(Partner partner) throws AxelorException  {
		
		Commune commune = this.getCommune(partner);
		
		if(commune.getDepartment() == null)  {
			throw new AxelorException(String.format("%s :\n Tiers %s : Aucun département selectionné pour la commune %s", 
					GeneralServiceAccount.getExceptionReminderMsg(), partner.getName(), commune.getName()), IException.CONFIGURATION_ERROR);
		}
		
		return  commune.getDepartment();
		
	}
	
	
	/**
	 * Fonction permettant de créer un courrier à destination des tiers pour un contrat standard
	 * @param contractLine
	 * 			Un contrat
	 * @param reminderMatrixLine
	 * 			Une ligne de relance
	 * @param partnerConcerned
	 * 			Le tiers concerné
	 * @return
	 * 			Un email
	 * @throws AxelorException
	 */
	public Mail runMailStandard(ReminderMethodLine reminderMethodLine, Partner partner, Company company) throws AxelorException  {
		LOG.debug("Begin runMailStandard service ...");	
		if(reminderMethodLine.getMessageTemplate() != null )  {
			Template messageTemplate = reminderMethodLine.getMessageTemplate();
//			Mail reminderMail = this.createGenericMail(messageTemplate, null, today.plusDays(reminderMethodLine.getStandardDeadline()), partner.getMainInvoicingAddress(), company);
//			
//			reminderMail.setReminderHistory(this.getReminderHistory(partner, company));
//			
//			return this.replaceTag(reminderMail);
			
			return null;  //TODO
		}
		else {
			throw new AxelorException(String.format("%s :\nContrat %s: Modèle de courrier absent pour la matrice de relance %s (Niveau %s).", 
					GeneralServiceAccount.getExceptionReminderMsg(), partner.getName(), reminderMethodLine.getReminderMethod().getName(), reminderMethodLine.getReminderLevel().getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public List<ReminderHistory> getReminderHistoryList(Partner partner, Company company)  {
		
		
		AccountingSituation accountingSituation = AccountingSituation.filter("self.partner = ?1 and self.company = ?2", partner, company).fetchOne();
		if(accountingSituation != null && accountingSituation.getReminder() != null)  {
			return accountingSituation.getReminder().getReminderHistoryList();
		}
		
		return new LinkedList<ReminderHistory>();
	}
	
	
	public ReminderHistory getReminderHistory(Partner partner, Company company)  {
		
		LinkedList<ReminderHistory>  reminderHistoryList = new LinkedList<ReminderHistory>();
		reminderHistoryList.addAll(this.getReminderHistoryList(partner, company));
		
		if(!reminderHistoryList.isEmpty())  {
			return reminderHistoryList.getLast();
		}
		return null;
	}
	
	
	/**
	 * Procédure permettant de remplacer les tags défini dans un mail par des valeurs
	 * @param mail
	 * 			Un mail
	 */
	public Mail replaceTag(Mail mail)  {
		
		List<MailModelTag> mailModelTagList = MailModelTag.all().fetch();

		String content = mail.getContent();
		String subject = mail.getSubject();
		
		for(MailModelTag mailModelTag : mailModelTagList)  {
			
			LOG.debug("Tag à remplacer - {}", mailModelTag.getTag());
			
			String link = mailModelTag.getLink();
			LOG.debug("Lien : {}", link);
			
			String[] links = link.split("\\.");
			LOG.debug("Nombre de niveau : {}", links.length);
			
			@SuppressWarnings("rawtypes")
			Class classGotten = mail.getClass();
			Field f = null;
			
			Object obj = mail;
			
			for(String fieldName : links)  {
				
				LOG.debug("Niveau traité : {}", fieldName);	
				
				// Récupération du type d'objet enfant
				f = ObjectTool.getField(fieldName, classGotten);
				classGotten = f.getType();
				
				// Récupération de l'objet enfant
				obj = ObjectTool.getObject(obj, fieldName);
				if(obj == null)  {
					obj = new String("");
					break;
				}
						
			}
			
			content = content.replaceAll(mailModelTag.getTag(), obj.toString());
			subject = subject.replaceAll(mailModelTag.getTag(), obj.toString());
		}
		
		mail.setContent(content);
		mail.setSubject(subject);
		
		return mail;
	}
	
	
	
	
	/**
	 * Procédure permettant de générer les fichiers pdf des emails et courriers en masse
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void generateAllPdfMail()  {
		LOG.debug("Génération en masse des emails et courriers au format Pdf");
		
		List<Mail> mailList = Mail.filter("(self.pdfFilePath IS NULL or self.pdfFilePath = '') AND self.sendRealDate IS NULL AND self.mailModel.pdfModelPath IS NOT NULL").fetch();
		
		LOG.debug("Nombre de fichiers à générer : {}",mailList.size());
		for(Mail mail : mailList)  {
			try {
				
				this.generatePdfMail(mail);
				
			} catch (AxelorException e) {
				TraceBackService.trace(e);
				
				LOG.error("Bug(Anomalie) généré(e) pour l'email/courrier {}", mail.getName());
	
			}
		}
	}
	
	
	public void generatePdfMail(Mail mail) throws AxelorException {

		MailModel mailModel = mail.getMailModel();
		
		if(mailModel == null )  {
			throw new AxelorException(String.format("%s : Aucun modèle de Courrier/Email de défini pour le Courrier/Email %s", 
					GeneralService.getExceptionMailMsg(), mail.getName()), IException.CONFIGURATION_ERROR);
		}
		
		String pdfName = mailModel.getPdfModelPath();
		
		if(pdfName == null || pdfName.isEmpty())  {
//			throw new AxelorException(String.format("%s : Aucun modèle d'impression Birt de défini dans le modèle de Courrier/Email pour le Courrier/Email %s", 
//					GeneralService.getExceptionMailMsg(), mail.getName()), IException.CONFIGURATION_ERROR);
		}
		else  {
			
			String url = new ReportSettings(pdfName)
							.addParam("__locale", "fr_FR")
							.addParam("MailId", mail.getId().toString())
							.getUrl();
			
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist != null){
				throw new AxelorException(String.format("%s : %s pour le Courrier/Email %s", 
						GeneralService.getExceptionMailMsg(), urlNotExist, mail.getName()), IException.CONFIGURATION_ERROR);
			}
			LOG.debug("Impression du mail ${mail.code} : ${url.toString()}");
			
			String filePath = this.getFilePath(mail);
			String fileName = this.getFileNameGenerated(mail);
			
			try {
				URLService.fileDownload(url, filePath, fileName);
			} catch (IOException e) {
				throw new AxelorException(String.format("%s : %s pour le Courrier/Email %s", 
						GeneralService.getExceptionMailMsg(), e, mail.getName()), IException.CONFIGURATION_ERROR);
			}
			
			mail.setPdfFilePath(filePath+fileName);
		}
	}
	
	
	public String getFileNameGenerated(Mail mail) throws AxelorException   {
		String prefix = this.getFileName(mail);
		DateFormat yyyyMMddHHmmssFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String dateFileName = yyyyMMddHHmmssFormat.format(GeneralService.getTodayDateTime().toDate());
		return String.format("%s%s%s.pdf", prefix, dateFileName, this.getNumber());
	}	
	
	
	/**
	 * Méthode permettant de retourner le numero de mail généré et de l'incrémenter
	 * @return
	 */
	public int getNumber()  {
		int number = this.number;
		this.number += 1;
		return number;
	}
		
	
	public String getFileName(Mail mail) throws AxelorException   {
		Company company = mail.getCompany();
		if(mail.getTypeSelect() == 0)  {
			return this.getEmailFileName(company);
		}
		else if(mail.getTypeSelect() == 1)  {
			return this.getMailFileName(company);
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de type de défini pour Courrier/Email %s", 
					GeneralService.getExceptionMailMsg(), mail.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public String getMailFileName(Company company) throws AxelorException   {
		if(company.getGeneratedMailFileName() != null && !company.getGeneratedMailFileName().isEmpty())  {
			return company.getGeneratedMailFileName();
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de chemin de défini pour les Courriers générés pour la société %s", 
					GeneralService.getExceptionMailMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public String getEmailFileName(Company company) throws AxelorException   {
		if(company.getGeneratedEmailFileName() != null && !company.getGeneratedEmailFileName().isEmpty())  {
			return company.getGeneratedEmailFileName();
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de chemin de défini pour les Emails générés pour la société %s", 
					GeneralService.getExceptionMailMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public String getFilePath(Mail mail) throws AxelorException  {
		Company company = mail.getCompany();
		if(mail.getTypeSelect() == 0)  {
			return this.getEmailFilePath(company);
		}
		else if(mail.getTypeSelect() == 1)  {
			return this.getMailFilePath(company);
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de type de défini pour Courrier/Email %s", 
					GeneralService.getExceptionMailMsg(), mail.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public String getMailFilePath(Company company) throws AxelorException  {
		if(company.getGeneratedMailFilePath() != null && !company.getGeneratedMailFilePath().isEmpty())  {
			return company.getGeneratedMailFilePath();
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de chemin de défini pour les Courriers générés pour la société %s", 
					GeneralService.getExceptionMailMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public String getEmailFilePath(Company company) throws AxelorException  {
		if(company.getGeneratedEmailFilePath() != null && !company.getGeneratedEmailFilePath().isEmpty())  {
			return company.getGeneratedEmailFilePath();
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de chemin de défini pour les Emais générés pour la société %s", 
					GeneralService.getExceptionMailMsg(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	public Company getCompany(Mail mail) throws AxelorException  {
		if(mail.getCompany() != null)  {
			return mail.getCompany();
		}
		else  {
			throw new AxelorException(String.format("%s : Pas de société de défini pour le Courrier/Email %s", 
					GeneralService.getExceptionMailMsg(), mail.getName()), IException.CONFIGURATION_ERROR);
		}
	}
	
	
	/**
	 * Methode permettant de récupérer l'ensemble des emails/courriers associés à un contrat
	 * @param contractLine
	 * 			Un contrat
	 * @return
	 */
	public List<Mail> getMailList(Partner partner)  {
		return Mail.filter("self.partner = ?1", partner).fetch();
	}
	
}
