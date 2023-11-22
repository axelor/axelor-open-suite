package com.axelor.apps.production.service;

import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.db.Model;

/** Generic interface to manage getter and setter origins of several type */
public interface ProductReservationServiceProduction {
  /**
   * Call "newProductReservation.setOriginXXX(originInstanceModel)" in its right field based on
   * originInstanceModel real class
   */
  void setOrigin(ProductReservation newProductReservation, Model originInstanceModel);
  /**
   * Call "newProductReservation.getOriginXXX()" in its right field based on originInstanceModel
   * real class
   */
  Model getOrigin(ProductReservation productReservationToSave, Model originInstanceModel);
}
