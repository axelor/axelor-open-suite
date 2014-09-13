/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.filter;

import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.Filter;
import com.axelor.apps.base.db.FilterList;
import com.axelor.apps.base.db.repo.FilterListRepository;
import com.axelor.apps.base.db.repo.FilterRepository;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class FilterService extends FilterRepository{
	
	@Inject
	private FilterListRepository filterListRepo;
	
	@Transactional
	public void createFilter(String code) {
		Filter filter = new Filter(code);
		save(filter);
	}
	
	@SuppressWarnings("rawtypes")
	public void copyFilter(String code, Filter otherFilter) {
		
		final Filter filter = new Filter(code);
		
		JPA.runInTransaction(new Runnable() {
			
			@Override
			public void run() {
				save(filter);
			}
		});
		
		final List<Map> list = filterListRepo.all().filter("self.filterCode = ?1", otherFilter.getCode()).select("recordId","redordModel").fetch(0, 0);
		
		JPA.runInTransaction(new Runnable() {
			@Override
			public void run() {
				for (Map map : list) {
					FilterList list = new FilterList();
					list.setRecordId(Long.valueOf(map.get("recordId").toString()));
					list.setRecordModel(map.get("redordModel").toString());
					list.setFilterCode(filter.getCode());
					list.setFilterId(filter.getId());
					filterListRepo.save(list);
				}
			}
		});
	}

}
