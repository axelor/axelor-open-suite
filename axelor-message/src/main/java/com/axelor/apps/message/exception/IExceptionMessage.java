/**
 * 
 */
package com.axelor.apps.message.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	
	/**
	 * Mail account service and controller
	 */
	static final String MAIL_ACCOUNT_1 = /*$$(*/ "Incorrect login or password" /*)*/;
	static final String MAIL_ACCOUNT_2 = /*$$(*/ "Unable to reach server. Please check Host,Port and SSL/TLS" /*)*/;
	static final String MAIL_ACCOUNT_3 = /*$$(*/ "Connection successful" /*)*/;
	static final String MAIL_ACCOUNT_4 = /*$$(*/ "Provided settings are wrong, please modify them and try again" /*)*/;
	
	/**
	 * Template service
	 */
	static final String TEMPLATE_SERVICE_1 = /*$$(*/ "Model empty. Please configure a model." /*)*/;
	static final String TEMPLATE_SERVICE_2 = /*$$(*/ "Your target receptor is not valid. Please check it." /*)*/;
	
	/**
	 * General message controller
	 */
	static final String MESSAGE_1 = /*$$(*/ "Veuillez configurer un template" /*)*/;
	static final String MESSAGE_2 = /*$$(*/ "Select template" /*)*/;
	static final String MESSAGE_3 = /*$$(*/ "Create message" /*)*/;
	static final String MESSAGE_4 = /*$$(*/ "Email envoyé" /*)*/;
	static final String MESSAGE_5 = /*$$(*/ "Message envoyé" /*)*/;
	static final String MESSAGE_6 = /*$$(*/ "Echec envoie email" /*)*/;
	
}
