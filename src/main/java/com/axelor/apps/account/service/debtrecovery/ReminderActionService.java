package com.axelor.apps.account.service.debtrecovery;

import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.ReminderHistory;
import com.axelor.apps.account.db.ReminderMethodLine;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.MailService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReminderActionService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReminderActionService.class); 
	
	@Inject
	private UserInfoService uis;
	
	@Inject
	private AlarmEngineService<Partner> aes;
	
	@Inject
	private MailService ms;

	private LocalDate today;

	@Inject
	public ReminderActionService() {

		this.today = GeneralService.getTodayDate();
		
	}
	
	/**
	 * Fonction permettant d'enregistrer les mails générérés
	 * @param mapVal
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void saveAction(Map<String, Object> mapVal)  {
		
		LOG.debug("Begin saveAction service ...");
		
		for(Entry<String, Object> entry : mapVal.entrySet()) {
		    String cle = entry.getKey();
		    Object valeur = entry.getValue();
		    if(cle.equals("ReminderMailSocial1") || cle.equals("ReminderMailSocial2") || cle.equals("ReminderMailStandard1")  || cle.equals("ReminderMailStandard2"))  {
		    	Mail reminderMail = (Mail) valeur;
		    	reminderMail.save();
		    }
		    else if(cle.equals("ReminderEmailSocialDept")  || cle.equals("ReminderEmailSocialMun")) {
		    	Mail reminderEmail = (Mail) valeur;
		    	reminderEmail.save();
		    }
		}
		
		LOG.debug("End saveAction service");
		
	}
	
	
	
	
	
	/**
	 * Procédure permettant de lancer l'ensemble des actions relative au niveau de relance d'un contrat
	 * @param contractLine
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void runAction(Reminder reminder) throws AxelorException  {
		
		LOG.debug("Begin runAction service ...");
		if(reminder.getReminderMethod()==null )  {
			throw new AxelorException(String.format("%s :\nContrat %s: Méthode de relance absente.", 
					GeneralService.getExceptionReminderMsg(), reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}
		if(reminder.getReminderMethodLine()==null)  {
			throw new AxelorException(
					String.format("%s :\nContrat %s: Ligne de relance absente.", 
							GeneralService.getExceptionReminderMsg(), reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}

		else  {
		
			//On enregistre la date de la relance
			reminder.setReminderDate(today);
			
			this.saveReminder(reminder);
						
			Mail mail = ms.runMailStandard(reminder.getReminderMethodLine(), reminder.getAccountingSituation().getPartner(), reminder.getAccountingSituation().getCompany()).save();
			
			this.updateReminderHistory(reminder, mail);
							
		}
		
		LOG.debug("End runAction service");
	}
	
	
	/**
	 * Procédure permettant de lancer manuellement l'ensemble des actions relative au niveau de relance d'un contrat
	 * @param contractLine
	 * @throws AxelorException 
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void runManualAction(Reminder reminder) throws AxelorException  {
		
		LOG.debug("Begin runManualAction service ...");
		if(reminder.getReminderMethod()==null )  {
			throw new AxelorException(String.format("%s :\nTiers %s: Méthode de relance absente.", 
					GeneralService.getExceptionReminderMsg(), reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}
		
		if(reminder.getWaitReminderMethodLine()==null)  {
			throw new AxelorException(String.format("%s :\nTiers %s: Ligne de relance absente.", 
					GeneralService.getExceptionReminderMsg(), reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}
		else  {
		
			//On enregistre la date de la relance
			reminder.setReminderDate(today);
			this.reminderLevelValidate(reminder);		
			
			this.saveReminder(reminder);
						
			
			Mail mail = ms.runMailStandard(reminder.getReminderMethodLine(), reminder.getAccountingSituation().getPartner(), reminder.getAccountingSituation().getCompany()).save();
			
			this.updateReminderHistory(reminder, mail);
				
		}
		LOG.debug("End runManualAction service");
	}
	
	
	/**
	 * Porcédure permettant de déplacer une ligne de relance vers une ligne de relance en attente
	 * @param contractLine
	 * 			Un contrat
	 * @param reminderMatrixLine
	 * 			La ligne de relance que l'on souhaite déplacer
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void moveReminderMethodLine(Reminder reminder, ReminderMethodLine reminderMethodLine) throws AxelorException  {
		
		LOG.debug("Begin MoveReminderMatrixLine service ...");
		reminder.setWaitReminderMethodLine(reminderMethodLine);	
		reminder.save();
		LOG.debug("End MoveReminderMatrixLine service");
		
	}
	
	
	
	/**
	 * Fonction permettant de valider la ligne de relance en attente en la déplaçant vers la ligne de relance courante d'un contrat
	 * @param contractLine
	 * 			Un contrat
	 * @return
	 * 			Le contrat
	 * @throws AxelorException
	 */
	public Reminder reminderLevelValidate(Reminder reminder) throws AxelorException  {
		LOG.debug("Begin ReminderLevelValidate service ...");
	
		reminder.setReminderMethodLine(reminder.getWaitReminderMethodLine());
		reminder.setWaitReminderMethodLine(null);

		LOG.debug("End ReminderLevelValidate service");	
		return reminder;
	}	
	
	/**
	 * Procédure permettant d'enregistrer les éléments de la relance dans l'historique des relances
	 * @param contractLine
	 */
	@Transactional
	public void saveReminder(Reminder reminder)  {
		LOG.debug("Begin saveReminder service ...");	
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
		
		reminderHistory.setUserReminder(uis.getUserInfo());
		reminder.getReminderHistoryList().add(reminderHistory);
		reminderHistory.save();
		LOG.debug("End saveReminder service");	
	}
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateReminderHistory(Reminder reminder, Mail reminderMail)  {
		LOG.debug("Begin updateReminderHistory service ...");	
		if(!reminder.getReminderHistoryList().isEmpty())  {
			reminder.getReminderHistoryList().get(reminder.getReminderHistoryList().size()-1).setReminderMail(reminderMail);
		}
		LOG.debug("End updateReminderHistory service");	
		
	}

	
}