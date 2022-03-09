/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool;

import com.axelor.exception.service.TraceBackService;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ObjectTool {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Méthode permettant de récupéré un champ d'une classe depuis son nom
   *
   * @param fieldName Le nom d'un champ
   * @param classGotten La classe portant le champ
   * @return
   */
  public static Field getField(String fieldName, @SuppressWarnings("rawtypes") Class classGotten) {
    Field field = null;
    try {
      LOG.debug("Classe traitée - {}", classGotten);
      field = classGotten.getDeclaredField(fieldName);

    } catch (SecurityException e) {
      TraceBackService.trace(e);
    } catch (NoSuchFieldException e) {
      TraceBackService.trace(e);
    }
    LOG.debug("Champ récupéré : {}", field);
    return field;
  }

  /**
   * Methode permettant de récupéré un object enfant (d'après le nom d'un champ) depuis un object
   * parent
   *
   * @param obj Un objet parent
   * @param linked Un nom de champ
   * @return
   */
  public static Object getObject(Object obj, String fieldName) {
    Method m = null;
    try {
      @SuppressWarnings("rawtypes")
      Class[] paramTypes = null;
      m = obj.getClass().getMethod("get" + StringTool.capitalizeFirstLetter(fieldName), paramTypes);
    } catch (SecurityException e) {
      return null;
    } catch (NoSuchMethodException e) {
      return null;
    }
    LOG.debug("Méthode récupéré : {}", m);
    try {
      Object[] args = null;
      obj = m.invoke(obj, args);
    } catch (IllegalArgumentException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    } catch (InvocationTargetException e) {
      return null;
    }
    LOG.debug("Objet récupéré", obj);
    return obj;
  }

  /**
   * Usefull to remove all duplicates on a list. Here we can choose on which key we want to check
   * for duplicate
   *
   * <p>ex: If we want to check on ids List<Person> distinctElements = list.stream().filter(
   * distinctByKey(p -> p.getId()) ).collect( Collectors.toList() );
   *
   * @param keyExtractor Extract method use
   * @return
   */
  public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
    Map<Object, Boolean> map = new ConcurrentHashMap<>();
    return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
  }
}
