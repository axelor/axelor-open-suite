/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.context;

import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.rpc.Context;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FullContext extends Context {

  private Model entity;

  private PropertyChangeSupport changeListener;

  public FullContext(Model entity) {
    super(valueMap(entity), EntityHelper.getEntityClass(entity));
    this.entity = entity;
    this.changeListener = new PropertyChangeSupport(this);
  }

  private static Map<String, Object> valueMap(Model entity) {
    Map<String, Object> map = new HashMap<>();
    if (entity instanceof MetaJsonRecord) {
      map.put("jsonModel", ((MetaJsonRecord) entity).getJsonModel());
    }
    return map;
  }

  @Override
  public void addChangeListener(PropertyChangeListener listener) {
    this.changeListener.addPropertyChangeListener(listener);
  }

  @Override
  public Object getTarget() {
    return this.entity;
  }

  @Override
  protected Object validate(String name, Object value) {
    return value;
  }

  @Override
  public Object get(Object key) {
    String name = (String) key;
    Object value = super.get(name);

    if (value instanceof Model) {
      return new FullContext((Model) value);
    }

    if (value instanceof Collection) {
      Collection<?> items = (Collection<?>) value;
      if (items.isEmpty()) return value;
      if (items.stream().findFirst().get() instanceof Model) {
        items =
            items.stream()
                .map(Model.class::cast)
                .map(FullContext::new)
                .collect(Collectors.toList());
        return items;
      }
    }

    return value;
  }

  @Override
  public Object put(String name, Object value) {
    Object old = super.put(name, value);
    if (old != value) {
      changeListener.firePropertyChange(name, old, value);
    }
    return old;
  }
}
