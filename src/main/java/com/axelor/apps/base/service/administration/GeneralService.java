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
package com.axelor.apps.base.service.administration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.user.UserInfoService;

@Singleton
public final class GeneralService {

	private static final String EXCEPTION = "Warning !";
	
	private static GeneralService INSTANCE;
	
	private Long administrationId;
	
	@Inject
	private GeneralService() {
	
		General general = General.all().fetchOne();
		if(general != null)  {
			administrationId = General.all().fetchOne().getId();
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
		return General.find(get().administrationId);
	}

// Date du jour
	
	/**
	 * Récupérer la date du jour avec l'heure.
	 * Retourne la date du jour paramétré dans l'utilisateur si existe,
	 * sinon récupère celle de l'administration générale,
	 * sinon date du jour.
	 * 
	 * @return
	 */
	public static DateTime getTodayDateTime(){	
		
		DateTime todayDateTime = new DateTime();
		
		UserInfoService userInfoService = new UserInfoService();
		UserInfo user = userInfoService.getUserInfo();
		
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
	 * Obtenir le message d'erreur pour la facturation.
	 * 
	 * @return
	 */
	public static String getExceptionInvoiceMsg(){
		
		if (getGeneral() != null) {
			
			if (getGeneral().getExceptionInvoiceMsg() != null ) {
				return getGeneral().getExceptionInvoiceMsg();
			}
			else {
				return getGeneral().getExceptionDefaultMsg();
			}
		}
		else {
			return EXCEPTION;
		}
		
	}
	
	/**
	 * Obtenir le message d'erreur pour la relance.
	 * 
	 * @return
	 */
	public static String getExceptionReminderMsg(){
		
		if (getGeneral() != null) {
			
			if (getGeneral().getExceptionReminderMsg() != null ) {
				return getGeneral().getExceptionReminderMsg();
			}
			else {
				return getGeneral().getExceptionDefaultMsg();
			}
		}
		else {
			return EXCEPTION;
		}
		
	}
	
	/**
	 * Obtenir le message d'erreur pour le moteur d'email et courrier.
	 * 
	 * @return
	 */
	public static String getExceptionMailMsg(){
		
		if (getGeneral() != null) {
			
			if (getGeneral().getExceptionMailMsg() != null ) {
				return getGeneral().getExceptionMailMsg();
			}
			else {
				return getGeneral().getExceptionDefaultMsg();
			}
		}
		else {
			return EXCEPTION;
		}
		
	}
	
	/**
	 * Obtenir le message d'erreur pour la compta.
	 * 
	 * @return
	 */
	public static String getExceptionAccountingMsg(){
		
		if (getGeneral() != null) {
			
			if (getGeneral().getExceptionAccountingMsg() != null ) {
				return getGeneral().getExceptionAccountingMsg();
			}
			else {
				return getGeneral().getExceptionDefaultMsg();
			}
		}
		else {
			return EXCEPTION;
		}
		
	}
	
	/**
	 * Obtenir le message d'erreur pour les achats/ventes.
	 * 
	 * @return
	 */
	public static String getExceptionSupplychainMsg(){
		
		if (getGeneral() != null) {
			
			if (getGeneral().getExceptionSupplychainMsg() != null ) {
				return getGeneral().getExceptionSupplychainMsg();
			}
			else {
				return getGeneral().getExceptionDefaultMsg();
			}
		}
		else {
			return EXCEPTION;
		}
		
	}



// Tax 
	
	/**
	 * Obtenir la tva à 0%
	 * 
	 * @return
	 */
	public static Tax getDefaultExemptionTax(){
		if (getGeneral() != null) { return getGeneral().getDefaultExemptionTax(); }
		else { return null; }
	}
	
	
// Consolidation des écritures de factures
	/**
	 * Savoir si le logger est activé
	 * 
	 * @return
	 */
	public static boolean IsInvoiceMoveConsolidated(){
		
		if (getGeneral() != null){
			return getGeneral().getIsInvoiceMoveConsolidated();
		}
		
		return false;
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
	
}
