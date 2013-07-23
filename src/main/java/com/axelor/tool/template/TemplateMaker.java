package com.axelor.tool.template;

import groovy.lang.Binding;
import groovy.lang.MissingPropertyException;
import groovy.xml.XmlUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.TemplateHelper;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.rpc.Resource;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Files;

public class TemplateMaker {
	
	private Class<?> entity;
	private Binding binding;
	private Map<String, Object> context;
	private Map<String, Object> localContext;
	
	private String template;
	
	public TemplateMaker(String template) {
		this.setTemplate(template);
	}
	
	public TemplateMaker(File template) throws FileNotFoundException {
		this.setTemplate(template);
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
		
		this.entity = getBeanClass(model);
		this.context = makeContext(nameInContext, model, map);
	}
	
	private Map<String, Object> makeContext(String nameInContext, Model model, Map<String, Object> map) {
		Map<String, Object> _map = Maps.newHashMap();
		Map<String, Object> modelMap = Resource.toMap(model);
		
		if(nameInContext != null) {
			_map.put(nameInContext, modelMap);
		}
		else {
			_map.putAll(modelMap);
		}
		
		if(map != null) {
			_map.putAll(map);
		}
		
		return _map;
	}
	
	private void setTemplate(String text) {
		text = text.replaceAll("\\{\\{\\s*(\\w+)(\\?)?\\.([^}]*?)\\s*\\|\\s*text\\s*\\}}", "\\${__fmt__.text($1, '$3')}");
		text = text.replaceAll("\\{\\{\\s*([^}]*?)\\s*\\|\\s*text\\s*\\}}", "\\${__fmt__.text('$1')}");
		text = text.replaceAll("\\{\\{\\s*([^}]*?)\\s*\\|\\s*e\\s*\\}}", "\\${($1) ?: ''}");
		
		if (text.trim().startsWith("<?xml ")) {
			text = text.replaceAll("\\{\\{(.*?)\\}}", "\\${__fmt__.escape($1)}");
		}
		else {
			text = text.replaceAll("\\{\\{(.*?)\\}}", "\\${$1}");
		}
		
		template = text;
	}
	
	private void setTemplate(File file) throws FileNotFoundException {
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
		
		Map<String, Object> _map = Maps.newHashMap();
		if(localContext != null && !localContext.isEmpty()) {
			_map.putAll(localContext);
		}
		_map.putAll(context);
		
		binding = new Binding(_map) {
			
			@Override
			public Object getVariable(String name) {
				try {
					return super.getVariable(name);
				} catch (MissingPropertyException e) {
					if ("__date__".equals(name))
						return new LocalDate();
					if ("__time__".equals(name))
						return new LocalDateTime();
					if ("__datetime__".equals(name))
						return new DateTime();
				}
				return null;
			}
		};
		
		binding.setProperty("__fmt__", new FormatHelper());
		return _make();
	}
	
	private String _make() {
		return TemplateHelper.make(template, binding);
	}
	
	class FormatHelper {
		
		private final Logger log = LoggerFactory.getLogger(FormatHelper.class);
		
		public Object escape(Object value) {
			if (value == null) {
				return "";
			}
			return XmlUtil.escapeXml(value.toString());
		}
		
		public String text(String expr) {
			return getSelectTitle(entity, expr, binding.getProperty(expr));
		}
		
		public String text(Object bean, String expr) {
			if (bean == null) {
				return "";
			}
			expr = expr.replaceAll("\\?", "");
			return getSelectTitle(bean.getClass(), expr, getValue(bean, expr));
		}

		private String getSelectTitle(Class<?> klass, String expr, Object value) {
			if (value == null) {
				return "";
			}
			Property property = this.getProperty(klass, expr);
			if (property == null || property.getSelection() == null) {
				return value == null ? "" : value.toString();
			}
			MetaSelectItem item = MetaSelectItem
					.all()
					.filter("self.select.name = ?1 AND self.value = ?2",
							property.getSelection(), value).fetchOne();
			if (item != null) {
				return item.getTitle();
			}
			return value == null ? "" : value.toString();
		}
		
		private Property getProperty(Class<?> beanClass, String name) {
			Iterator<String> iter = Splitter.on(".").split(name).iterator();
			Property p = Mapper.of(beanClass).getProperty(iter.next());
			while(iter.hasNext() && p != null) {
				p = Mapper.of(p.getTarget()).getProperty(iter.next());
			}
			return p;
		}
		
		@SuppressWarnings("all")
		private Object getValue(Object bean, String expr) {
			if (bean == null) return null;
			Iterator<String> iter = Splitter.on(".").split(expr).iterator();
			Object obj = null;
			if (bean instanceof Map) {
				obj = ((Map) bean).get(iter.next());
			} else {
				obj = Mapper.of(bean.getClass()).get(bean, iter.next());
			}
			if(iter.hasNext() && obj != null) {
				return getValue(obj, Joiner.on(".").join(iter));
			}
			return obj;
		}

		public void info(String text,  Object... params) {
			log.info(text, params);
		}

		public void debug(String text,  Object... params) {
			log.debug(text, params);
		}

		public void error(String text,  Object... params) {
			log.error(text, params);
		}

		public void trace(String text,  Object... params) {
			log.trace(text, params);
		}
	}

}
