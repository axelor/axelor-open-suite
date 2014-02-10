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
package com.axelor.apps.base.service.filter;

import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Filter;
import com.axelor.apps.base.db.FilterList;
import com.axelor.db.JPA;
import com.google.inject.persist.Transactional;

public class FilterService {
	
	@Transactional
	public void createFilter(String code) {
		Filter filter = new Filter(code);
		filter.save();
	}
	
	@SuppressWarnings("rawtypes")
	public void copyFilter(String code, Filter otherFilter) {
		
		final Filter filter = new Filter(code);
		
		JPA.runInTransaction(new Runnable() {
			
			@Override
			public void run() {
				filter.save();
			}
		});
		
		final List<Map> list = FilterList.all().filter("self.filterCode = ?1", otherFilter.getCode()).select("recordId","redordModel").fetch(0, 0);
		
		JPA.runInTransaction(new Runnable() {
			@Override
			public void run() {
				for (Map map : list) {
					FilterList list = new FilterList();
					list.setRecordId(Long.valueOf(map.get("recordId").toString()));
					list.setRecordModel(map.get("redordModel").toString());
					list.setFilterCode(filter.getCode());
					list.setFilterId(filter.getId());
					list.save();
				}
			}
		});
	}

}
