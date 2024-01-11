/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.alarm;

import com.axelor.apps.base.db.Alarm;
import com.axelor.apps.base.db.AlarmEngine;
import com.axelor.apps.base.db.AlarmMessage;
import com.axelor.apps.base.db.repo.AlarmEngineRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.service.MetaModelService;
import com.axelor.text.Templates;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Classe implémentant l'ensemble des fonctions utiles au moteur d'alarmes. */
public class AlarmEngineService<T extends Model> {

  protected AppBaseService appBaseService;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Templates templates;

  @Inject private AlarmEngineRepository alarmEngineRepo;

  @Inject
  public AlarmEngineService(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  public Alarm get(String alarmEngineCode, T t, boolean isExternal) {

    AlarmEngine alarmEngine =
        alarmEngineRepo
            .all()
            .filter(
                "self.code = ?1 AND externalOk = ?2 AND activeOk = true",
                alarmEngineCode,
                isExternal)
            .fetchOne();

    if (alarmEngine != null) {
      return createAlarm(alarmEngine, t);
    } else return null;
  }

  /**
   * Obtenir le tuple model cible et sa liste d'alarmes pour un type de moteur précis.
   *
   * @param klass Le modèle cible des requête.
   * @param params Liste de paramètre de la requête.
   * @return Un dictionnaire contenant l'ensemble des éléments remonté par la requête avec la liste
   *     des alarmes concernées.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public Map<T, List<Alarm>> get(Class<T> klass, T... params) {

    List<? extends AlarmEngine> alarmEngines =
        alarmEngineRepo
            .all()
            .filter(
                "metaModel = ?1 AND activeOk = true AND externalOk = false",
                MetaModelService.getMetaModel(klass))
            .fetch();

    LOG.debug("Engines launching of type {} : {} engines to launch", klass, alarmEngines.size());

    return get(alarmEngines, klass, params);
  }

  /**
   * Obtenir le tuple model cible et sa liste d'alarmes.
   *
   * @param alarmEngineLines Une liste d'éléments (lignes) d'un ou plusieurs moteur d'alarme.
   * @param klass Le modèle cible des requête.
   * @param inList Une liste d'éléments du modèle cible pré-établies limitant le champs de recherche
   *     à ces éléments.
   * @param params Liste de paramètre de la requête.
   * @return Un dictionnaire contenant l'ensemble des éléments remonté par la requête avec la liste
   *     des alarmes concernées.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  protected Map<T, List<Alarm>> get(
      List<? extends AlarmEngine> alarmEngines, Class<T> klass, T... params) {

    Map<T, List<Alarm>> map = new HashMap<T, List<Alarm>>();
    Map<T, Alarm> alarmMap = new HashMap<T, Alarm>();

    for (AlarmEngine alarmEngine : alarmEngines) {

      alarmMap.clear();
      alarmMap.putAll(get(alarmEngine, klass, params));

      for (T t : alarmMap.keySet()) {

        if (!map.containsKey(t)) {
          map.put(t, new ArrayList<Alarm>());
        }

        map.get(t).add(alarmMap.get(t));
      }
    }

    return map;
  }

  /**
   * Obtenir le tuple model cible et alarme.
   *
   * @param message Le message à attribuer à l'alarme.
   * @param query La condition de la requête (Clause WHERE).
   * @param klass Le modèle cible de la requête.
   * @param inList Une liste d'éléments du modèle cible pré-établies limitant le champs de recherche
   *     à ces éléments.
   * @param params Liste de paramètre de la requête.
   * @return Un dictionnaire contenant l'ensemble des éléments remonté par la requête avec l'alarme
   *     concernée.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  protected Map<T, Alarm> get(AlarmEngine alarmEngine, Class<T> klass, T... params) {

    Map<T, Alarm> map = new HashMap<T, Alarm>();

    for (T t : this.results(alarmEngine.getQuery(), klass, params)) {

      if (!map.containsKey(t)) {
        map.put(t, createAlarm(alarmEngine, t));
      }
    }

    LOG.debug("{} objects with alarm", map.size());

    return map;
  }

  /**
   * Lancer une requête pour un model défini.
   *
   * @param query La condition de la requête (Clause WHERE).
   * @param klass Le modèle cible de la requête.
   * @param inList Une liste d'éléments du modèle cible pré-établies limitant le champs de recherche
   *     à ces éléments.
   * @param params Liste de paramètre de la requête.
   * @return Liste d'élément correspondant au modèle cible.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<T> results(String query, Class<T> klass, T... params) {

    LOG.debug(
        "Execution of the query {} => Object: {}, params: {}",
        new Object[] {query, klass.getSimpleName(), params});

    if (params != null && params.length > 0) {

      String query2 = String.format("self in ?1 AND (%s)", query);

      return JPA.all(klass).filter(query2, Arrays.asList(params)).fetch();
    }

    return JPA.all(klass).filter(query).fetch();
  }

  public Alarm createAlarm(AlarmEngine alarmEngine, T t) {

    Alarm alarm = new Alarm();

    alarm.setDate(appBaseService.getTodayDateTime());
    alarm.setAlarmEngine(alarmEngine);
    alarm.setContent(content(alarmEngine.getAlarmMessage(), t));

    if (alarm.getAlarmEngine().getLockingOk()) {
      alarm.setAcquitOk(false);
    } else {
      alarm.setAcquitOk(true);
    }

    return alarm;
  }

  protected String content(AlarmMessage alarmMessage, T t) {
    return templates.fromText(alarmMessage.getMessage()).make(Mapper.toMap(t)).render();
  }
}
