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
package com.axelor.apps.crm.service;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.crm.db.CalendarConfiguration;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.google.inject.persist.Transactional;

public class CalendarConfigurationService {
	
	private static final Logger LOG = LoggerFactory.getLogger(CalendarConfigurationService.class);
	
	private final String NAME = "crm-root-event-calendar-";
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void createEntryMenu(CalendarConfiguration calendarConfiguration)  {
		
		String menuName = NAME+calendarConfiguration.getId();
		String title = calendarConfiguration.getName();
		
		MetaAction metaAction = this.createMetaAction(menuName.replaceAll("-", "."), title);
		MetaMenu metaMenu = this.createMetaMenu(menuName, title, calendarConfiguration.getCalendarGroup(), calendarConfiguration.getCalendarUser(), metaAction);
		metaMenu.save();
		
		calendarConfiguration.setMetaAction(metaAction);
		calendarConfiguration.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void deleteEntryMenu(CalendarConfiguration calendarConfiguration)  {

		calendarConfiguration.setMetaAction(null);
		
		String menuName = NAME+calendarConfiguration.getId();
		
		MetaMenu metaMenu = MetaMenu.all().filter("self.name = ",menuName).fetchOne();
		
		metaMenu.remove();
		
		MetaAction metaAction = MetaAction.all().filter("self.name = ", menuName.replaceAll("-", ".")).fetchOne();
		
		metaAction.remove();
		
		
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
		
		
		String xml = "<action-view title=\""+title+"\" icon=\"img/icons/calendar-task.png\" model=\"com.axelor.apps.crm.db.Event\" name=\""+name+"\"> "+
    "<view type=\"calendar\" name=\"event-calendar\"/>" +
    "<view type=\"grid\" name=\"event-grid\"/>" +
    "<view type=\"form\" name=\"event-form\"/>" +
    "<context name=\"_typeSelect\" expr=\"2\"/>" +
    "</action-view>";
		
		metaAction.setXml(xml);
		
		return metaAction;
		
	}
	
	
	
	
}
