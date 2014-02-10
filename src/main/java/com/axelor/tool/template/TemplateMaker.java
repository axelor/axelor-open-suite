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
package com.axelor.tool.template;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.DateRenderer;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.NumberRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.StringRenderer;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;

import com.axelor.auth.AuthUtils;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.rpc.Resource;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class TemplateMaker {
	
	private Map<String, Object> context;
	private Map<String, Object> localContext;
	
	private String template;
	private STGroup stGroup;
	private Locale locale;
	
	public TemplateMaker(Locale locale, char delimiterStartChar, char delimiterStopChar) {
		this.locale = locale;
		this.stGroup = new STGroup(delimiterStartChar,delimiterStopChar);
		//Custom renderer
		this.stGroup.registerModelAdaptor(Model.class, new ModelFormatRenderer());
		this.stGroup.registerRenderer(LocalDate.class, new LocalDateRenderer());
		this.stGroup.registerRenderer(LocalDateTime.class, new LocalDateTimeRenderer());
		this.stGroup.registerRenderer(LocalTime.class, new LocalTimeRenderer());
		//Default renderer provide by ST
		this.stGroup.registerRenderer(String.class, new StringRenderer());
		this.stGroup.registerRenderer(Number.class, new NumberRenderer());
		this.stGroup.registerRenderer(Date.class, new DateRenderer());
	}
	
	public void setContext(Model model) {
		this.setContext(model, null, null);
	}
	
	public void setContext(Model model, String nameInContext) {
		this.setContext(model, null, nameInContext);
	}
	
	public void setContext(Model model, Map<String, Object> map) {
		this.setContext(model, map, null);
	}
	
	public void setContext(Model model, Map<String, Object> map, String nameInContext) {
		Preconditions.checkNotNull(model);
		this.context = makeContext(nameInContext, model, map);
	}
	
	private Map<String, Object> makeContext(String nameInContext, Model model, Map<String, Object> map) {
		Map<String, Object> _map = Maps.newHashMap();
		
		if(nameInContext != null) {
			_map.put(nameInContext, model);
		}
		else {
			_map.putAll(Resource.toMap(model));
		}
		
		if(map != null) {
			_map.putAll(map);
		}
		
		return _map;
	}
	
	public void setTemplate(String text) {
		this.template = text;
	}
	
	public void setTemplate(File file) throws FileNotFoundException {
		if (!file.isFile()) {
			throw new FileNotFoundException("No such template: " + file.getName());
		}
		
		String text;
		try {
			text = Files.toString(file, Charsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		
		this.setTemplate(text);
	}
	
	public void addInContext(String key, Object value) {
		if(localContext == null) {
			localContext = Maps.newHashMap();
		}
		localContext.put(key, value);
	}
	
	public void addInContext(Map<String, Object> map) {
		if(localContext == null) {
			localContext = Maps.newHashMap();
		}
		localContext.putAll(map);
	}

	public Class<?> getBeanClass(Model model) {
		return model.getClass();
	}
	
	public String make() {
		if (Strings.isNullOrEmpty(this.template)) {
			throw new IllegalArgumentException("Templating can not be empty");
		}
		
		ST st = new ST(stGroup, template);
		
		Map<String, Object> _map = Maps.newHashMap();
		if(localContext != null && !localContext.isEmpty()) {
			_map.putAll(localContext);
		}
		_map.putAll(context);
		
		//Internal context
		_map.put("__user__", AuthUtils.getUser());
		_map.put("__date__", new LocalDate());
		_map.put("__time__", new LocalTime());
		_map.put("__datetime__", new LocalDateTime());
		
		for (String key : _map.keySet()) {
			st.add(key, _map.get(key));
		}
		
		return _make(st);
	}
	
	private String _make(ST st) {
		return st.render(locale);
	}
	
	class ModelFormatRenderer implements ModelAdaptor {
		
		private Property getProperty(Class<?> beanClass, String name) {
			return Mapper.of(beanClass).getProperty(name);
		}
		
		private String getSelectionValue(Property prop, Object o, Object value) {
			if(value == null) {
				return "";
			}
			MetaSelectItem item = MetaSelectItem
					.all()
					.filter("self.select.name = ?1 AND self.value = ?2",
							prop.getSelection(), value).fetchOne();
			
			if (item != null) {
				return item.getTitle();
			}
			return value == null ? "" : value.toString();
		}

		@Override
		public Object getProperty(Interpreter interp, ST self, Object o, Object property, String propertyName) {
			Property prop = this.getProperty(o.getClass(), (String) property);
			ModelAdaptor adap = self.groupThatCreatedThisInstance.getModelAdaptor(ObjectModelAdaptor.class);
			
			if (prop == null || Strings.isNullOrEmpty(prop.getSelection())) {
				return adap.getProperty(interp, self, o, property, propertyName);
			}

			Object value = adap.getProperty(interp, self, o, property, propertyName);
			return getSelectionValue(prop, o, value);
		}
	}
	
	class LocalDateRenderer implements AttributeRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
	        if ( formatString==null ) return o.toString();
	        LocalDate ld = (LocalDate) o;
	        return ld.toString(formatString);
		}
	}
	
	class LocalDateTimeRenderer implements AttributeRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
	        if ( formatString==null ) return o.toString();
	        LocalDateTime ld = (LocalDateTime) o;
	        return ld.toString(formatString);
		}
	}
	
	class LocalTimeRenderer implements AttributeRenderer {

		@Override
		public String toString(Object o, String formatString, Locale locale) {
	        if ( formatString==null ) return o.toString();
	        LocalTime ld = (LocalTime) o;
	        return ld.toString(formatString);
		}
	}
}
