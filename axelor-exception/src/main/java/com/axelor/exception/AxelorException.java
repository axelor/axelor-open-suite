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
package com.axelor.exception;

/**
 * Exception spécifique Axelor.
 */
public class AxelorException extends Exception {
	
	private static final long serialVersionUID = 1028105628735355226L;
	
	private int category;

	/**
	 * Constructeur par défaut.
	 */
	public AxelorException() {
	}

	/**
	 * Créer une exception avec son message et son type.
	 * 
	 * @param message
	 * 		Le message de l'exception
	 * @param category
	 * <ul>
	 * <li>1: Champ manquant</li>
	 * <li>2: Clef non unique</li>
	 * <li>3: Aucune valeur retournée</li>
	 * <li>4: Problème de configuration</li>
	 * <li>5: Incohérence</li>
	 * </ul>
	 */
	public AxelorException(String message, int category) {
		super(message);
		this.category = category;
	}

	/**
	 * Créer une exception avec sa cause et son type.
	 * 
	 * @param cause
	 * 		La cause de l'exception 
	 * @param category
	 * <ul>
	 * <li>1: Champ manquant</li>
	 * <li>2: Clef non unique</li>
	 * <li>3: Aucune valeur retournée</li>
	 * <li>4: Problème de configuration</li>
	 * <li>5: Incohérence</li>
	 * </ul>
	 * 
	 * @see Throwable
	 */
	public AxelorException(Throwable cause, int category) {
		super(cause);
		this.category = category;
	}

	/**
	 * Créer une exception avec son message, sa cause et son type.
	 * 
	 * @param message
	 * 		Le message de l'exception
	 * @param cause
	 * 		La cause de l'exception 
	 * @param category
	 * <ul>
	 * <li>1: Champ manquant</li>
	 * <li>2: Clef non unique</li>
	 * <li>3: Aucune valeur retournée</li>
	 * <li>4: Problème de configuration</li>
	 * <li>5: Incohérence</li>
	 * </ul>
	 * 
	 * @see Throwable
	 */
	public AxelorException(String message, Throwable cause, int category) {
		super(message, cause);
		this.category = category;
	}
	
	/**
	 * Récupérer la catégorie de l'exception
	 * 
	 * @return
	 * Un entier correspondant à l'une des catégories suivantes :
	 * <ul>
	 * <li>1: Champ manquant</li>
	 * <li>2: Clef non unique</li>
	 * <li>3: Aucune valeur retournée</li>
	 * <li>4: Problème de configuration</li>
	 * <li>5: Incohérence</li>
	 * </ul>
	 */
	public int getcategory(){
		
		return this.category;
		
	}

}
