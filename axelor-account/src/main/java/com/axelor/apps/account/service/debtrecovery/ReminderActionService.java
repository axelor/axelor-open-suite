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
package com.axelor.apps.account.service.debtrecovery;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.ReminderHistory;
import com.axelor.apps.account.db.ReminderMethodLine;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.ReminderHistoryRepository;
import com.axelor.apps.account.db.repo.ReminderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReminderActionService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected UserService userService;
	protected ReminderRepository reminderRepo;
	protected ReminderHistoryRepository reminderHistoryRepository;
	protected TemplateMessageService templateMessageService;

	protected LocalDate today;

	@Inject
	public ReminderActionService(UserService userService, ReminderRepository reminderRepo, ReminderHistoryRepository reminderHistoryRepository, 
			TemplateMessageService templateMessageService, GeneralService generalService) {

		this.userService = userService;
		this.reminderRepo = reminderRepo;
		this.reminderHistoryRepository = reminderHistoryRepository;
		this.templateMessageService = templateMessageService;
		
		this.today = generalService.getTodayDate();

	}



	/**
	 * Procédure permettant de lancer l'ensemble des actions relative au niveau de relance d'un tiers
	 * @param reminder
	 * 			Une relance
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void runAction(Reminder reminder) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		if(reminder.getReminderMethod()==null )  {
			throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s: "+I18n.get(IExceptionMessage.REMINDER_ACTION_1),
					GeneralServiceImpl.EXCEPTION, reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}
		if(reminder.getReminderMethodLine()==null)  {
			throw new AxelorException(
					String.format("%s :\n"+I18n.get("Tiers")+" %s: "+I18n.get(IExceptionMessage.REMINDER_ACTION_2),
							GeneralServiceImpl.EXCEPTION, reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}

		else  {

			//On enregistre la date de la relance
			reminder.setReminderDate(today);

			this.saveReminder(reminder);

			Message message = this.runStandardMessage(reminder);

			Beans.get(MessageRepository.class).save(message);

			this.updateReminderHistory(reminder, message);

		}

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
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public Message runStandardMessage(Reminder reminder) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		ReminderMethodLine reminderMethodLine = reminder.getReminderMethodLine();
		Partner partner =  reminder.getAccountingSituation().getPartner();
		Company company = reminder.getAccountingSituation().getCompany();

		Template template = reminderMethodLine.getMessageTemplate();

		if(template == null )  {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.REMINDER_ACTION_3),
					GeneralServiceImpl.EXCEPTION, partner.getName(), reminderMethodLine.getReminderMethod().getName(), reminderMethodLine.getReminderLevel().getName()), IException.CONFIGURATION_ERROR);
		}

		ReminderHistory reminderHistory = this.getReminderHistory(partner, company);

		reminderHistory.setReminderMessage(templateMessageService.generateMessage(reminderHistory, template));

		return reminderHistory.getReminderMessage();

	}


	public List<ReminderHistory> getReminderHistoryList(Partner partner, Company company)  {
		
		AccountingSituationRepository accSituationRepo = Beans.get(AccountingSituationRepository.class);
		
		AccountingSituation accountingSituation = accSituationRepo.all().filter("self.partner = ?1 and self.company = ?2", partner, company).fetchOne();
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
	 * Procédure permettant de lancer manuellement l'ensemble des actions relative au niveau de relance d'un tiers
	 * @param reminder
	 * 			Une relance
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void runManualAction(Reminder reminder) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException  {

		log.debug("Begin runManualAction service ...");
		if(reminder.getReminderMethod()==null )  {
			throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s: "+I18n.get(IExceptionMessage.REMINDER_ACTION_1),
					GeneralServiceImpl.EXCEPTION, reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}

		if(reminder.getWaitReminderMethodLine()==null)  {
			throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s: "+I18n.get(IExceptionMessage.REMINDER_ACTION_2),
					GeneralServiceImpl.EXCEPTION, reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}
		else  {

			//On enregistre la date de la relance
			reminder.setReminderDate(today);
			this.reminderLevelValidate(reminder);

			this.saveReminder(reminder);

			Message message = this.runStandardMessage(reminder);

			this.updateReminderHistory(reminder, message);

		}
		log.debug("End runManualAction service");
	}


	/**
	 * Porcédure permettant de déplacer une ligne de relance vers une ligne de relance en attente
	 * @param partner
	 * 			Une relance
	 * @param reminderMatrixLine
	 * 			La ligne de relance que l'on souhaite déplacer
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void moveReminderMethodLine(Reminder reminder, ReminderMethodLine reminderMethodLine) throws AxelorException  {

		reminder.setWaitReminderMethodLine(reminderMethodLine);

		reminderRepo.save(reminder);

	}



	/**
	 * Fonction permettant de valider la ligne de relance en attente en la déplaçant vers la ligne de relance courante d'un tiers
	 * @param reminder
	 * 			Une relance
	 * @return
	 * 			La relance
	 * @throws AxelorException
	 */
	public Reminder reminderLevelValidate(Reminder reminder) throws AxelorException  {
		log.debug("Begin ReminderLevelValidate service ...");

		reminder.setReminderMethodLine(reminder.getWaitReminderMethodLine());
		reminder.setWaitReminderMethodLine(null);

		log.debug("End ReminderLevelValidate service");
		return reminder;
	}

	/**
	 * Procédure permettant d'enregistrer les éléments de la relance dans l'historique des relances
	 * @param reminder
	 * 			Une relance
	 */
	@Transactional
	public void saveReminder(Reminder reminder)  {

		ReminderHistory reminderHistory = new ReminderHistory();
		reminderHistory.setReminder(reminder);
		reminderHistory.setBalanceDue(reminder.getBalanceDue());
		reminderHistory.setBalanceDueReminder(reminder.getBalanceDueReminder());
		reminderHistory.setReminderDate(reminder.getReminderDate());
		reminderHistory.setReminderMethodLine(reminder.getReminderMethodLine());
		reminderHistory.setSetToIrrecoverableOK(reminder.getSetToIrrecoverableOk());
		reminderHistory.setUnknownAddressOK(reminder.getUnknownAddressOk());
		reminderHistory.setReferenceDate(reminder.getReferenceDate());
		reminderHistory.setReminderMethod(reminder.getReminderMethod());

		reminderHistory.setUserReminder(userService.getUser());
		reminder.getReminderHistoryList().add(reminderHistory);
		reminderHistoryRepository.save(reminderHistory);

	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateReminderHistory(Reminder reminder, Message reminderMessage)  {

		if(!reminder.getReminderHistoryList().isEmpty())  {
			reminder.getReminderHistoryList().get(reminder.getReminderHistoryList().size()-1).setReminderMessage(reminderMessage);
		}

	}


}