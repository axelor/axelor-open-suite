/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.administration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.proxy.HibernateProxy;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import com.axelor.inject.Beans;

@Singleton
public class GeneralService extends GeneralRepository{

	protected static final String EXCEPTION = "Warning !";

	private static GeneralService INSTANCE;

	private Long administrationId;

	@Inject
	protected GeneralService() {

		General general = all().fetchOne();
		if(general != null)  {
			administrationId = all().fetchOne().getId();
		}
		else  {
			throw new RuntimeException("Veuillez configurer l'administration générale.");
		}

	}

	private static GeneralService get() {

		if (INSTANCE == null) { INSTANCE = new GeneralService(); }

		return INSTANCE;
	}

// Accesseur

	/**
	 * Récupérer l'administration générale
	 *
	 * @return
	 */
	public static General getGeneral() {
		return Beans.get(GeneralRepository.class).find(get().administrationId);
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
	public static DateTime getTodayDateTime(){

		DateTime todayDateTime = new DateTime();

		User user = AuthUtils.getUser();

		if (user != null && user.getToday() != null){
			todayDateTime = user.getToday();
		}
		else if (getGeneral() != null && getGeneral().getToday() != null){
			todayDateTime = getGeneral().getToday();
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
	public static LocalDate getTodayDate(){

		return getTodayDateTime().toLocalDate();

	}



// Log

	/**
	 * Savoir si le logger est activé
	 *
	 * @return
	 */
	public static boolean isLogEnabled(){

		if (getGeneral() != null){
			return getGeneral().getLogOk();
		}

		return false;
	}

	public static Unit getUnit(){

		if (getGeneral() != null){
			return getGeneral().getDefaultProjectUnit();
		}

		return null;
	}


// Message exception



	/**
	 * Obtenir le message d'erreur pour les achats/ventes.
	 *
	 * @return
	 */
	public static String getExceptionSupplychainMsg(){

			return EXCEPTION;

	}


// Conversion de devise

	/**
	 * Obtenir la tva à 0%
	 *
	 * @return
	 */
	public static List<CurrencyConversionLine> getCurrencyConfigurationLineList(){
		if (getGeneral() != null) { return getGeneral().getCurrencyConversionLineList(); }
		else { return null; }
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Model> getPersistentClass(Model model){

		if (model instanceof HibernateProxy) {
		      return ((HibernateProxy) model).getHibernateLazyInitializer().getPersistentClass();
		}
		else { return model.getClass(); }

	}

}
