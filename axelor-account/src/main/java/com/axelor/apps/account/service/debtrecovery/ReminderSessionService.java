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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.ReminderConfigLine;
import com.axelor.apps.account.db.ReminderMethod;
import com.axelor.apps.account.db.ReminderMethodLine;
import com.axelor.apps.account.db.repo.ReminderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReminderSessionService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected ReminderRepository reminderRepo;
	
	protected LocalDate today;

	@Inject
	public ReminderSessionService(ReminderRepository reminderRepo, GeneralService generalService) {

		this.today = generalService.getTodayDate();

	}



	/**
	 * Fonction permettant de récupérer une méthode de relance en fonction de la categorie du tiers et d'une société
	 * @param reminder
	 * 			Une relance
	 * @return
	 */
	public ReminderMethod getReminderMethod(Reminder reminder)  {

		AccountingSituation accountingSituation = reminder.getAccountingSituation();
		Company company = accountingSituation.getCompany();
		Partner partner = accountingSituation.getPartner();
		List<ReminderConfigLine> reminderConfigLines = company.getAccountConfig().getReminderConfigLineList();

		for(ReminderConfigLine reminderConfigLine : reminderConfigLines)  {
			if(reminderConfigLine.getPartnerCategory().equals(partner.getPartnerCategory()))  {

				log.debug("méthode de relance determinée ");
				return reminderConfigLine.getReminderMethod();
			}
		}

		log.debug("méthode de relance non determinée ");

		return null;
	}



	/**
	 * Session de relance
	 *
	 * @param reminder
	 * 				Une relance
	 * @throws AxelorException
	 */
	public Reminder reminderSession(Reminder reminder) throws AxelorException  {

		log.debug("Begin ReminderActiveSession service...");

		LocalDate referenceDate = reminder.getReferenceDate();
		BigDecimal balanceDueReminder = reminder.getBalanceDueReminder();

		int reminderLevel=0;
		if(reminder.getReminderMethodLine()!=null && reminder.getReminderMethodLine().getReminderLevel().getName()!=null)  {
			reminderLevel = reminder.getReminderMethodLine().getReminderLevel().getName();
		}

		int theoricalReminderLevel;

		int levelMax = this.getMaxLevel(reminder);

		// Test inutile... à verifier
		if((today.isAfter(referenceDate)  ||  today.isEqual(referenceDate))  &&  balanceDueReminder.compareTo(BigDecimal.ZERO) > 0  )  {
			log.debug("Si la date actuelle est égale ou ultérieur à la date de référence et le solde due relançable positif");
			//Pour les client à haut risque vital, on passe directement du niveau de relance 2 au niveau de relance 4
			if(reminderLevel < levelMax)  {
				log.debug("Sinon ce n'est pas un client à haut risque vital");
				theoricalReminderLevel = reminderLevel+1;
			}
			else  {
				log.debug("Sinon c'est un client à un haut risque vital");
				theoricalReminderLevel = levelMax;
			}

			ReminderMethodLine reminderMethodLine = this.getReminderMethodLine(reminder, theoricalReminderLevel);


			if((!(referenceDate.plusDays(reminderMethodLine.getStandardDeadline())).isAfter(today))
					&&  balanceDueReminder.compareTo(reminderMethodLine.getMinThreshold()) > 0 )  {
				log.debug("Si le seuil du solde exigible relançable est respecté et le délai est respecté");

				if(!reminderMethodLine.getManualValidationOk())  {
					log.debug("Si le niveau ne necessite pas de validation manuelle");
					reminder.setReminderMethodLine(reminderMethodLine);		// Afin d'afficher la ligne de niveau sur le tiers
					reminder.setWaitReminderMethodLine(null);
					// et lancer les autres actions du niveau
				}
				else  {
					log.debug("Si le niveau necessite une validation manuelle");
					reminder.setWaitReminderMethodLine(reminderMethodLine);  // Si le passage est manuel
				}
			}

		}
		else  {
			log.debug("Sinon on lance une réinitialisation");
			this.reminderInitialisation(reminder);
		}
		log.debug("End ReminderActiveSession service");
		return reminder;
	}



	public int getMaxLevel(Reminder reminder)  {

		ReminderMethod reminderMethod = reminder.getReminderMethod();

		int levelMax = 0;

		if(reminderMethod!=null && reminderMethod.getReminderMethodLineList()!=null)  {
			for(ReminderMethodLine reminderMethodLine : reminderMethod.getReminderMethodLineList())  {
				int currentLevel = reminderMethodLine.getReminderLevel().getName();
				if(currentLevel > levelMax)  {
					levelMax = currentLevel;
				}
			}
		}

		return levelMax;

	}




	/**
	 * Fonction réinitialisant la relance
	 * @throws AxelorException
	 * @param relance
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void reminderInitialisation (Reminder reminder) throws AxelorException  {

		log.debug("Begin ReminderInitialisation service...");

		reminder.setReminderMethodLine(null);
		reminder.setWaitReminderMethodLine(null);
		reminder.setBalanceDue(BigDecimal.ZERO);
		reminder.setBalanceDueReminder(BigDecimal.ZERO);
		reminder.setInvoiceReminderSet(new HashSet<Invoice>());
		reminder.setPaymentScheduleLineReminderSet(new HashSet<PaymentScheduleLine>());

		log.debug("End ReminderInitialisation service");

		reminderRepo.save(reminder);
	}



	/**
	 * Fonction permetant de récupérer l'ensemble des lignes de relance de la matrice de relance pour un tiers
	 *
	 * @param reminder
	 *			Une relance
	 * @return
	 *			Une liste de ligne de matrice de relance
	 */
	public List<ReminderMethodLine>  getReminderMethodLineList(Reminder reminder)  {
		return reminder.getReminderMethod().getReminderMethodLineList();
	}


	/**
	 * Fonction permettant de récupérer une ligne de relance de la matrice de relance pour un tiers
	 *
	 * @param reminder
	 *			Une relance
	 * @param reminderLevel
	 * 			Un niveau de relance
	 * @return
	 *			Une ligne de matrice de relance
	 * @throws AxelorException
	 */
	public ReminderMethodLine getReminderMethodLine(Reminder reminder, int reminderLevel) throws AxelorException  {
		if(reminder.getReminderMethod() == null
				|| reminder.getReminderMethod().getReminderMethodLineList() == null
				|| reminder.getReminderMethod().getReminderMethodLineList().isEmpty())  {
			throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s: +"+I18n.get(IExceptionMessage.REMINDER_SESSION_1), GeneralServiceImpl.EXCEPTION,
					reminder.getAccountingSituation().getPartner().getName()), IException.MISSING_FIELD);
		}
		for(ReminderMethodLine reminderMatrixLine : reminder.getReminderMethod().getReminderMethodLineList())  {
			if(reminderMatrixLine.getReminderLevel().getName().equals(reminderLevel))  {
				return reminderMatrixLine;
			}
		}
		return null;
	}




}