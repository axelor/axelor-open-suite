/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.base.service.template;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.TypedQuery;

import com.axelor.apps.base.db.TemplateContextLine;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.Strings;

public class TemplateContextLineService {

	public Object evaluate(TemplateContextLine line, Model bean) {
		
		String query = line.getQuery();
		if(Strings.isNullOrEmpty(query)) {
			return null;
		}
		
		return this.evaluate(query, bean);
	}
	
	public Object evaluate(String query, Model bean) {
		try {
			Class<?> klass = this.extractClass(query);
			StringBuilder sb = new StringBuilder(query);
			int n = 0, i = sb.indexOf("?");
			while (i > -1) {
				sb.replace(i, i + 1, "?" + (++n));
				i = sb.indexOf("?", i + 1);
			}

			TypedQuery<?> querie = JPA.em().createQuery(sb.toString(), klass);
			for (int j = 1; j <= n; j++) {
				querie.setParameter(n, bean);
			}
			List<?> list = querie.getResultList();
			if(list != null && !list.isEmpty() && list.size() == 1) {
				return list.get(0);
			}
			else {
				return list;
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private Class<?> extractClass(String query) {
		Pattern pattern = Pattern.compile("(from|FROM)(\\s*)(.+?)(\\s)");
		Matcher matcher = pattern.matcher(query);
		
		String klassName = "";
		if (matcher.find()) {
			klassName = matcher.group(3).trim();
		}
		
		MetaModel model = MetaModel.findByName(klassName);
		try {
			return Class.forName(model.getFullName());
		}
		catch(Exception ex){
		}
		return null;
	}
	
	
}
