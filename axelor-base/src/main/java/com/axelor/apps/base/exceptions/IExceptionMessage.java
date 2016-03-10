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
package com.axelor.apps.base.exceptions;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	static final public String NOT_IMPLEMENTED_METHOD = /*$$(*/ "Not implemented yet!" /*)*/;
	
	static final String BIRT_EXTERNAL_REPORT_NO_URL = /*$$(*/ "Impossible to generate report, url toward Birt viewer is not correclty configured (%s)" /*)*/;
	
	
	/**
	 * Currency service and controller
	 */
	
	static final String CURRENCY_1 = /*$$(*/ "No currency conversion found from '%s' to '%s' for date %s" /*)*/ ;
	static final String CURRENCY_2 = /*$$(*/ "The currency exchange rate from '%s' to '%s' for date %s must be different from zero." /*)*/ ;
	static final String CURRENCY_3 = /*$$(*/ "WARNING : please close the current conversion period to create a new one." /*)*/ ;
	static final String CURRENCY_4 = /*$$(*/ "The end date has to be greater than or equal to the start date." /*)*/ ;
	static final String CURRENCY_5 = /*$$(*/ "Both currencies must be saved before currency rate apply" /*)*/ ;
	static final String CURRENCY_6 = /*$$(*/ "Currency conversion webservice not working" /*)*/ ;
	
	

	/**
	 * Unit conversion service
	 */
	
	static final String UNIT_CONVERSION_1 = /*$$(*/ "Please configure unit conversion from '%s' to '%s'." /*)*/ ;
	static final String UNIT_CONVERSION_2 = /*$$(*/ "Please configure units." /*)*/ ;
	
	static final String CURRENCY_CONVERSION_1 = /*$$(*/ "WARNING : Please close the current conversion period before creating new one" /*)*/ ;
	static final String CURRENCY_CONVERSION_2 = /*$$(*/ "WARNING : To Date must be after or equals to From Date" /*)*/ ;
	
	
	/**
	 * Account management service
	 */
	
	static final public  String ACCOUNT_MANAGEMENT_1 = /*$$(*/ "Tax configuration is missing for Product: %s (company: %s)" /*)*/ ;
	static final public  String ACCOUNT_MANAGEMENT_2 = /*$$(*/ "No tax found for product %s" /*)*/ ;
	static final public  String ACCOUNT_MANAGEMENT_3 = /*$$(*/ "Tax is missing for Product: %s (company: %s)" /*)*/ ;

	
	/**
	 * Period service
	 */
	
	static final public String PERIOD_1 = /*$$(*/ "No period found or it has been closed for the company %s" /*)*/ ;
	static final public String PERIOD_2 = /*$$(*/ "Period closed" /*)*/ ;
	
	/**
	 * Abstract batch
	 */
	
	static final public String ABSTRACT_BATCH_1 =  /*$$(*/ "This batch is not runnable !" /*)*/;
	
	/**
	 * Indicator generator grouping service
	 */
	static final public String INDICATOR_GENERATOR_GROUPING_1 = /*$$(*/ "\nError : no export path has been set" /*)*/;
	static final public String INDICATOR_GENERATOR_GROUPING_2 = /*$$(*/ "\nError : no code has been set" /*)*/;
	static final public String INDICATOR_GENERATOR_GROUPING_3 = /*$$(*/ "Error while creating the file" /*)*/;
	static final public String INDICATOR_GENERATOR_GROUPING_4 = /*$$(*/ "Result exported" /*)*/;
	/**
	 * Indicator generator service 
	 */
	static final public String INDICATOR_GENERATOR_1 = /*$$(*/ "Error : a request has to be set for the indicatior generator %s" /*)*/;
	static final public String INDICATOR_GENERATOR_2 = /*$$(*/ "Error : incorrrect request for the indicatior generator %s" /*)*/;
	static final public String INDICATOR_GENERATOR_3 = /*$$(*/ "Request performed" /*)*/;
	
	
	/**
	 * Alarm engine batch service
	 */
	static final public String ALARM_ENGINE_BATCH_1 = /*$$(*/ "Alarm Engine %s" /*)*/;
	static final public String ALARM_ENGINE_BATCH_2 = /*$$(*/ "Alarms report :\n" /*)*/;
	static final public String ALARM_ENGINE_BATCH_3 = /*$$(*/ "\t* %s object(s) into alarm\n" /*)*/;
	static final public String ALARM_ENGINE_BATCH_4 = /*$$(*/ "\t* %s anomaly(ies)" /*)*/;
	static final public String ALARM_ENGINE_BATCH_5 = /*$$(*/ "Alarm batch" /*)*/;

	/**
	 * Base batch service
	 */
	static final public String BASE_BATCH_1 = /*$$(*/ "Unknown action %s for the %s treatment" /*)*/;
	static final public String BASE_BATCH_2 = /*$$(*/ "Batch %s unknown" /*)*/;
	
	/**
	 * Importer
	 */
	static final public String IMPORTER_1 = /*$$(*/ "Error : Mapping file is unreachable." /*)*/;
	
	/**
	 * Importer Listener 
	 */
	static final public String IMPORTER_LISTERNER_1 = /*$$(*/ "\nTotal : " /*)*/;
	static final public String IMPORTER_LISTERNER_2 = /*$$(*/ " - Succeeded : " /*)*/;
	static final public String IMPORTER_LISTERNER_3 = /*$$(*/ "\nGenerated anomalies :" /*)*/;
	static final public String IMPORTER_LISTERNER_4 = /*$$(*/ "The line cannot be imported (import : %s)" /*)*/;
	
	/**
	 * Template message service base impl
	 */
	static final public String TEMPLATE_MESSAGE_BASE_1 = /*$$(*/ "%s : Path to Birt template is incorrect" /*)*/;
	static final public String TEMPLATE_MESSAGE_BASE_2 = /*$$(*/ "Unable to generate Birt report file" /*)*/;

	
	/**
	 * Querie Service and controller
	 */
	static final public String QUERIE_1 = /*$$(*/ "Error : There is no query set for the querie %s" /*)*/;
	static final public String QUERIE_2 = /*$$(*/ "Error : Incorrect query for the querie %s" /*)*/;
	static final public String QUERIE_3 = /*$$(*/ "Valid query." /*)*/;
	
	/**
	 * Scheduler service
	 */
	static final public String SCHEDULER_1 = /*$$(*/ "Please enter a periodicity for the scheduler %s" /*)*/;
	
	/**
	 * Tax service
	 */
	static final public String TAX_1 = /*$$(*/ "Please enter a tax version for the tax %s" /*)*/;
	static final public String TAX_2 = /*$$(*/ "Tax is missing" /*)*/;
	
	/**
	 * Template rule service
	 */
	static final public String TEMPLATE_RULE_1 = /*$$(*/ "Bean is not an instance of " /*)*/;
	
	/**
	 * Address controller
	 */
	static final public String ADDRESS_1 = /*$$(*/ "OK" /*)*/;
	static final public String ADDRESS_2 = /*$$(*/ "Service unavailable, please contact a administrator" /*)*/;
	static final public String ADDRESS_3 = /*$$(*/ "There is no matching address in the QAS base" /*)*/;
	static final public String ADDRESS_4 = /*$$(*/ "NA" /*)*/;
	static final public String ADDRESS_5 = /*$$(*/ "<B>%s</B> not found" /*)*/;
	static final public String ADDRESS_6 = /*$$(*/ "Feature currently not available with Open Street Maps." /*)*/;
	static final public String ADDRESS_7 = /*$$(*/ "Current user's partner delivery address not set" /*)*/;
	
	/**
	 * Bank details controller
	 */
	static final public String BANK_DETAILS_1 = /*$$(*/ "The entered IBAN code is not valid . <br> Either the code doesn't respect the norm, or the format you have entered is not correct. It has to be without any blank space, as the following : <br> FR0000000000000000000000000" /*)*/;
	static final public String BANK_DETAILS_2 = /*$$(*/ "At least one iban code you have entered for this partner is not valid. Here is the list of invalid codes : %s" /*)*/;
	
	/**
	 * General controller
	 */
	static final public String GENERAL_1 = /*$$(*/ "No duplicate records found" /*)*/;
	static final public String GENERAL_2 = /*$$(*/ "Duplicate records" /*)*/;
	static final public String GENERAL_3 = /*$$(*/ "Please select key fields to check duplicate" /*)*/;
	static final public String GENERAL_4 = /*$$(*/ "Attachment directory OR Application source does not exist" /*)*/;
	static final public String GENERAL_5 = /*$$(*/ "Export Object" /*)*/;
	static final public String GENERAL_6 = /*$$(*/ "Connection successful" /*)*/;
	static final public String GENERAL_7 = /*$$(*/ "Error in Connection" /*)*/;
	
	/**
	 * Messsage controller 
	 */
	static final public String MESSAGE_1 = /*$$(*/ "Error in print. Please check report configuration and print setting." /*)*/;
	static final public String MESSAGE_2 = /*$$(*/ "Please select the Message(s) to print." /*)*/;
	
	/**
	 * Partner controller
	 */
	static final public String PARTNER_1 = /*$$(*/ "There is no sequence set for the partners" /*)*/;
	
	/**
	 * Product controller
	 */
	static final public String PRODUCT_1 = /*$$(*/ "Variants generated" /*)*/;
	static final public String PRODUCT_2 = /*$$(*/ "Prices updated" /*)*/;
}
