package com.axelor.apps.base.rest.dto;

import com.axelor.utils.api.ResponseStructure;

public class MetaPermissionRuleResponse extends ResponseStructure {

  protected final Long metaPermissionRuleId;
  protected final String field;
  protected final boolean canRead;
  protected final boolean canWrite;
  protected final String metaPermissionName;
  protected final String metaPermissionObject;

  public MetaPermissionRuleResponse(
      int version,
      Long metaPermissionRuleId,
      String field,
      boolean canRead,
      boolean canWrite,
      String metaPermissionName,
      String metaPermissionObject) {
    super(version);
    this.metaPermissionRuleId = metaPermissionRuleId;
    this.field = field;
    this.canRead = canRead;
    this.canWrite = canWrite;
    this.metaPermissionName = metaPermissionName;
    this.metaPermissionObject = metaPermissionObject;
  }

  public Long getMetaPermissionRuleId() {
    return metaPermissionRuleId;
  }

  public String getField() {
    return field;
  }

  public boolean isCanRead() {
    return canRead;
  }

  public boolean isCanWrite() {
    return canWrite;
  }

  public String getMetaPermissionName() {
    return metaPermissionName;
  }

  public String getMetaPermissionObject() {
    return metaPermissionObject;
  }
}
