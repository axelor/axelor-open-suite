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
package com.axelor.apps.base.service.querie;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.axelor.apps.base.db.IQuerie;
import com.axelor.apps.base.db.Querie;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.db.MetaModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class QuerieService {
	
	public List<Long> getQuerieResult(Set<Querie> querieSet) throws AxelorException {
		Set<Long> idList = Sets.newHashSet();
		
		if(querieSet != null) {
			for (Querie querie : querieSet) {
				idList.addAll(this.getQuerieResult(querie));
			}
		}
		
		return Lists.newArrayList(idList);
	}
	
	public List<Long> getQuerieResult(Querie querie) throws AxelorException {
		List<Long> result = Lists.newArrayList();
		int requestType = querie.getType();
		String filter = querie.getQuery();
	
		if(filter == null || filter.isEmpty())  {
			throw new AxelorException(String.format("Error : There is no query set for the querie %s", querie.getId()), IException.MISSING_FIELD);
		}
		
		Class<?> klass = this.getClass(querie.getMetaModel());
		try {
			if(requestType == IQuerie.QUERY_SELECT_SQL)  {
				result = this.runSqlRequest(filter);
			}
			else if(requestType == IQuerie.QUERY_SELECT_JPQL) {
				result = this.runJpqlRequest(filter,klass);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new AxelorException(String.format("Error : Incorrect query for the querie %s", querie.getId()), IException.CONFIGURATION_ERROR);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Long> runSqlRequest(String filter)  {
		List<Long> idLists = Lists.newArrayList();
		
		javax.persistence.Query query = JPA.em().createNativeQuery(filter);
		List<BigInteger> queryResult = query.getResultList();
		
		for (BigInteger bi : queryResult) {
			idLists.add(bi.longValue());
		}
		
		return idLists;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Long> runJpqlRequest(String filter, Class<?> klass)  {
		List<Long> idLists = Lists.newArrayList();
		
		List<Map> result = Query.of((Class<? extends Model>) klass).filter(filter).select("id").fetch(0, 0);
		for (Map map : result) {
			idLists.add(Long.valueOf(map.get("id").toString()));
		}
		
		return idLists;
	}
	
	private Class<?> getClass(MetaModel metaModel) {
		String model = metaModel.getFullName();

		try {
			return Class.forName(model);
		} catch (NullPointerException e) {
		} catch (ClassNotFoundException e) {
		}
		return null;
	}

	public void checkQuerie(Querie querie) throws AxelorException {
		this.getQuerieResult(querie);
	}

}
