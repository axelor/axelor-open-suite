package com.axelor.apps.base.service.administration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.axelor.apps.account.db.Vat;
import com.axelor.apps.base.db.CurrencyConversionLine;
import com.axelor.apps.base.db.FormulaGenerator;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

@Singleton
public final class GeneralService {

	private static final String EXCEPTION = "Warning !";
	
	private static GeneralService INSTANCE;
	
	private Long administrationId;
	
	@Inject
	private GeneralService() {
		
		try {
			administrationId = General.all().fetchOne().getId();
		}
		catch(Exception e) { throw new RuntimeException("Veuillez configurer l'administration générale.", e); }
		
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

// Formules
	
	/**
	 * Obtenir l'unité par défaut pour la facturation de l'abonnement.
	 * 
	 * @return 
	 * @throws AxelorException 
	 */
	public static FormulaGenerator getFormulaGenerator() throws AxelorException {

		if (getGeneral() != null) {
			return getGeneral().getFormulaGenerator();
		}
		else {
			throw new AxelorException("Formule indisponible dans l'administration Axelor", IException.CONFIGURATION_ERROR);
		}
		
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


// TVA 
	
	/**
	 * Obtenir la tva à 0%
	 * 
	 * @return
	 */
	public static Vat getDefaultExemptionVat(){
		if (getGeneral() != null) { return getGeneral().getDefaultExemptionVat(); }
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
