/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import java.util.HashSet;

import com.axelor.inject.Beans;
import com.axelor.apps.crm.db.CalendarConfiguration;
import com.axelor.apps.crm.db.repo.CalendarConfigurationRepository;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class CalendarConfigurationService {
	
	private final String NAME = "crm-root-event-calendar-";
	
	@Inject
	protected CalendarConfigurationRepository calendarConfigurationRepo;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createEntryMenu(CalendarConfiguration calendarConfiguration)  {
		
		String menuName = NAME+calendarConfiguration.getId();
		String title = calendarConfiguration.getName();
		
		MetaAction metaAction = this.createMetaAction(menuName.replaceAll("-", "."), title);
		MetaMenu metaMenu = this.createMetaMenu(menuName, title, calendarConfiguration.getCalendarGroup(), calendarConfiguration.getCalendarUser(), metaAction);
		Beans.get(MetaMenuRepository.class).save(metaMenu);
		
		calendarConfiguration.setMetaAction(metaAction);
		calendarConfigurationRepo.save(calendarConfiguration);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteEntryMenu(CalendarConfiguration calendarConfiguration)  {

		calendarConfiguration.setMetaAction(null);
		
		String menuName = NAME+calendarConfiguration.getId();
		
		MetaMenuRepository metaMenuRepository = Beans.get(MetaMenuRepository.class);
		
		MetaMenu metaMenu = metaMenuRepository.findByName(menuName);
		
		metaMenuRepository.remove(metaMenu);
		
		MetaActionRepository metaActionRepository = Beans.get(MetaActionRepository.class);
		
		MetaAction metaAction = metaActionRepository.findByName(menuName.replaceAll("-", "."));
		
		metaActionRepository.remove(metaAction);
		
	}
	
	

	public MetaMenu createMetaMenu(String name, String title, Group group, User user, MetaAction metaAction)  {
		MetaMenu metaMenu = new MetaMenu();
		metaMenu.setName(name);
		metaMenu.setAction(metaAction);
		metaMenu.setGroups(new HashSet<Group>());
		metaMenu.getGroups().add(group);
		metaMenu.setModule("axelor-crm");
		metaMenu.setTitle(title);
		
		return metaMenu;
	}
	
	public MetaAction createMetaAction(String name, String title)  {
		
		MetaAction metaAction = new MetaAction();
		metaAction.setModel("com.axelor.apps.crm.db.Event");
		metaAction.setModule("axelor-crm");
		metaAction.setName(name);
		metaAction.setType("action-view");
		
		
		String xml = "<action-view title=\""+title+"\" icon=\"img/16px/calendarTask_16px.png\" model=\"com.axelor.apps.crm.db.Event\" name=\""+name+"\"> "+
    "<view type=\"calendar\" name=\"event-calendar\"/>" +
    "<view type=\"grid\" name=\"event-grid\"/>" +
    "<view type=\"form\" name=\"event-form\"/>" +
    "<context name=\"_typeSelect\" expr=\"2\"/>" +
    "</action-view>";
		
		metaAction.setXml(xml);
		
		return metaAction;
		
	}
	
	
	
	
}
