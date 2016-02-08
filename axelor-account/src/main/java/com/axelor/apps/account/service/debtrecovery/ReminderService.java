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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reminder;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.ReminderRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.administration.GeneralServiceImpl;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ReminderService {

	private final Logger log = LoggerFactory.getLogger( getClass() );

	protected ReminderSessionService reminderSessionService;
	protected ReminderActionService reminderActionService;
	protected AccountCustomerService accountCustomerService;
	protected MoveLineRepository moveLineRepo;
	protected PaymentScheduleLineRepository paymentScheduleLineRepo;
	protected AccountConfigService accountConfigService;
	protected ReminderRepository reminderRepo;

	protected LocalDate today;

	@Inject
	public ReminderService(ReminderSessionService reminderSessionService, ReminderActionService reminderActionService, AccountCustomerService accountCustomerService,
			MoveLineRepository moveLineRepo, PaymentScheduleLineRepository paymentScheduleLineRepo, AccountConfigService accountConfigService, ReminderRepository reminderRepo,
			GeneralService generalService) {

		this.reminderSessionService = reminderSessionService;
		this.reminderActionService = reminderActionService;
		this.accountCustomerService = accountCustomerService;
		this.moveLineRepo = moveLineRepo;
		this.paymentScheduleLineRepo = paymentScheduleLineRepo;
		this.accountConfigService = accountConfigService;
		this.reminderRepo = reminderRepo;
		this.today = generalService.getTodayDate();

	}


	public void testCompanyField(Company company) throws AxelorException  {

		AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

		accountConfigService.getReminderConfigLineList(accountConfig);

	}


	/**
	 * Fonction permettant de calculer le solde exigible relançable d'un tiers
	 * @param reminder
	 * 			Une relance
	 * @return
	 * 			Le solde exigible relançable
	 */
	public BigDecimal getBalanceDueReminder(List<MoveLine> moveLineList, Partner partner)  {
		BigDecimal balanceSubstract = this.getSubstractBalanceDue(partner);
		BigDecimal balanceDueReminder = BigDecimal.ZERO;
		for(MoveLine moveLine : moveLineList)  {
			balanceDueReminder = balanceDueReminder.add(moveLine.getAmountRemaining());
		}
		balanceDueReminder = balanceDueReminder.add(balanceSubstract);
		return balanceDueReminder;
	}


	public BigDecimal getSubstractBalanceDue( Partner partner)  {
		List<? extends MoveLine> moveLineQuery = moveLineRepo.all().filter("self.partner = ?1", partner).fetch();
		BigDecimal balance = BigDecimal.ZERO;
		for(MoveLine moveLine : moveLineQuery)  {
			if(moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)  {
				if(moveLine.getAccount()!=null && moveLine.getAccount().getReconcileOk())  {
					balance = balance.subtract(moveLine.getAmountRemaining());
				}
			}
		}
		return balance;
	}


	/**
	 * Fonction qui récupère la plus ancienne date d'échéance d'une liste de lignes d'écriture
	 * @param moveLineList
	 * 			Une liste de lignes d'écriture
	 * @return
	 * 			la plus ancienne date d'échéance
	 */
	public LocalDate getOldDateMoveLine(List<MoveLine> moveLineList)  {
		LocalDate minMoveLineDate = new LocalDate();


		if(moveLineList != null && !moveLineList.isEmpty())  {
			for(MoveLine moveLine : moveLineList)  {
				if(minMoveLineDate.isAfter(moveLine.getDueDate()))  {	minMoveLineDate=moveLine.getDueDate();	}
			}
		}
		else  {	minMoveLineDate=null;	}
		return minMoveLineDate;
	}


	/**
	 * Fonction qui récupére la plus récente date entre deux date
	 * @param date1
	 * 			Une date
	 * @param date2
	 * 			Une date
	 * @return minDate
	 * 			La plus ancienne date
	 */
	public LocalDate getLastDate(LocalDate date1, LocalDate date2)  {
		LocalDate minDate = new LocalDate();
		if(date1!=null && date2!=null)  {
			if(date1.isAfter(date2))  {	minDate=date1;	}
			else  {  minDate=date2;  }
		}
		else if(date1!=null) {	minDate=date1;	}
		else if(date2!=null)  {	minDate=date2;	}
		else  {	minDate=null;	}
		return minDate;
	}


	/**
	 * Fonction qui permet de récupérer la date de relance la plus récente
	 * @param reminder
	 * 			Une relance
	 * @return
	 * 			La date de relance la plus récente
	 */
	public LocalDate getLastDateReminder(Reminder reminder)  {
		return reminder.getReminderDate();
	}


	/**
	 * Fonction qui détermine la date de référence
	 * @param reminder
	 * 			Une relance
	 * @return
	 * 			La date de référence
	 */
	public LocalDate getReferenceDate(Reminder reminder)  {
		AccountingSituation accountingSituation = reminder.getAccountingSituation();
		List<MoveLine> moveLineList = this.getMoveLineReminder(accountingSituation.getPartner(), accountingSituation.getCompany());

		// Date la plus ancienne des lignes d'écriture
		LocalDate minMoveLineDate = getOldDateMoveLine(moveLineList);
		log.debug("minMoveLineDate : {}",minMoveLineDate);

		// 2: Date la plus récente des relances
		LocalDate reminderLastDate = getLastDateReminder(reminder);
		log.debug("reminderLastDate : {}",reminderLastDate);

		// Date de référence : Date la plus récente des deux ensembles (1 et 2)
		LocalDate reminderRefDate = getLastDate(minMoveLineDate, reminderLastDate);
		log.debug("reminderRefDate : {}",reminderRefDate);

		return reminderRefDate;
	}



	/**
	 * Fonction permettant de récuperer une liste de ligne d'écriture exigible relançable d'un tiers
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 * @return
	 * 			La liste de ligne d'écriture
	 */
	public List<MoveLine> getMoveLineReminder(Partner partner, Company company)  {
		List<MoveLine> moveLineList = new ArrayList<MoveLine>();

		List<MoveLine> moveLineQuery = (List<MoveLine>) this.getMoveLine(partner, company);

		int mailTransitTime = company.getAccountConfig().getMailTransitTime();

		for(MoveLine moveLine : moveLineQuery)  {
			if(moveLine.getMove()!=null && !moveLine.getMove().getIgnoreInReminderOk())  {
				Move move = moveLine.getMove();
				//facture exigibles non bloquée en relance et dont la date de facture + délai d'acheminement < date du jour
				if(move.getInvoice()!=null && !move.getInvoice().getReminderBlockingOk()
						&& !move.getInvoice().getSchedulePaymentOk()
						&& ((move.getInvoice().getInvoiceDate()).plusDays(mailTransitTime)).isBefore(today))  {
					if((moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)
							&& moveLine.getDueDate() != null
							&&	(today.isAfter(moveLine.getDueDate())  || today.isEqual(moveLine.getDueDate())))  {
						if(moveLine.getAccount()!=null && moveLine.getAccount().getReconcileOk())  {
							if(moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
								moveLineList.add(moveLine);
							}
						}
					}
				}
				//échéances rejetées qui ne sont pas bloqués
				else if(move.getInvoice()==null)  {
					if(moveLine.getPaymentScheduleLine() != null
							&& (moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)
							&& moveLine.getDueDate() != null
							&&	(today.isAfter(moveLine.getDueDate())  || today.isEqual(moveLine.getDueDate())))  {
						if(moveLine.getAccount()!=null && moveLine.getAccount().getReconcileOk())  {
							if(moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
								moveLineList.add(moveLine);
							}
						}
					}
				}
			}
		}
		return moveLineList;
	}


	public List<Invoice> getInvoiceList(List<MoveLine> moveLineList)  {
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		for(MoveLine moveLine : moveLineList)  {
			if(moveLine.getMove().getInvoice()!=null && !moveLine.getMove().getInvoice().getReminderBlockingOk() )  {
				invoiceList.add(moveLine.getMove().getInvoice());
			}
		}
		return invoiceList;
	}


	public List<PaymentScheduleLine> getPaymentScheduleList(List<MoveLine> moveLineList, Partner partner)  {
		List<PaymentScheduleLine> paymentScheduleLineList = new ArrayList<PaymentScheduleLine>();
		for(MoveLine moveLine : moveLineList)  {
			if(moveLine.getMove().getInvoice()==null )  {
				// Ajout à la liste des échéances exigibles relançables
				PaymentScheduleLine paymentScheduleLine = getPaymentScheduleFromMoveLine(partner, moveLine);
				if(paymentScheduleLine != null)  {
					// Si un montant reste à payer, c'est à dire une échéance rejeté
					if(moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0)  {
						paymentScheduleLineList.add(paymentScheduleLine);
					}
				}
			}
		}
		return paymentScheduleLineList;
	}

	/**
	 * Méthode permettant de récupérer l'ensemble des lignes d'écriture d'un tiers
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 * @return
	 */
	public List<? extends MoveLine> getMoveLine(Partner partner, Company company)  {

		return moveLineRepo.all().filter("self.partner = ?1 and self.move.company = ?2", partner, company).fetch();

	}


	/**
	 * Méthode permettant de récupérer une ligne d'échéancier depuis une ligne d'écriture
	  * @param partner
	 * 			Un tiers
	 * @param moveLine
	 * @return
	 */
	public PaymentScheduleLine getPaymentScheduleFromMoveLine(Partner partner, MoveLine moveLine)  {
		return paymentScheduleLineRepo.all().filter("self.rejectMoveLine = ?1", moveLine).fetchOne();
	}


	/**
	 * Procédure permettant de tester si aujourd'hui nous sommes dans une période particulière
	 * @param dayBegin
	 * 			Le jour du début de la période
	 * @param dayEnd
	 * 			Le jour de fin de la période
	 * @param monthBegin
	 * 			Le mois de début de la période
	 * @param monthEnd
	 * 			Le mois de fin de la période
	 * @return
	 * 			Sommes-nous dans la période?
	 */
	public boolean periodOk(int dayBegin, int dayEnd, int monthBegin, int monthEnd)  {

		return DateTool.dateInPeriod(today, dayBegin, monthBegin, dayEnd, monthEnd);

	}


	public Reminder getReminder(Partner partner, Company company) throws AxelorException  {
		
		AccountingSituationRepository accSituationRepo = Beans.get(AccountingSituationRepository.class);
		AccountingSituation accountingSituation = accSituationRepo.all().filter("self.partner = ?1 and self.company = ?2", partner, company).fetchOne();
		
		if(accountingSituation != null)  {
			if(accountingSituation.getReminder() != null)  {
				return accountingSituation.getReminder();
			}
			else  {
				return this.createReminder(accountingSituation);
			}
		}

		else  {
			throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s, "+I18n.get("Société")+" %s : "+I18n.get(IExceptionMessage.REMINDER_1),
					GeneralServiceImpl.EXCEPTION, partner.getName(), company.getName()), IException.CONFIGURATION_ERROR);
		}
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public Reminder createReminder(AccountingSituation accountingSituation)  {
		Reminder reminder = new Reminder();
		reminder.setAccountingSituation(accountingSituation);
		reminderRepo.save(reminder);
		return reminder;
	}


	/**
	 * Méthode de relance en masse
	 * @param partner
	 * 			Un tiers
	 * @param company
	 * 			Une société
	 * @throws AxelorException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public boolean reminderGenerate(Partner partner, Company company) throws AxelorException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		boolean remindedOk = false;

		Reminder reminder = this.getReminder(partner, company); // ou getReminder si existe

		BigDecimal balanceDue = accountCustomerService.getBalanceDue(partner, company);

		if (balanceDue.compareTo(BigDecimal.ZERO) > 0)  {

			reminder.setBalanceDue(balanceDue);
			log.debug("balanceDue : {} ",balanceDue);

			BigDecimal balanceDueReminder = accountCustomerService.getBalanceDueReminder(partner, company);

			if (balanceDueReminder.compareTo(BigDecimal.ZERO) > 0) {
				log.debug("balanceDueReminder : {} ",balanceDueReminder);

				remindedOk = true;

				List<MoveLine> moveLineList = this.getMoveLineReminder(partner, company);

				this.updateInvoiceReminder(reminder,  this.getInvoiceList(moveLineList));
				this.updatePaymentScheduleLineReminder(reminder, this.getPaymentScheduleList(moveLineList, partner));

				reminder.setBalanceDueReminder(balanceDueReminder);

				Integer levelReminder = 0;
				if(reminder.getReminderMethodLine() != null)  {
					levelReminder = reminder.getReminderMethodLine().getReminderLevel().getName();
				}

				LocalDate referenceDate = this.getReferenceDate(reminder);

				if(referenceDate != null)  {
					log.debug("date de référence : {} ",referenceDate);
					reminder.setReferenceDate(referenceDate);
				}
				else {
					throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s, "+I18n.get("Société")+" %s : "+I18n.get(IExceptionMessage.REMINDER_2),
							GeneralServiceImpl.EXCEPTION, partner.getName(), company.getName()), IException.CONFIGURATION_ERROR);
				}
				if(reminder.getReminderMethod() == null)  {
					if(reminderSessionService.getReminderMethod(reminder)!=null)  {
						reminder.setReminderMethod(reminderSessionService.getReminderMethod(reminder));
						reminderSessionService.reminderSession(reminder);
					}
					else  {
						throw new AxelorException(String.format("%s :\n"+I18n.get("Tiers")+" %s, "+I18n.get("Société")+" %s : "+I18n.get(IExceptionMessage.REMINDER_3),
								GeneralServiceImpl.EXCEPTION, partner.getName(), company.getName()), IException.CONFIGURATION_ERROR);
					}
				}
				else {
					reminderSessionService.reminderSession(reminder);
				}
				if(reminder.getWaitReminderMethodLine()==null)  {
					// Si le niveau de relance à évolué
					if(reminder.getReminderMethodLine() != null && reminder.getReminderMethodLine().getReminderLevel() != null &&
							reminder.getReminderMethodLine().getReminderLevel().getName() > levelReminder)  {
						reminderActionService.runAction(reminder);
					}
				}
				else  {
					log.debug("Tiers {}, Société {} - Niveau de relance en attente ", partner.getName(), company.getName());
					// TODO Alarm ?
					TraceBackService.trace(new AxelorException(
						String.format("%s :\n"+I18n.get("Tiers")+" %s, "+I18n.get("Société")+" %s : "+I18n.get(IExceptionMessage.REMINDER_4),
								GeneralServiceImpl.EXCEPTION, partner.getName(), company.getName()), IException.INCONSISTENCY));
				}
			}
		}
		else  {
			reminderSessionService.reminderInitialisation(reminder);
		}
		return remindedOk;
	}


	public void updateInvoiceReminder(Reminder reminder, List<Invoice> invoiceList)  {
		reminder.setInvoiceReminderSet(new HashSet<Invoice>());
		reminder.getInvoiceReminderSet().addAll(invoiceList);
	}

	public void updatePaymentScheduleLineReminder(Reminder reminder, List<PaymentScheduleLine> paymentSchedueLineList)  {
		reminder.setPaymentScheduleLineReminderSet(new HashSet<PaymentScheduleLine>());
		reminder.getPaymentScheduleLineReminderSet().addAll(paymentSchedueLineList);
	}

}
