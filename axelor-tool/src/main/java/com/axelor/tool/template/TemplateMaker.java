/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaJsonRecordRepository;
import com.axelor.rpc.Context;
import com.axelor.text.StringTemplates;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

public class TemplateMaker {
  private Map<String, Object> context;
  private Map<String, Object> localContext;

  private StringTemplates st;
  private String template;
  private Locale locale;

  public TemplateMaker(Locale locale, char delimiterStartChar, char delimiterStopChar) {
    this.locale = locale;
    this.st = new StringTemplates(delimiterStartChar, delimiterStopChar).withLocale(locale);
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
      this.context = new HashMap<>();
    }
    this.context.put(nameInContext, object);
  }

  private Map<String, Object> makeContext(
      String nameInContext, Model model, Map<String, Object> map) {

    Map<String, Object> res = new HashMap<>();
    Map<String, Object> ctx =
        model instanceof MetaJsonRecord
            ? Beans.get(MetaJsonRecordRepository.class).create((MetaJsonRecord) model)
            : new Context(model.getId(), EntityHelper.getEntityClass(model));

    if (nameInContext != null) {
      res.put(nameInContext, ctx);
    } else {
      res = ctx;
    }

    if (map != null) {
      for (Map.Entry<String, Object> item : map.entrySet()) {
        res.put(item.getKey(), escapeIfString(item.getValue()));
      }
    }

    return res;
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
      text = Files.asCharSource(file, Charsets.UTF_8).read();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }

    this.setTemplate(text);
  }

  public void addInContext(String key, Object value) {
    if (localContext == null) {
      localContext = new HashMap<>();
    }
    localContext.put(key, value);
  }

  public void addInContext(Map<String, Object> map) {
    if (localContext == null) {
      localContext = new HashMap<>();
    }
    localContext.putAll(map);
  }

  public Class<?> getBeanClass(Model model) {
    return model.getClass();
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }

  public String make() {
    if (Strings.isNullOrEmpty(this.template)) {
      throw new IllegalArgumentException(I18n.get(IExceptionMessage.TEMPLATE_MAKER_2));
    }

    Map<String, Object> ctx = context == null ? new HashMap<>() : context;

    if (localContext != null) {
      for (Map.Entry<String, Object> entry : localContext.entrySet()) {
        ctx.putIfAbsent(entry.getKey(), escapeIfString(entry.getValue()));
      }
    }

    return st.fromText(template).make(ctx).render();
  }

  private Object escapeIfString(Object value) {
    return value instanceof String ? StringEscapeUtils.escapeXml11(value.toString()) : value;
  }
}
