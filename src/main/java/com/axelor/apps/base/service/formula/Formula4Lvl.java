package com.axelor.apps.base.service.formula;

/**
 * Interface pour les formules pour 4 niveaux de paramètrages.
 * 
 * @author guerrier
 *
 * @param <R> Type retourné par la fonction compute
 * @param <K> Clef permettant d'appeller la bonne formule
 * @param <P1> Paramètre 1
 * @param <P2> Paramètre 2
 * @param <P3> Paramètre 3
 * @param <P3> Paramètre 4
 */
public interface Formula4Lvl<R, P1, P2, P3, P4> extends Formula3Lvl<R, P1, P2, P3> {
	
	R compute (String key, P1 parameter1, P2 parameter2, P3 parameter3, P4 parameter4);
	
}
