package com.axelor.exception;

/**
 * Exception spécifique Axelor. Exception soulever pour des anomalies contrôlées dans GIE.
 * 
 * @author guerrier
 * @version 1.0
 *
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
