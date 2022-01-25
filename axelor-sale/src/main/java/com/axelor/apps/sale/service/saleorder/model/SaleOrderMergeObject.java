package com.axelor.apps.sale.service.saleorder.model;

public class SaleOrderMergeObject {

  private boolean canBeNull;
  private Object commonObject;
  private boolean existDiff = false;

  public SaleOrderMergeObject(Object commonObject, boolean canBeNull) {
    this.commonObject = commonObject;
    this.canBeNull = canBeNull;
  }

  public boolean isDifferent(Object object) {
    // If it can be null, then both null is not different
    if (commonObject == null && object == null) {
      return canBeNull ? false : true;
    }
    // If only one of them is null then it is different
    if (commonObject == null && object != null || commonObject != null && object == null) {
      return true;
    }
    return !commonObject.equals(object);
  }

  public Object getCommonObject() {
    return commonObject;
  }

  public void setCommonObject(Object commonObject) {
    this.commonObject = commonObject;
  }

  public boolean getExistDiff() {
    return existDiff;
  }

  public void setExistDiff(boolean existDiff) {
    this.existDiff = existDiff;
  }
}
