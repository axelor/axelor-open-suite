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
package com.axelor.apps.production.exceptions;

/**
 * Interface of Exceptions. Enum all exception of axelor-organisation.
 * 
 * @author dubaux
 * 
 */
public interface IExceptionMessage {

	
	
	/**
	 * Production order service
	 */
	
	static final String PRODUCTION_ORDER_SEQ = "Aucune séquence configurée pour les Ordres de production";
	
	
	/**
	 * Production order sale order service
	 */
	
	static final String PRODUCTION_ORDER_SALES_ORDER_NO_BOM = "Aucune nomenclature définie pour le produit %s (%s)";
	
	
	
	/**
	 * Manuf order service
	 */
	
	static final String MANUF_ORDER_SEQ = "Aucune séquence configurée pour les Ordres de fabrication";
	
}
