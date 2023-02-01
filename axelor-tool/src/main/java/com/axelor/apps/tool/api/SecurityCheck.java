/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.api;

import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.ws.rs.ForbiddenException;

public class SecurityCheck {

  protected List<Class<? extends Model>> listCreate;
  protected Map<Class<? extends Model>, List<Long>> mapRead;
  protected Map<Class<? extends Model>, List<Long>> mapWrite;
  protected Map<Class<? extends Model>, List<Long>> mapRemove;
  protected Map<Class<? extends Model>, List<Long>> mapExport;

  protected JpaSecurity jpaSecurity;

  public SecurityCheck() {
    this.listCreate = new ArrayList<>();
    this.mapRead = new HashMap<>();
    this.mapWrite = new HashMap<>();
    this.mapRemove = new HashMap<>();
    this.mapExport = new HashMap<>();
    this.jpaSecurity = Beans.get(JpaSecurity.class);
  }

  public void check() {
    Consumer<? super Map.Entry<Class<? extends Model>, List<Long>>> handlerUnauthorized =
        entry -> {
          throw new ForbiddenException("You can't access " + entry.getKey() + " for this action.");
        };

    Consumer<Class<? extends Model>> handlerUnauthorizedForCreateAccess =
        klass -> {
          throw new ForbiddenException("You can't access " + klass + " for this action.");
        };

    listCreate.stream()
        .filter(klass -> !jpaSecurity.isPermitted(JpaSecurity.CAN_CREATE, klass))
        .forEach(handlerUnauthorizedForCreateAccess);
    mapRead.entrySet().stream()
        .filter(
            entry ->
                !jpaSecurity.isPermitted(
                    JpaSecurity.CAN_READ, entry.getKey(), entry.getValue().toArray(new Long[0])))
        .forEach(handlerUnauthorized);
    mapWrite.entrySet().stream()
        .filter(
            entry ->
                !jpaSecurity.isPermitted(
                    JpaSecurity.CAN_WRITE, entry.getKey(), entry.getValue().toArray(new Long[0])))
        .forEach(handlerUnauthorized);
    mapRemove.entrySet().stream()
        .filter(
            entry ->
                !jpaSecurity.isPermitted(
                    JpaSecurity.CAN_REMOVE, entry.getKey(), entry.getValue().toArray(new Long[0])))
        .forEach(handlerUnauthorized);
    mapExport.entrySet().stream()
        .filter(
            entry ->
                !jpaSecurity.isPermitted(
                    JpaSecurity.CAN_EXPORT, entry.getKey(), entry.getValue().toArray(new Long[0])))
        .forEach(handlerUnauthorized);
  }

  public SecurityCheck createAccess(Class<? extends Model> klass) {
    if (this.listCreate == null) {
      this.listCreate = new ArrayList<>();
    }
    this.listCreate.add(klass);
    return this;
  }

  public SecurityCheck createAccess(List<Class<? extends Model>> klassList) {
    if (this.listCreate == null) {
      this.listCreate = new ArrayList<>();
    }
    this.listCreate.addAll(klassList);
    return this;
  }

  public SecurityCheck readAccess(Class<? extends Model> klass, Long... ids) {
    if (this.mapRead == null) {
      this.mapRead = new HashMap<>();
    }
    this.mapRead.put(klass, Arrays.asList(ids));
    return this;
  }

  public SecurityCheck readAccess(List<Class<? extends Model>> klassList) {
    if (this.mapRead == null) {
      this.mapRead = new HashMap<>();
    }
    for (Class<? extends Model> klass : klassList) {
      this.mapRead.put(klass, new ArrayList<>());
    }
    return this;
  }

  public SecurityCheck writeAccess(Class<? extends Model> klass, Long... ids) {
    if (this.mapWrite == null) {
      this.mapWrite = new HashMap<>();
    }
    this.mapWrite.put(klass, Arrays.asList(ids));
    return this;
  }

  public SecurityCheck writeAccess(List<Class<? extends Model>> klassList) {
    if (this.mapWrite == null) {
      this.mapWrite = new HashMap<>();
    }
    for (Class<? extends Model> klass : klassList) {
      this.mapWrite.put(klass, new ArrayList<>());
    }
    return this;
  }

  public SecurityCheck removeAccess(Class<? extends Model> klass, Long... ids) {
    if (this.mapRemove == null) {
      this.mapRemove = new HashMap<>();
    }
    this.mapRemove.put(klass, Arrays.asList(ids));
    return this;
  }

  public SecurityCheck removeAccess(List<Class<? extends Model>> klassList) {
    if (this.mapRemove == null) {
      this.mapRemove = new HashMap<>();
    }
    for (Class<? extends Model> klass : klassList) {
      this.mapRemove.put(klass, new ArrayList<>());
    }
    return this;
  }

  public SecurityCheck exportAccess(Class<? extends Model> klass, Long... ids) {
    if (this.mapExport == null) {
      this.mapExport = new HashMap<>();
    }
    this.mapExport.put(klass, Arrays.asList(ids));
    return this;
  }

  public SecurityCheck exportAccess(List<Class<? extends Model>> klassList) {
    if (this.mapExport == null) {
      this.mapExport = new HashMap<>();
    }
    for (Class<? extends Model> klass : klassList) {
      this.mapExport.put(klass, new ArrayList<>());
    }
    return this;
  }
}
