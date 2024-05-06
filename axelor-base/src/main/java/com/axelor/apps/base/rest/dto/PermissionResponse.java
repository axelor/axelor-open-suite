package com.axelor.apps.base.rest.dto;

public class PermissionResponse {

  protected final Long id;
  protected final String name;
  protected final String object;
  protected final boolean canRead;
  protected final boolean canWrite;
  protected final boolean canCreate;
  protected final boolean canRemove;

  public PermissionResponse(
      Long id,
      String name,
      String object,
      boolean canRead,
      boolean canWrite,
      boolean canCreate,
      boolean canRemove) {
    this.id = id;
    this.name = name;
    this.object = object;
    this.canRead = canRead;
    this.canWrite = canWrite;
    this.canCreate = canCreate;
    this.canRemove = canRemove;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getObject() {
    return object;
  }

  public boolean isCanRead() {
    return canRead;
  }

  public boolean isCanWrite() {
    return canWrite;
  }

  public boolean isCanCreate() {
    return canCreate;
  }

  public boolean isCanRemove() {
    return canRemove;
  }
}
