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
					list.setRedordModel(map.get("redordModel").toString());
					list.setFilterCode(filter.getCode());
					list.setFilterId(filter.getId());
					list.save();
				}
			}
		});
	}

}
