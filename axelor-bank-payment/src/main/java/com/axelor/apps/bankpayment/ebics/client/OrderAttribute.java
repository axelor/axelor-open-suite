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

import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;

/**
 * 12.3 Order attributes The following settings are permissible for the order attribute (5 bytes
 * alphanumeric) in EBICS
 */
public class OrderAttribute {

  /**
   * Type of transmitted data O = order data and ES’s U = bank-technical ES’s D = order data and
   * transport ES (D is also used for HIA, INI, HPB)
   */
  private char transmittedDataType;
  /** O = order data and ES’s */
  public static final char TRANSMITTED_DATA_TYPE_O = 'O';
  /** U = bank-technical ES’s */
  public static final char TRANSMITTED_DATA_TYPE_U = 'U';
  /** D = order data and transport ES (D is also used for HIA, INI, HPB) */
  public static final char TRANSMITTED_DATA_TYPE_D = 'D';

  /** Compression type for order data and/or ES’s Z = ZIP compression */
  private char compressionType;

  /** Z = ZIP compression */
  public static final char COMPRESSION_TYPE_Z = 'Z';

  /** Encryption type for order data and/or ES’s N = no encryption H = hybrid process AES/RSA */
  private char encryptionType;
  /** N = no encryption */
  public static final char ENCRYPTION_TYPE_N = 'N';
  /** H = hybrid process AES/RSA */
  public static final char ENCRYPTION_TYPE_H = 'H';

  public static final char RESERVED_POSITION = 'N';

  private String orderAttributes;

  public char computeTransmittedDataType(OrderType orderType, int ebicsTypeSelect) {
    if (orderType.equals(OrderType.INI)) {
      return TRANSMITTED_DATA_TYPE_D;
    } else if (orderType.equals(OrderType.HIA)) {
      return TRANSMITTED_DATA_TYPE_D;
    } else if (orderType.equals(OrderType.HPB)) {
      return TRANSMITTED_DATA_TYPE_D;
    } else if (orderType.equals(OrderType.FUL)) {
      if (ebicsTypeSelect == EbicsPartnerRepository.EBICS_TYPE_T) {
        return TRANSMITTED_DATA_TYPE_D;
      } else if (ebicsTypeSelect == EbicsPartnerRepository.EBICS_TYPE_TS) {
        return TRANSMITTED_DATA_TYPE_O;
      }
    } else if (orderType.equals(OrderType.FDL)
        || orderType.equals(OrderType.HTD)
        || orderType.equals(OrderType.HPD)
        || orderType.equals(OrderType.PTK)) {
      return TRANSMITTED_DATA_TYPE_D;
      // "O" if the bank use financial institution’s bank-technical ES
    } else if (orderType.equals(OrderType.SPR)) {
      return TRANSMITTED_DATA_TYPE_U;
    }

    throw new IllegalArgumentException("NOT SUPPORTED ORDER TYPE OR EBICS MODE");
  }

  public char computeCompressionType() {
    return COMPRESSION_TYPE_Z;
  }

  public char computeEncryptionType(OrderType orderType) {
    if (orderType.equals(OrderType.INI)) {
      return ENCRYPTION_TYPE_N;
    } else if (orderType.equals(OrderType.HIA)) {
      return ENCRYPTION_TYPE_N;
    } else if (orderType.equals(OrderType.HPB)) {
      return ENCRYPTION_TYPE_H;
    } else if (orderType.equals(OrderType.FUL)) {
      return ENCRYPTION_TYPE_H;
    } else if (orderType.equals(OrderType.FDL)
        || orderType.equals(OrderType.HTD)
        || orderType.equals(OrderType.HPD)
        || orderType.equals(OrderType.PTK)) {
      return ENCRYPTION_TYPE_H;
    } else if (orderType.equals(OrderType.SPR)) {
      return ENCRYPTION_TYPE_H;
    }

    throw new IllegalArgumentException("NOT SUPPORTED ORDER TYPE OR EBICS MODE");
  }

  /**
   * Depending on order type and possible further marginal conditions, an EBICS client MUST enter
   * the following order attributes in the control data of the first EBICS request of an EBICS
   * transaction (ebicsRequest/header/static/OrderDetails/OrderAttribute):
   *
   * <p>Order type | Marginal conditions | Order attributes
   * --------------------------------------------------------------------------------------------
   * INI   | - | DZNNN HIA | - | DZNNN HSA | - | OZNNN HPB | - | DZHNN PUB | - | OZHNN HCA | - |
   * OZHNN HCS | - | OZHNN SPR | - | UZHNN HVE | - | UZHNN HVS | - | UZHNN other upload order types
   * | order data and ES(s) | OZHNN other upload order types | only bank-technical ES(s), no order
   * data | UZHNN other upload order types | order data with transport signature | DZHNN | (release
   * of the order via accompanying | | note instead of bank-technical ES) | other download order
   * types | download data request with financial | OZHNN | institution’s banktechnical ES | other
   * download order types | download data request without financial | DZHNN | institution’s
   * banktechnical ES |
   *
   * @param orderType
   * @param ebicsTypeSelect
   */
  public OrderAttribute(OrderType orderType, int ebicsTypeSelect) {

    this.transmittedDataType = computeTransmittedDataType(orderType, ebicsTypeSelect);
    this.compressionType = computeCompressionType();
    this.encryptionType = computeEncryptionType(orderType);
  }

  public OrderAttribute(char transmittedDataType, char compressionType, char encryptionType) {
    this.transmittedDataType = transmittedDataType;
    this.compressionType = compressionType;
    this.encryptionType = encryptionType;
  }

  public OrderAttribute build() {

    orderAttributes =
        new StringBuilder()
            .append(transmittedDataType)
            .append(compressionType)
            .append(encryptionType)
            .append(RESERVED_POSITION)
            .append(RESERVED_POSITION)
            .toString();

    return this;
  }

  /** @return the orderType */
  public String getOrderAttributes() {
    return orderAttributes;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OrderAttribute) {
      return orderAttributes.equals(((OrderAttribute) obj).getOrderAttributes());
    }

    return false;
  }
}
