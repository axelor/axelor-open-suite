/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.ebics.client;

/**
 * A BCS order type.
 *
 * @author Hachani
 */
public class OrderType {

  /**
   * Constructs new Order type instance
   *
   * @param orderType the order type
   */
  public OrderType(String orderType) {
    this.orderType = orderType;
  }

  /** @return the orderType */
  public String getOrderType() {
    return orderType;
  }

  /** @param orderType the orderType to set */
  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  /**
   * Returns the corresponding <code>OrderType</code> to a given string order type.
   *
   * @param orderType the given order type.
   * @return the corresponding <code>OrderType</code>
   */
  public static OrderType toOrderType(String orderType) {
    if (orderType.equals("INI")) {
      return INI;
    } else if (orderType.equals("HIA")) {
      return HIA;
    } else if (orderType.equals("HPB")) {
      return HPB;
    } else if (orderType.equals("FUL")) {
      return FUL;
    } else if (orderType.equals("FDL")) {
      return FDL;
    } else if (orderType.equals("HTD")) {
      return HTD;
    } else if (orderType.equals("HPD")) {
      return HPD;
    } else if (orderType.equals("PTK")) {
      return PTK;
    } else if (orderType.equals("SPR")) {
      return SPR;
    } else {
      throw new IllegalArgumentException("NOT SUPPORTED ORDER TYPE");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OrderType) {
      return orderType.equals(((OrderType) obj).getOrderType());
    }

    return false;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private String orderType;

  /** Send the users protocol keys. */
  public static final OrderType HIA;

  /** Fetch the banks protocol keys. */
  public static final OrderType HPB;

  /** Return bank parameters. */
  public static final OrderType HPD;

  /** Fetch user informations. */
  public static final OrderType HTD;

  /** Send the first signature key. */
  public static final OrderType INI;

  /** File upload */
  public static final OrderType FUL;

  /** File download */
  public static final OrderType FDL;

  /** Lock the channel. */
  public static final OrderType SPR;

  public static final OrderType PTK;

  static {
    HIA = new OrderType("HIA");
    HPB = new OrderType("HPB");
    HPD = new OrderType("HPD");
    HTD = new OrderType("HTD");
    INI = new OrderType("INI");
    FUL = new OrderType("FUL");
    FDL = new OrderType("FDL");
    SPR = new OrderType("SPR");
    PTK = new OrderType("PTK");
  }
}
