package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.QIDefault;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;

public class QIResolutionDefaultRequest extends RequestStructure {

  protected Long id;
  protected Long qiDefaultId;
  protected BigDecimal quantity;
  protected String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getQiDefaultId() {
    return qiDefaultId;
  }

  public void setQiDefaultId(Long qiDefaultId) {
    this.qiDefaultId = qiDefaultId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public QIDefault fetchQiDefault() {
    if (qiDefaultId == null || qiDefaultId == 0L) {
      return null;
    }
    return ObjectFinder.find(QIDefault.class, qiDefaultId, ObjectFinder.NO_VERSION);
  }
}
