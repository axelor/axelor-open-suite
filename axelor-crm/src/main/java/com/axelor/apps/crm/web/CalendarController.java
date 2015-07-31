/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.web;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fortuna.ical4j.connector.ObjectNotFoundException;
import net.fortuna.ical4j.connector.ObjectStoreException;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ConstraintViolationException;
import net.fortuna.ical4j.model.ValidationException;

import com.axelor.apps.base.db.ImportConfiguration;

import com.axelor.apps.crm.db.Calendar;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.common.FileUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.service.MetaService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;


public class CalendarController {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Inject
	private CalendarService calendarService;
	
	public void exportCalendar(ActionRequest request, ActionResponse response) throws IOException, ParserException, ValidationException, ObjectStoreException, ObjectNotFoundException {
		
		calendarService.exportCalendar();
	}
	
	
	public void importCalendarFile(ActionRequest request, ActionResponse response) throws IOException, ParserException
	{

		ImportConfiguration imp = request.getContext().asType(ImportConfiguration.class);
		
		//File truc = new File(imp.getDataMetaFile().getFilePath());
		

		Calendar cal = calendarService.find(Long.valueOf(request.getContext().get("_id").toString()) );
		log.debug("Import for calendar ::: {}", cal.getName());
		
		//System.out.println(truc.getPath() + "  " + truc.getCanonicalPath() + "    " + truc.getParentFile() + "   " + truc.getUsableSpace());
		
		
		File data = MetaFiles.getPath( imp.getDataMetaFile() ).toFile();
		//imp.getDataMetaFile().	
		
		

		calendarService.importCalendar(cal, data);
		
		
		
	}
	
	
	public void importCalendar(ActionRequest request, ActionResponse response) throws IOException, ParserException 
	{
		Calendar cal = request.getContext().asType(Calendar.class);
		//calendarService.importCalendar(cal);
		
		response.setView(ActionView
				  .define(I18n.get(IExceptionMessage.LEAD_5))
				  .model("com.axelor.apps.base.db.ImportConfiguration")
				  .add("form", "import-calendar-form")
				  .param("popup", "reload")
				  .param("forceEdit", "true")
				  .context("_id", cal.getId().toString())
				  .map());
		
		
		
		
		/*
		ImportConfiguration leadImportConfig  = Beans.get(ImportConfigurationRepository.class).all().filter("self.bindMetaFile.fileName = ?1","import-config-lead.xml").fetchOne();
		LOG.debug("ImportConfig for lead: {}",leadImportConfig);
		if(leadImportConfig == null){
			response.setFlash(I18n.get(IExceptionMessage.LEAD_4));
		}
		else{
			response.setView(ActionView
							  .define(I18n.get(IExceptionMessage.LEAD_5))
							  .model("com.axelor.apps.base.db.ImportConfiguration")
							  .add("form", "import-configuration-form")
							  .param("popup", "reload")
							  .param("forceEdit", "true")
							  .context("_showRecord", leadImportConfig.getId().toString())
							  .map());
		}
		
		
		<action-view title="All Documents" name="dms.all" model="com.axelor.dms.db.DMSFile">
  <view type="grid" name="dms-file-grid"/>
  <view-param name="ui-template:grid" value="dms-file-list"/>
  <view-param name="search-filters" value="dms-file-filters"/>
</action-view>


== > 

<grid edit-icon="false" name="dms-file-grid" title="Documents" model="com.axelor.dms.db.DMSFile">
  <field name="typeIcon" x-type="icon"/>
  <field name="fileName"/>
  <field name="downloadIcon" x-type="icon"/>
  <field name="detailsIcon" x-type="icon"/>
  <field name="lastModified" title="Last modified" width="120" x-type="datetime"/>
  <field name="metaFile.sizeText" title="Size" width="80"/>
</grid>

		
		
		
		*/
		
		
		
		
		
		
	}
	
	public void synchronizeCalendars(ActionRequest request, ActionResponse response) throws MalformedURLException, SocketException, ObjectStoreException, ObjectNotFoundException, ConstraintViolationException {
		
		calendarService.synchronizeCalendars(null);
	}
	
	
	public void testConnect(ActionRequest request, ActionResponse response) throws Exception
	{
		Calendar cal = request.getContext().asType(Calendar.class);
		if (calendarService.testConnect(cal))
			response.setValues(cal);
		else
			response.setAlert("Login and password do not match.");
		
	}
}
