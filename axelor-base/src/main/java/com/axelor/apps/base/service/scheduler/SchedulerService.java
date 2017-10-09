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
package com.axelor.apps.base.service.scheduler;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Scheduler;
import com.axelor.apps.base.db.SchedulerInstance;
import com.axelor.apps.base.db.SchedulerInstanceHistory;
import com.axelor.apps.base.db.repo.SchedulerInstanceRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
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


	protected AppBaseService appBaseService;

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private LocalDate today;

	@Inject
	private SchedulerInstanceRepository schedulerInstanceRepo;

	@Inject
	public SchedulerService(AppBaseService appBaseService){
		this.appBaseService = appBaseService;
		today = this.appBaseService.getTodayDate();

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
			throw new AxelorException(IException.MISSING_FIELD, I18n.get(IExceptionMessage.SCHEDULER_1),scheduler.getName());
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

		DayOfWeek weekDay = DayOfWeek.MONDAY;

		if(scheduler.getMonday())
			weekDay = DayOfWeek.MONDAY;
		else if(scheduler.getTuesday())
			weekDay = DayOfWeek.TUESDAY;
		else if(scheduler.getWednesday())
			weekDay = DayOfWeek.WEDNESDAY;
		else if(scheduler.getThursday())
			weekDay = DayOfWeek.THURSDAY;
		else if(scheduler.getFriday())
			weekDay = DayOfWeek.FRIDAY;
		else if(scheduler.getSaturday())
			weekDay = DayOfWeek.SATURDAY;
		else if(scheduler.getSunday())
			weekDay = DayOfWeek.SUNDAY;

		
		return date.plusWeeks(scheduler.getWeekWeekly()).with(TemporalAdjusters.next(weekDay));
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

			int start = 1;
			int end = date.plusWeeks(scheduler.getWeekWeekly()).lengthOfMonth();

			if(start <= scheduler.getDayMonthly() && scheduler.getDayMonthly() <= end)
				return date.plusMonths(scheduler.getMonthMonthly()).withDayOfMonth(scheduler.getDayMonthly());
			else if(scheduler.getDayMonthly() < start)
				return date.plusWeeks(scheduler.getWeekWeekly()).withDayOfMonth(start);
			else if(scheduler.getDayMonthly() > end)
				return date.plusWeeks(scheduler.getWeekWeekly()).withDayOfMonth(end);

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

		Month monthOfYear = null;

		if(scheduler.getMonthAnnuallySelect().equals(IAdministration.JAN))
			monthOfYear = Month.JANUARY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.FEB))
			monthOfYear = Month.FEBRUARY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.MAR))
			monthOfYear = Month.MARCH;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.APR))
			monthOfYear = Month.APRIL;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.MAY))
			monthOfYear = Month.MAY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.JUN))
			monthOfYear = Month.JUNE;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.JUL))
			monthOfYear = Month.JULY;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.AUG))
			monthOfYear = Month.AUGUST;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.SEP))
			monthOfYear = Month.SEPTEMBER;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.OCT))
			monthOfYear = Month.OCTOBER;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.NOV))
			monthOfYear = Month.NOVEMBER;
		else if(scheduler.getMonthAnnuallySelect().equals(IAdministration.DEC))
			monthOfYear = Month.DECEMBER;

		if(monthOfYear != null){

			int start = 1;
			int end = date.plusWeeks(scheduler.getWeekWeekly()).lengthOfMonth();

			if(start <= scheduler.getDayAnnually() && scheduler.getDayAnnually() <= end)
				return date.plusYears(scheduler.getYearAnnually()).withMonth(monthOfYear.getValue()).withDayOfMonth(scheduler.getDayAnnually());
			else if(scheduler.getDayMonthly() < start)
				return date.plusYears(scheduler.getYearAnnually()).withMonth(monthOfYear.getValue()).withDayOfMonth(1);
			else if(scheduler.getDayMonthly() > end)
				return date.plusYears(scheduler.getYearAnnually()).withMonth(monthOfYear.getValue()).withDayOfMonth(end);

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

		int start = 1;
		int end = localDate.lengthOfMonth();

		if(localDate.getDayOfMonth() <= dayMonthly){

			if(start <= dayMonthly && dayMonthly <= end)
				date = localDate.withDayOfMonth(dayMonthly);
			else if(dayMonthly < start)
				date = localDate.withDayOfMonth(start);
			else if(dayMonthly > end)
				date = localDate.withDayOfMonth(end);

		}
		else{

			int startNext = 1;
			int endNext = localDate.plusMonths(1).lengthOfMonth();

			if(startNext <= dayMonthly && dayMonthly <= endNext)
				date = localDate.plusMonths(1).withDayOfMonth(dayMonthly);
			else if(dayMonthly < startNext)
				date = localDate.plusMonths(1).withDayOfMonth(startNext);
			else if(dayMonthly > endNext)
				date = localDate.plusMonths(1).withDayOfMonth(endNext);

		}

		return date;
	}


}
