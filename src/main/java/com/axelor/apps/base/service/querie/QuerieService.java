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
