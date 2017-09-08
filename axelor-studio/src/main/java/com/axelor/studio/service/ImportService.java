/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service;

import java.util.Map;

import javax.xml.bind.JAXBException;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ChartBuilder;
import com.axelor.studio.db.DashboardBuilder;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.ActionBuilderRepo;
import com.axelor.studio.db.repo.ChartBuilderRepository;
import com.axelor.studio.db.repo.DashboardBuilderRepository;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.service.wkf.WkfService;
import com.google.inject.Inject;

public class ImportService {
	
	@Inject
	private ChartBuilderRepository chartBuilderRepo;
	
	@Inject
	private MetaJsonModelRepository metaJsonModelRepo;
	
	@Inject
	private DashboardBuilderRepository dashboardBuilderRepo;
	
	@Inject
	private MenuBuilderRepository menuBuilderRepo;
	
	@Inject
	private ActionBuilderRepo actionBuilderRepo;
	
	@Inject
	private WkfService wkfService;
	
	public Object importMetaJsonModel(Object bean, Map<String,Object> values) {
		
		assert bean instanceof MetaJsonModel;
		
		return metaJsonModelRepo.save((MetaJsonModel) bean);
	}
	
	public Object importChartBuilder(Object bean, Map<String,Object> values) throws JAXBException, AxelorException {
		
		assert bean instanceof ChartBuilder;
		
		return chartBuilderRepo.save((ChartBuilder) bean);
	}
	
	public Object importDashboardBuilder(Object bean, Map<String,Object> values) {
		
		assert bean instanceof DashboardBuilder;
		
		return dashboardBuilderRepo.save((DashboardBuilder) bean);
	}
	
	public Object importMenuBuilder(Object bean, Map<String,Object> values) {
		
		assert bean instanceof MenuBuilder;
		
		return menuBuilderRepo.save((MenuBuilder) bean);
	}
	
	public Object importActionBuilder(Object bean, Map<String,Object> values) {
		
		assert bean instanceof ActionBuilder;
		
		return actionBuilderRepo.save((ActionBuilder) bean);
	}
	
	public Object importWkf(Object bean, Map<String,Object> values) {
		
		assert bean instanceof Wkf;
		
		Wkf wkf = (Wkf) bean;
		wkfService.process(wkf);
		
		return wkf;
	}
}
