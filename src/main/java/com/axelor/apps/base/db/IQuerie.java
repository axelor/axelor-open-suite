package com.axelor.apps.base.db;

public interface IQuerie {
	/**
	 * Static querie type select
	 */

	static final int QUERY_SELECT_SQL = 1;
	static final int QUERY_SELECT_JPQL = 2;
	static final int QUERY_SELECT_FILTER = 3;
	
}
