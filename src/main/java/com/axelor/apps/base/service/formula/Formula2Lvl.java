package com.axelor.apps.base.service.formula;

/**
 * Interface pour les formules pour 2 niveaux de paramètrages.
 * 
 * @author guerrier
 *
 * @param <R> Type retourné par la fonction compute
 * @param <K> Clef permettant d'appeller la bonne formule
 * @param <P1> Paramètre 1
 * @param <P2> Paramètre 2
 */
public interface Formula2Lvl<R, P1, P2> extends Formula1Lvl<R, P1> {
	
	R compute (String key, P1 parameter1, P2 parameter2);
	
}
