/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import javax.inject.Singleton;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

@Singleton
public class AppBaseServiceImpl extends AppServiceImpl implements AppBaseService {

	public static final String EXCEPTION = "Warning !";

	private static AppBaseServiceImpl INSTANCE;

	@Inject
	private AppBaseRepository appBaseRepo;
	

	public static AppBaseServiceImpl get() {

		if (INSTANCE == null) { INSTANCE = new AppBaseServiceImpl(); }

		return INSTANCE;
	}

// Accesseur

	/**
	 * Récupérer l'administration générale
	 *
	 * @return
	 */
	@Override
	public AppBase getAppBase() {
		return appBaseRepo.all().fetchOne();
	}

// Date du jour

	/**
	 * Récupérer la date du jour avec l'heure.
	 * Retourne la date du jour paramétré dans l'utilisateur si existe,
	 * sinon récupère celle de l'administration générale,
	 * sinon date du jour.
	 * private
	 * @return
	 */
	@Override
	public ZonedDateTime getTodayDateTime(){

		ZonedDateTime todayDateTime = ZonedDateTime.now();

		String applicationMode = AppSettings.get().get("application.mode", "prod");

		if ("dev".equals(applicationMode)) {
			User user = AuthUtils.getUser();
			if (user != null && user.getToday() != null){
				todayDateTime = user.getToday();
			}
			else { 
				AppBase appBase = getAppBase();
				if (appBase != null && appBase.getToday() != null){
					return appBase.getToday();
				}
			}
		}

		return todayDateTime;
	}

	/**
	 * Récupérer la date du jour.
	 * Retourne la date du jour paramétré dans l'utilisateur si existe,
	 * sinon récupère celle de l'administration générale,
	 * sinon date du jour.
	 *
	 * @return
	 */
	@Override
	public LocalDate getTodayDate(){

		return getTodayDateTime().toLocalDate();

	}



// Log

	@Override
	public Unit getUnit(){
		
		AppBase appBase = getAppBase();
		
		if (appBase != null){
			return appBase.getDefaultProjectUnit();
		}

		return null;
	}


	@Override
	public int getNbDecimalDigitForUnitPrice(){
		
		AppBase appBase = getAppBase();
		
		if (appBase != null){
			return appBase.getNbDecimalDigitForUnitPrice();
		}

		return IAdministration.DEFAULT_NB_DECIMAL_DIGITS;
	}


// Conversion de devise

	/**
	 * Obtenir la tva à 0%
	 *
	 * @return
	 */
	@Override
	public List<CurrencyConversionLine> getCurrencyConfigurationLineList(){
		AppBase appBase = getAppBase();
		if (appBase != null) { return appBase.getCurrencyConversionLineList(); }
		else { return null; }
	}

	@Override
	public BigDecimal getDurationHours(BigDecimal duration){

		if(duration == null) { return null; }

		AppBase appBase = this.getAppBase();

		if(appBase != null){
			String timePref = appBase.getTimeLoggingPreferenceSelect();

			if(timePref.equals("days")){
				duration = duration.multiply(appBase.getDailyWorkHours());
			}
			else if (timePref.equals("minutes")) {
				duration = duration.divide(new BigDecimal(60), 2, RoundingMode.HALF_EVEN);
			}
		}

		return duration;
	}

	@Override
	public BigDecimal getGeneralDuration(BigDecimal duration){

		if(duration == null) { return null; }

		AppBase appBase = getAppBase();

		if(appBase != null){
			String timePref = appBase.getTimeLoggingPreferenceSelect();

			BigDecimal dailyWorkHrs = appBase.getDailyWorkHours();

			if(timePref.equals("days") && dailyWorkHrs != null && dailyWorkHrs.compareTo(BigDecimal.ZERO) != 0){
				duration = duration.divide(dailyWorkHrs, 2, RoundingMode.HALF_EVEN);
			}
			else if (timePref.equals("minutes")) {
				duration = duration.multiply(new BigDecimal(60));
			}
		}

		return duration;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(rollbackOn = { AxelorException.class, Exception.class })
	public void setManageMultiBanks(boolean manageMultiBanks) {
		getAppBase().setManageMultiBanks(manageMultiBanks);
	}

}
