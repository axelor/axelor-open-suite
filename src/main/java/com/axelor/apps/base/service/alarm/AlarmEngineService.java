/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service.alarm;

import groovy.lang.Binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.AlarmEngine;
import com.axelor.apps.base.db.AlarmMessage;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.TemplateHelper;
import com.axelor.meta.service.MetaModelService;
import com.google.inject.Inject;

/**
 * Classe implémentant l'ensemble des fonctions utiles au moteur d'alarmes.
 * 
 * @author guerrier
 * @version 1.0
 *
 */
public class AlarmEngineService <T extends Model>  {

	private static final Logger LOG = LoggerFactory.getLogger(AlarmEngineService.class);
		
	private DateTime dateTime;

	@Inject
	public AlarmEngineService() {
		
		dateTime = GeneralService.getTodayDateTime();
		
	}

	public AlarmEngineService(DateTime dateTime) {
		
		this.dateTime = dateTime;
		
	}
	
	public Alarm get(String alarmEngineCode, T t, boolean isExternal){
		
		AlarmEngine alarmEngine = AlarmEngine.all().filter("self.code = ?1 AND externalOk = ?2 AND activeOk = true", alarmEngineCode, isExternal).fetchOne();
		
		if (alarmEngine != null) { return createAlarm(alarmEngine, t); }
		else return null;
		
	}
	
	/**
	 * Obtenir le tuple model cible et sa liste d'alarmes pour un type de moteur précis.
	 * 
	 * @param klass
	 * 		Le modèle cible des requête.
	 * @param params
	 * 		Liste de paramètre de la requête.
	 * 
	 * @return
	 * 		Un dictionnaire contenant l'ensemble des éléments remonté par la requête avec la liste des alarmes concernées.
	 * 
	 * @throws Exception
	 */
	public Map<T, List<Alarm>> get(Class<T> klass, T... params) {
		
		List<AlarmEngine> alarmEngines = AlarmEngine.all().filter("metaModel = ?1 AND activeOk = true AND externalOk = false", MetaModelService.getMetaModel(klass)).fetch(); 
		
		LOG.debug("Lancement des moteurs de type {} : {} moteurs à lancer", klass, alarmEngines.size());
				
		return get(alarmEngines, klass, params);
		
	}
	
	/**
	 * Obtenir le tuple model cible et sa liste d'alarmes.
	 * 
	 * @param alarmEngineLines
	 * 		Une liste d'éléments (lignes) d'un ou plusieurs moteur d'alarme.
	 * @param klass
	 * 		Le modèle cible des requête.
	 * @param inList
	 * 		Une liste d'éléments du modèle cible pré-établies limitant le champs de recherche à ces éléments.
	 * @param params
	 * 		Liste de paramètre de la requête.
	 * 
	 * @return
	 * 		Un dictionnaire contenant l'ensemble des éléments remonté par la requête avec la liste des alarmes concernées.
	 * 
	 * @throws Exception
	 */
	protected Map<T, List<Alarm>> get(List<AlarmEngine> alarmEngines, Class<T> klass, T... params) {
	
		Map<T, List<Alarm>> map = new HashMap<T, List<Alarm>>();
		Map<T, Alarm> alarmMap = new HashMap<T, Alarm>();
		
		for (AlarmEngine alarmEngine : alarmEngines){
			
			alarmMap.clear();
			alarmMap.putAll( get(alarmEngine, klass, params) );
			
			for (T t : alarmMap.keySet()){
				
				if (!map.containsKey(t)) { map.put(t, new ArrayList<Alarm>()); }
				
				map.get(t).add(alarmMap.get(t));
				
			}
			
		}
		
		return map;
		
	}
	
	/**
	 * Obtenir le tuple model cible et alarme.
	 * 
	 * @param message
	 * 		Le message à attribuer à l'alarme.
	 * @param query
	 * 		La condition de la requête (Clause WHERE).
	 * @param klass
	 * 		Le modèle cible de la requête.
	 * @param inList
	 * 		Une liste d'éléments du modèle cible pré-établies limitant le champs de recherche à ces éléments.
	 * @param params
	 * 		Liste de paramètre de la requête.
	 * 
	 * @return
	 * 		Un dictionnaire contenant l'ensemble des éléments remonté par la requête avec l'alarme concernée.
	 * 
	 * @throws Exception
	 */
	protected Map<T, Alarm> get(AlarmEngine alarmEngine, Class<T> klass, T... params) {
		
		Map<T, Alarm> map = new HashMap<T, Alarm>();
				
		for (T t : this.results(alarmEngine.getQuery(), klass, params)){
			
			if (!map.containsKey(t)) { map.put(t, createAlarm(alarmEngine, t)); }
		}
		
		LOG.debug("{} objets en alarmes", map.size());
		
		return map;
		
	}
	
	/**
	 * Lancer une requête pour un model défini.
	 * 
	 * @param query
	 * 		La condition de la requête (Clause WHERE).
	 * @param klass
	 * 		Le modèle cible de la requête.
	 * @param inList
	 * 		Une liste d'éléments du modèle cible pré-établies limitant le champs de recherche à ces éléments.
	 * @param params
	 * 		Liste de paramètre de la requête.
	 * 
	 * @return
	 * 		Liste d'élément correspondant au modèle cible.	
	 * 
	 * @throws Exception
	 */
	public List<T> results(String query, Class<T> klass, T... params) {
		
		LOG.debug("Lancement de la requête {} => Objet: {}, params: {}", new Object[]{query, klass.getSimpleName(), params});
		
		if (params != null && params.length > 0){
						
			String query2 = String.format("self in ?1 AND (%s)", query);
			
			return JPA.all(klass).filter(query2, Arrays.asList(params)).fetch();
		
		}
		
		return JPA.all(klass).filter(query).fetch();
		
	}
	
	public Alarm createAlarm(AlarmEngine alarmEngine, T t){

		Alarm alarm = new Alarm();
		
		alarm.setDate(dateTime);
		alarm.setAlarmEngine(alarmEngine);
		alarm.setContent( content(alarmEngine.getAlarmMessage(), t) );
		
		if ( alarm.getAlarmEngine().getLockingOk()) { alarm.setAcquitOk(false); }
		else { alarm.setAcquitOk(true); }
		
		return alarm;
		
	}
	
	protected String content(AlarmMessage alarmMessage, T t){
		return TemplateHelper.make(alarmMessage.getMessage(), new Binding(Mapper.toMap(t)));
	}
}