/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.tool.template;

import com.axelor.apps.tool.exception.IExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.rpc.Resource;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.stringtemplate.v4.AttributeRenderer;
import org.stringtemplate.v4.DateRenderer;
import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.NumberRenderer;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.StringRenderer;
import org.stringtemplate.v4.misc.ObjectModelAdaptor;

public class TemplateMaker {

  private Map<String, Object> context;
  private Map<String, Object> localContext;

  private String template;
  private STGroup stGroup;
  private Locale locale;

  public TemplateMaker(Locale locale, char delimiterStartChar, char delimiterStopChar) {
    this.locale = locale;
    this.stGroup = new STGroup(delimiterStartChar, delimiterStopChar);
    // Custom renderer
    this.stGroup.registerModelAdaptor(Model.class, new ModelFormatRenderer());
    this.stGroup.registerRenderer(LocalDate.class, new LocalDateRenderer());
    this.stGroup.registerRenderer(LocalDateTime.class, new LocalDateTimeRenderer());
    this.stGroup.registerRenderer(LocalTime.class, new LocalTimeRenderer());
    // Default renderer provide by ST
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

  public void addContext(String nameInContext, Object object) {
    if (this.context == null) {
      this.context = Maps.newHashMap();
    }

    this.context.put(nameInContext, object);
  }

  private Map<String, Object> makeContext(
      String nameInContext, Model model, Map<String, Object> map) {
    Map<String, Object> _map = Maps.newHashMap();

    if (nameInContext != null) {
      _map.put(nameInContext, model);
    } else {
      _map.putAll(Resource.toMap(model));
    }

    if (map != null) {
      _map.putAll(map);
    }

    return _map;
  }

  public void setTemplate(String text) {
    this.template = text;
  }

  public void setTemplate(File file) throws FileNotFoundException {
    if (!file.isFile()) {
      throw new FileNotFoundException(
          I18n.get(IExceptionMessage.TEMPLATE_MAKER_1) + ": " + file.getName());
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
    if (localContext == null) {
      localContext = Maps.newHashMap();
    }
    localContext.put(key, value);
  }

  public void addInContext(Map<String, Object> map) {
    if (localContext == null) {
      localContext = Maps.newHashMap();
    }
    localContext.putAll(map);
  }

  public Class<?> getBeanClass(Model model) {
    return model.getClass();
  }

  public String make() {
    if (Strings.isNullOrEmpty(this.template)) {
      throw new IllegalArgumentException(I18n.get(IExceptionMessage.TEMPLATE_MAKER_2));
    }

    ST st = new ST(stGroup, template);

    Map<String, Object> _map = Maps.newHashMap();
    if (localContext != null && !localContext.isEmpty()) {
      _map.putAll(localContext);
    }
    if (context != null) {
      _map.putAll(context);
    }

    // Internal context
    _map.put("__user__", AuthUtils.getUser());
    _map.put("__date__", LocalDate.now());
    _map.put("__time__", LocalTime.now());
    _map.put("__datetime__", LocalDateTime.now());

    for (String key : _map.keySet()) {
      Object value = _map.get(key);
      if (value instanceof String) {
        value = StringEscapeUtils.escapeXml11(value.toString());
      }
      st.add(key, value);
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
      if (value == null) {
        return "";
      }
      MetaSelectItem item =
          Beans.get(MetaSelectItemRepository.class)
              .all()
              .filter("self.select.name = ?1 AND self.value = ?2", prop.getSelection(), value)
              .fetchOne();

      if (item != null) {
        return item.getTitle();
      }
      return value == null ? "" : value.toString();
    }

    @Override
    public Object getProperty(
        Interpreter interp, ST self, Object o, Object property, String propertyName) {
      Property prop = this.getProperty(o.getClass(), (String) property);
      ModelAdaptor adap =
          self.groupThatCreatedThisInstance.getModelAdaptor(ObjectModelAdaptor.class);

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
      if (formatString == null) return o.toString();
      LocalDate ld = (LocalDate) o;
      return ld.format(DateTimeFormatter.ofPattern(formatString));
    }
  }

  class LocalDateTimeRenderer implements AttributeRenderer {

    @Override
    public String toString(Object o, String formatString, Locale locale) {
      if (formatString == null) return o.toString();
      LocalDateTime ld = (LocalDateTime) o;
      return ld.format(DateTimeFormatter.ofPattern(formatString));
    }
  }

  class LocalTimeRenderer implements AttributeRenderer {

    @Override
    public String toString(Object o, String formatString, Locale locale) {
      if (formatString == null) return o.toString();
      LocalTime ld = (LocalTime) o;
      return ld.format(DateTimeFormatter.ofPattern(formatString));
    }
  }
}
