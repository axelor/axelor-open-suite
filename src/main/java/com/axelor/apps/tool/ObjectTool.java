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
package com.axelor.apps.tool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ObjectTool {
	
	private static final Logger LOG = LoggerFactory.getLogger(ObjectTool.class); 
	
	/**
	 * Méthode permettant de récupéré un champ d'une classe depuis son nom
	 * @param fieldName
	 * 			Le nom d'un champ
	 * @param classGotten
	 * 			La classe portant le champ
	 * @return
	 */
	public static Field getField(String fieldName, @SuppressWarnings("rawtypes") Class classGotten)  {
		Field field = null;
		try {
			LOG.debug("Classe traitée - {}", classGotten);
			field = classGotten.getDeclaredField(fieldName);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		LOG.debug("Champ récupéré : {}", field);
		return field;
	}
	
	
	/**
	 * Methode permettant de récupéré un object enfant (d'après le nom d'un champ) depuis un object parent
	 * @param obj
	 * 			Un objet parent
	 * @param linked
	 * 			Un nom de champ
	 * @return
	 */
	public static Object getObject(Object obj, String fieldName)   {
		Method m= null;
		try {
			@SuppressWarnings("rawtypes")
			Class[] paramTypes = null;
			m = obj.getClass().getMethod("get"+StringTool.capitalizeFirstLetter(fieldName),paramTypes);
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			return null;
		}
		LOG.debug("Méthode récupéré : {}", m);
		try {
			Object[] args = null;
			obj = m.invoke(obj,args);
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
	
	
	
}
