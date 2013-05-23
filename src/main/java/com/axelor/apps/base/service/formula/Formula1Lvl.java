package com.axelor.apps.base.service.formula;


/**
 * Interface pour les formules pour 1 niveaux de paramètrages.
 * 
 * @author guerrier
 *
 * @param <R> Type retourné par la fonction compute
 * @param <K> Clef permettant d'appeller la bonne formule
 * @param <P1> Paramètre 1
 */
public interface Formula1Lvl<R, P> {
	
	R compute (String key, P parameter);
}
