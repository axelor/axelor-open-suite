package com.axelor.apps.tool.api;

import com.axelor.db.JpaSecurity;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.ws.rs.ForbiddenException;

public class SecurityCheck {

  protected List<Class<? extends Model>> listCreate;
  protected List<Class<? extends Model>> listRead;
  protected List<Class<? extends Model>> listWrite;
  protected List<Class<? extends Model>> listRemove;
  protected List<Class<? extends Model>> listExport;

  protected JpaSecurity jpaSecurity;

  public SecurityCheck() {
    this.listCreate = new ArrayList<>();
    this.listRead = new ArrayList<>();
    this.listWrite = new ArrayList<>();
    this.listRemove = new ArrayList<>();
    this.listExport = new ArrayList<>();
    this.jpaSecurity = Beans.get(JpaSecurity.class);
  }

  public void check() {
    Consumer<Class<? extends Model>> handlerUnauthorized =
        object -> {
          throw new ForbiddenException("You can't access " + object + " for this action.");
        };

    listCreate.stream()
        .filter(object -> !jpaSecurity.isPermitted(JpaSecurity.CAN_CREATE, object))
        .forEach(handlerUnauthorized);
    listRead.stream()
        .filter(object -> !jpaSecurity.isPermitted(JpaSecurity.CAN_READ, object))
        .forEach(handlerUnauthorized);
    listWrite.stream()
        .filter(object -> !jpaSecurity.isPermitted(JpaSecurity.CAN_WRITE, object))
        .forEach(handlerUnauthorized);
    listRemove.stream()
        .filter(object -> !jpaSecurity.isPermitted(JpaSecurity.CAN_REMOVE, object))
        .forEach(handlerUnauthorized);
    listExport.stream()
        .filter(object -> !jpaSecurity.isPermitted(JpaSecurity.CAN_EXPORT, object))
        .forEach(handlerUnauthorized);
  }

  public SecurityCheck createAccess(Class<? extends Model> object) {
    if (this.listCreate == null) {
      this.listCreate = new ArrayList<>();
    }
    this.listCreate.add(object);
    return this;
  }

  public SecurityCheck createAccess(List<Class<? extends Model>> objects) {
    this.listCreate = objects;
    return this;
  }

  public SecurityCheck readAccess(Class<? extends Model> object) {
    if (this.listRead == null) {
      this.listRead = new ArrayList<>();
    }
    this.listRead.add(object);
    return this;
  }

  public SecurityCheck readAccess(List<Class<? extends Model>> objects) {
    this.listRead = objects;
    return this;
  }

  public SecurityCheck writeAccess(Class<? extends Model> object) {
    if (this.listWrite == null) {
      this.listWrite = new ArrayList<>();
    }
    this.listWrite.add(object);
    return this;
  }

  public SecurityCheck writeAccess(List<Class<? extends Model>> objects) {
    this.listWrite = objects;
    return this;
  }

  public SecurityCheck removeAccess(Class<? extends Model> object) {
    if (this.listRemove == null) {
      this.listRemove = new ArrayList<>();
    }
    this.listRemove.add(object);
    return this;
  }

  public SecurityCheck removeAccess(List<Class<? extends Model>> objects) {
    this.listRemove = objects;
    return this;
  }

  public SecurityCheck exportAccess(Class<? extends Model> object) {
    if (this.listExport == null) {
      this.listExport = new ArrayList<>();
    }
    this.listExport.add(object);
    return this;
  }

  public SecurityCheck exportAccess(List<Class<? extends Model>> objects) {
    this.listExport = objects;
    return this;
  }
}
