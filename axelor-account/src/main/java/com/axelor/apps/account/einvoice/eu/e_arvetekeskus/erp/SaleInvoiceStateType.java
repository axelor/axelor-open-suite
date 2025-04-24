package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for SaleInvoiceStateType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="SaleInvoiceStateType"&gt;
 *   &lt;restriction base="{http://www.pangaliit.ee/arveldused/e-arve/}ShortTextType"&gt;
 *     &lt;enumeration value="CREATED"/&gt;
 *     &lt;enumeration value="IMPORTED"/&gt;
 *     &lt;enumeration value="SENT"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "SaleInvoiceStateType")
@XmlEnum
public enum SaleInvoiceStateType {
  CREATED,
  IMPORTED,
  SENT;

  public String value() {
    return name();
  }

  public static SaleInvoiceStateType fromValue(String v) {
    return valueOf(v);
  }
}
