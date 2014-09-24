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
package com.axelor.apps.base.exceptions;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	
	
	/**
	 * Currency service
	 */
	
	static final String CURRENCY_1 = /*$$(*/ "Aucune conversion trouvée de la devise '%s' à la devise '%s' à la date du %s" /*)*/ ;
	static final String CURRENCY_2 = /*$$(*/ "Le taux de conversion de la devise '%s' à la devise '%s' à la date du %s doit être différent de zéro" /*)*/ ;
	
	

	/**
	 * Unit conversion service
	 */
	
	static final String UNIT_CONVERSION_1 = /*$$(*/ "Veuillez configurer les conversions d'unités de '%s' à '%s'." /*)*/ ;
	static final String UNIT_CONVERSION_2 = /*$$(*/ "Veuillez configurer les conversions d'unités." /*)*/ ;
	
	static final String CURRENCY_CONVERSION_1 = /*$$(*/ "WARNING : Please close the current conversion period before creating new one" /*)*/ ;
	static final String CURRENCY_CONVERSION_2 = /*$$(*/ "WARNING : To Date must be after or equals to From Date" /*)*/ ;
	
	
	/**
	 * Account management service
	 */
	
	public  String ACCOUNT_MANAGEMENT_1 = /*$$(*/ "Tax configuration is missing for Product: %s (company: %s)" /*)*/ ;
	
}
