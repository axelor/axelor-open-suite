/**
 * 
 */
package com.axelor.apps.tool.exception;

/**
 * @author axelor
 *
 */
public interface IExceptionMessage {
	
	/**
	 * Period service
	 */
	static final String PERIOD_1 = /*$$(*/ "Années sur 360 jours" /*)*/;
	
	/**
	 * URL service
	 */
	static final String URL_SERVICE_1 = /*$$(*/  "Can not opening the connection to a empty URL."  /*)*/;
	static final String URL_SERVICE_2 = /*$$(*/  "Url %s is malformed."  /*)*/;
	static final String URL_SERVICE_3 = /*$$(*/  "An error occurs while opening the connection. Please verify the following URL : %s." /*)*/;
	
	/**
	 * Template maker
	 */
	static final String TEMPLATE_MAKER_1 = /*$$(*/  "No such template"  /*)*/;
	static final String TEMPLATE_MAKER_2 = /*$$(*/  "Templating can not be empty"  /*)*/;
	
}
