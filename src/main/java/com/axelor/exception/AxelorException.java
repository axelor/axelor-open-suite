/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
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
