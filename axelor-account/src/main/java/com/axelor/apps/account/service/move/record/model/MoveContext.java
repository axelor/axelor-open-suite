/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move.record.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class MoveContext {

  protected final Map<String, Object> valuesContext;
  protected final Map<String, Map<String, Object>> attrsContext;
  protected StringJoiner flashContext = new StringJoiner("\n");
  protected StringJoiner alertContext = new StringJoiner("\n");
  protected StringJoiner notifyContext = new StringJoiner("\n");
  protected StringJoiner errorContext = new StringJoiner("\n");

  public MoveContext() {
    this.valuesContext = new HashMap<>();
    this.attrsContext = new HashMap<>();
  }

  public void putInValues(Map<String, Object> map) {
    Objects.requireNonNull(map);

    valuesContext.putAll(map);
  }

  public void putInValues(String key, Object value) {
    this.valuesContext.put(key, value);
  }

  public void putInAttrs(Map<String, Map<String, Object>> map) {
    Objects.requireNonNull(map);

    attrsContext.putAll(map);
  }

  public void putInAttrs(String fieldName, String attributeName, Object attributeValue) {
    this.attrsContext.putIfAbsent(fieldName, new HashMap<>());
    this.attrsContext.get(fieldName).put(attributeName, attributeValue);
  }

  public Map<String, Object> getValues() {
    return this.valuesContext;
  }

  public Map<String, Map<String, Object>> getAttrs() {
    return this.attrsContext;
  }

  public void putInFlash(String value) {
    this.flashContext.add(value);
  }

  public String getFlash() {
    return flashContext.toString();
  }

  public void putInAlert(String value) {
    this.alertContext.add(value);
  }

  public String getAlert() {
    return alertContext.toString();
  }

  public void putInNotify(String value) {
    this.notifyContext.add(value);
  }

  public String getNotify() {
    return notifyContext.toString();
  }

  public void putInError(String value) {
    this.errorContext.add(value);
  }

  public String getError() {
    return errorContext.toString();
  }

  public void merge(MoveContext moveContext) {
    if (moveContext != null) {
      if (!moveContext.getAttrs().isEmpty()) {
        this.attrsContext.putAll(moveContext.getAttrs());
      }
      if (!moveContext.getValues().isEmpty()) {
        this.valuesContext.putAll(moveContext.getValues());
      }
      if (!moveContext.getFlash().isEmpty()) {
        this.flashContext.add(moveContext.getFlash());
      }
      if (!moveContext.getAlert().isEmpty()) {
        this.alertContext.add(moveContext.getAlert());
      }
      if (!moveContext.getError().isEmpty()) {
        this.errorContext.add(moveContext.getError());
      }
    }
  }
}
