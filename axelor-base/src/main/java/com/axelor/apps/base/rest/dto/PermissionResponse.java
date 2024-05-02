package com.axelor.apps.base.rest.dto;

public class PermissionResponse {

  protected final Long permissionId;
  protected final String name;
  protected final boolean canRead;
  protected final boolean canWrite;
  protected final boolean canCreate;
  protected final boolean canRemove;

  public PermissionResponse(
      Long permissionId,
      String name,
      boolean canRead,
      boolean canWrite,
      boolean canCreate,
      boolean canRemove) {
    this.permissionId = permissionId;
    this.name = name;
    this.canRead = canRead;
    this.canWrite = canWrite;
    this.canCreate = canCreate;
    this.canRemove = canRemove;
  }

  public Long getPermissionId() {
    return permissionId;
  }

  public String getName() {
    return name;
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
