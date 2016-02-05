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
package com.axelor.apps.base.service.scheduler;

import java.util.ArrayList;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Scheduler;
import com.axelor.apps.base.db.SchedulerInstance;
import com.axelor.apps.base.db.SchedulerInstanceHistory;
import com.axelor.apps.base.db.repo.SchedulerInstanceRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * SchedulerService est une classe implémentant l'ensemble des services des
 * planificateurs.
 *
 * @author P. Belloy
 *
 * @version 1.0
 *
 */
public class SchedulerService {


	protected GeneralService generalService;

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private LocalDate today;

	@Inject
	private SchedulerInstanceRepository schedulerInstanceRepo;

	@Inject
	public SchedulerService(GeneralService generalService){
		this.generalService = generalService;
		today = this.generalService.getTodayDate();

	}

	/**
	 * Méthode qui détermine si une instance de planificateur peut etre lancé
	 *
	 * @param schedulerI
	 * 		Instance de planificateur
	 *
	 * @return boolean
	 * 		True si il est temps de lancer, sinon False
	 *
	 * @throws AxelorException
	 */
	public boolean isSchedulerInstanceIsReady(SchedulerInstance schedulerI) throws AxelorException{

		LocalDate firstExecution = schedulerI.getFirstExecutionDate();
		LocalDate lastTheoreticalExecution = schedulerI.getLastTheoreticalExecutionDate();

		//Si date de démarrage egal ou supérieur à date du jour
		if(lastTheoreticalExecution == null ){
			LOG.debug("Date de dernière exécution absente");
			if(firstExecution == null){
				LOG.debug(String.format("Date de premiere executon absente"));
			}
			else if(firstExecution.equals(today) || firstExecution.isBefore(today)){
				LOG.debug("Date de démarrage supérieur ou égal a date du jour");
				return true;
			}
			else{
				LOG.debug(String.format("La facturation ne peux pas encore être exécuté : %s < %s",today,firstExecution));
			}
		}
		else{
			//Sinon calcul le cycle
			return this.processScheduler(schedulerI);
		}

		return false;
	}

	/**
	 * Méthode qui détermine si une instance de planificateur peut etre lancé
	 *
	 * @param schedulerI
	 * 		Instance de planificateur
	 *
	 * @return boolean
	 * 		True si il est temps de lancer, sinon False
	 *
	 * @throws AxelorException
	 */
	private boolean processScheduler(SchedulerInstance schedulerI) throws AxelorException{

		LocalDate date = this.getComputeDate(schedulerI);

		LOG.debug("Date dernière éxécution théorique: "+schedulerI.getLastTheoreticalExecutionDate());
		LOG.debug("Date calculé : "+date);
		LOG.debug("Date du jour : "+today);

		if(date != null && (date.equals(today) || date.isBefore(today)))
			return true;

		return false;
	}

	/**
	 * Méthode qui calcule da date de prochaine éxécution d'une instance d planificateur
	 *
	 * @param schedulerI
	 * 		Instance de planificateur
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 *
	 * @throws AxelorException
	 */
	public LocalDate getComputeDate(SchedulerInstance schedulerI) throws AxelorException{

		return this.getComputeDate(
				schedulerI.getScheduler(),
				schedulerI.getLastTheoreticalExecutionDate());

	}



	/**
	 * Méthode qui calcule da date de prochaine éxécution d'une instance d planificateur
	 *
	 * @param schedulerI
	 * 		Instance de planificateur
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 *
	 * @throws AxelorException
	 */
	public LocalDate getComputeDate(Scheduler scheduler, LocalDate date) throws AxelorException{

		if(scheduler.getAnnually())
			return this.getAnnualComputeDate(scheduler,date);
		else if(scheduler.getMonthly())
			return this.getMonthlyComputeDate(scheduler,date);
		else if(scheduler.getWeekly())
			return this.getWeeklyComputeDate(scheduler,date);
		else if(scheduler.getDaily())
			return this.getDailyComputeDate(scheduler,date);
		else
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.SCHEDULER_1),scheduler.getName()), IException.MISSING_FIELD);
	}



	/**
	 * Méthode qui détermine la prochaine date d'éxécution d'un planificateur pour un rythme quotidien
	 *
	 * @param scheduler
	 * 		Instance de planificateur
	 * @param date
	 * 		Derniere date d'éxécution théorique
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 */
	private LocalDate getDailyComputeDate(Scheduler scheduler,LocalDate date){

		return date.plusDays(scheduler.getDayDaily());
	}

	/**
	 * Méthode qui détermine la prochaine date d'éxécution d'un planificateur pour un rythme hebdomadaire
	 *
	 * @param scheduler
	 * 		Instance de planificateur
	 * @param date
	 * 		Derniere date d'éxécution théorique
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 */
	private LocalDate getWeeklyComputeDate(Scheduler scheduler,LocalDate date){

		int weekDay = 0;

		if(scheduler.getMonday())
			weekDay = DateTimeConstants.MONDAY;
		else if(scheduler.getTuesday())
			weekDay = DateTimeConstants.TUESDAY;
		else if(scheduler.getWednesday())
			weekDay = DateTimeConstants.WEDNESDAY;
		else if(scheduler.getThursday())
			weekDay = DateTimeConstants.THURSDAY;
		else if(scheduler.getFriday())
			weekDay = DateTimeConstants.FRIDAY;
		else if(scheduler.getSaturday())
			weekDay = DateTimeConstants.SATURDAY;
		else if(scheduler.getSunday())
			weekDay = DateTimeConstants.SUNDAY;

		if(weekDay == 0)
			weekDay = 1;

		return date.plusWeeks(scheduler.getWeekWeekly()).withDayOfWeek(weekDay);
	}

	/**
	 * Méthode qui détermine la prochaine date d'éxécution d'un planificateur pour un rythme mensuel
	 *
	 * @param scheduler
	 * 		Instance de planificateur
	 * @param date
	 * 		Derniere date d'éxécution théorique
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 */
	private LocalDate getMonthlyComputeDate(Scheduler scheduler,LocalDate date){

		if(scheduler.getDayMonthly() == 0)
			return date.plusMonths(scheduler.getMonthMonthly()).withDayOfMonth(1);
		else {

			int start = date.plusWeeks(scheduler.getWeekWeekly()).dayOfMonth().getMinimumValue();
			int end = date.plusWeeks(scheduler.getWeekWeekly()).dayOfMonth().getMaximumValue();

			if(start <= scheduler.getDayMonthly() && scheduler.getDayMonthly() <= end)
				return date.plusMonths(scheduler.getMonthMonthly()).withDayOfMonth(scheduler.getDayMonthly());
			else if(scheduler.getDayMonthly() < start)
				return date.plusWeeks(scheduler.getWeekWeekly()).dayOfMonth().withMinimumValue();
			else if(scheduler.getDayMonthly() > end)
				return date.plusWeeks(scheduler.getWeekWeekly()).dayOfMonth().withMaximumValue();

			return null;
		}
	}

	/**
	 * Méthode qui détermine la prochaine date d'éxécution d'un planificateur pour un rythme annuel
	 *
	 * @param scheduler
	 * 		Instance de planificateur
	 * @param date
	 * 		Derniere date d'éxécution théorique
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 */
	private LocalDate getAnnualComputeDate(Scheduler scheduler,LocalDate date){

		int monthOfYear = 0;

		if(scheduler.getMonthAnnuallySelect().equals(IAdministration.JAN))
			monthOfYear = DateTimeConstants.JANUARY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.FEB))
			monthOfYear = DateTimeConstants.FEBRUARY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.MAR))
			monthOfYear = DateTimeConstants.MARCH;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.APR))
			monthOfYear = DateTimeConstants.APRIL;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.MAY))
			monthOfYear = DateTimeConstants.MAY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.JUN))
			monthOfYear = DateTimeConstants.JUNE;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.JUL))
			monthOfYear = DateTimeConstants.JULY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.AUG))
			monthOfYear = DateTimeConstants.AUGUST;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.SEP))
			monthOfYear = DateTimeConstants.SEPTEMBER;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.OCT))
			monthOfYear = DateTimeConstants.OCTOBER;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.NOV))
			monthOfYear = DateTimeConstants.NOVEMBER;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.DEC))
			monthOfYear = DateTimeConstants.DECEMBER;

		if(monthOfYear != 0){

			int start = date.plusWeeks(scheduler.getWeekWeekly()).dayOfMonth().getMinimumValue();
			int end = date.plusWeeks(scheduler.getWeekWeekly()).dayOfMonth().getMaximumValue();

			if(start <= scheduler.getDayAnnually() && scheduler.getDayAnnually() <= end)
				return date.plusYears(scheduler.getYearAnnually()).withMonthOfYear(monthOfYear).withDayOfMonth(scheduler.getDayAnnually());
			else if(scheduler.getDayMonthly() < start)
				return date.plusYears(scheduler.getYearAnnually()).withMonthOfYear(monthOfYear).dayOfMonth().withMinimumValue();
			else if(scheduler.getDayMonthly() > end)
				return date.plusYears(scheduler.getYearAnnually()).withMonthOfYear(monthOfYear).dayOfMonth().withMaximumValue();

		}

		return null;

	}

	/**
	 * Historise l'éxécution
	 *
	 * @param schedulerI
	 * 		Instance de planificateur
	 * @param currentDay
	 * 		Date d'éxécution
	 * @param isImmediate
	 * 		Mettre a jour le cycle ? Dans le cas de facturation mémoire immédiate
	 *
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void addInHistory(SchedulerInstance schedulerI, LocalDate currentDay, boolean isImmediate) throws AxelorException {

		LocalDate date = this.getTheoricalExecutionDate(schedulerI);

		schedulerI.setLastExecutionDate(currentDay);
		if(!isImmediate){
			schedulerI.setLastTheoreticalExecutionDate(date);
		}

		SchedulerInstanceHistory history = new SchedulerInstanceHistory();
		history.setLastExecutionDate(currentDay);
		history.setLastThoereticalExecutionDate(date);
		history.setSchedulerInstance(schedulerI);

		if(schedulerI.getSchedulerInstanceHistoryList() == null){
			schedulerI.setSchedulerInstanceHistoryList(new ArrayList<SchedulerInstanceHistory>());
		}

		schedulerI.getSchedulerInstanceHistoryList().add(history);

		schedulerInstanceRepo.save(schedulerI);
	}

	/**
	 * Méthode qui détermine la prochaine date d'éxécution théorique d'une instance de planificateur
	 *
	 * @param schedulerI
	 * 		Instance de planificateur
	 *
	 * @return LocalDate
	 * 		Prochaine date d'éxécution
	 *
	 * @throws AxelorException
	 */
	public LocalDate getTheoricalExecutionDate(SchedulerInstance schedulerI) throws AxelorException{

		LocalDate theoricalExecutionDate = null;

		if(schedulerI.getLastTheoreticalExecutionDate() != null){

			theoricalExecutionDate = this.getComputeDate(schedulerI);

		}
		else
			theoricalExecutionDate = schedulerI.getFirstExecutionDate();

		return theoricalExecutionDate;
	}

	/**
	 * Obtient le prochain jour du mois à partir d'une date
	 *
	 * @param localDate
	 * 		Date de référence
	 * @param dayMonthly
	 * 		Jour du mois
	 *
	 * @return LocalDate
	 * 		Date correspondant au prochain jour du mois
	 */
	public LocalDate getNextDayMonth(LocalDate localDate, int dayMonthly){
		LocalDate date = null;

		int start = localDate.dayOfMonth().getMinimumValue();
		int end = localDate.dayOfMonth().getMaximumValue();

		if(localDate.dayOfMonth().get() <= dayMonthly){

			if(start <= dayMonthly && dayMonthly <= end)
				date = localDate.withDayOfMonth(dayMonthly);
			else if(dayMonthly < start)
				date = localDate.dayOfMonth().withMinimumValue();
			else if(dayMonthly > end)
				date = localDate.dayOfMonth().withMaximumValue();

		}
		else{

			int startNext = localDate.plusMonths(1).dayOfMonth().getMinimumValue();
			int endNext = localDate.plusMonths(1).dayOfMonth().getMaximumValue();

			if(startNext <= dayMonthly && dayMonthly <= endNext)
				date = localDate.plusMonths(1).withDayOfMonth(dayMonthly);
			else if(dayMonthly < startNext)
				date = localDate.plusMonths(1).dayOfMonth().withMinimumValue();
			else if(dayMonthly > endNext)
				date = localDate.plusMonths(1).dayOfMonth().withMaximumValue();

		}

		return date;
	}


}
