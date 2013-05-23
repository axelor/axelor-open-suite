package com.axelor.apps.base.service.formula.loader;

import groovy.lang.GroovyClassLoader;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.formula.Condition1Lvl;
import com.axelor.apps.base.service.formula.Condition2Lvl;

@Singleton
public final class Loader extends GroovyClassLoader {
	
	private static final Logger LOG = LoggerFactory.getLogger(Loader.class);
	
	private static volatile Loader instance = null;
	
	/**
	 * Constructeur.
	 * Initialise l'instance pour les formules de prix unitaires d'affichage.
	 */
	@Inject
	private Loader(){

		LOG.debug("Création du chargeur de formule");
		
	}
	
	/**
	 * Méthode d'accès au singleton
	 * @return DisplayFormulaCall
	 */
	private static Loader get(){
		
		if(instance == null){
			synchronized(Loader.class){
				
				if(instance == null) { instance = new Loader(); }
				
			}
		}
		
		return instance;
		
	}
	
	public static Loader loader(){
		
		return get();
		
	}
	
	public void removeClassCache(Class<?> klass){
		
		removeClassCacheEntry(klass.getName());
		
	}

	public static Class<?> loaderFormula(String code) throws InstantiationException, IllegalAccessException {
		
		return loader().parseClass(code);
		
	}

	@SuppressWarnings("unchecked")
	public static <P> Condition1Lvl<P> loaderCondition1Lvl(String code) throws InstantiationException, IllegalAccessException{
		
		Class<?> classLoader = loader().parseClass(code);
		return (Condition1Lvl<P>) classLoader.newInstance();
		
	}

	@SuppressWarnings("unchecked")
	public static <P1, P2> Condition2Lvl<P1, P2> loaderCondition2Lvl(String code) throws InstantiationException, IllegalAccessException{
		
		Class<?> classLoader = loader().parseClass(code);
		return (Condition2Lvl<P1, P2>) classLoader.newInstance();
		
	}
	
}
