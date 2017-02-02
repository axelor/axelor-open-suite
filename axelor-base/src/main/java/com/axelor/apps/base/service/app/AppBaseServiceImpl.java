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
package com.axelor.apps.base.service.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Query;

import org.hibernate.proxy.HibernateProxy;
import java.time.ZonedDateTime;
import java.time.LocalDate;

import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.google.inject.Inject;

@Singleton
public class AppBaseServiceImpl extends AppServiceImpl implements AppBaseService {

	public static final String EXCEPTION = "Warning !";

	private static AppBaseServiceImpl INSTANCE;

	private Long administrationId;
	
	@Inject
	private AppBaseRepository appBaseRepo;
	
	@Inject
	public AppBaseServiceImpl() {
		Query q = JPA.em().createQuery("FROM AppBase");
		AppBase appBase = (AppBase)q.setMaxResults(1).getSingleResult();
		if(appBase != null)  {
			administrationId = appBase.getId();
		}
		else  {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}

	}

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
		return appBaseRepo.find(administrationId);
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

		User user = AuthUtils.getUser();

		if (user != null && user.getToday() != null){
			todayDateTime = user.getToday();
		}
		else if (getAppBase() != null && getAppBase().getToday() != null){
			todayDateTime = getAppBase().getToday();
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

		if (getAppBase() != null){
			return getAppBase().getDefaultProjectUnit();
		}

		return null;
	}


	@Override
	public int getNbDecimalDigitForUnitPrice(){

		if (getAppBase() != null){
			return getAppBase().getNbDecimalDigitForUnitPrice();
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
		if (getAppBase() != null) { return getAppBase().getCurrencyConversionLineList(); }
		else { return null; }
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends Model> getPersistentClass(Model model){

		if (model instanceof HibernateProxy) {
		      return ((HibernateProxy) model).getHibernateLazyInitializer().getPersistentClass();
		}
		else { return model.getClass(); }

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

		AppBase appBase = this.getAppBase();

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

}
