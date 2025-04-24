package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for InvoiceFormatType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="InvoiceFormatType"&gt;
 *   &lt;restriction base="{http://www.pangaliit.ee/arveldused/e-arve/}ShortTextType"&gt;
 *     &lt;enumeration value="AXAPTA"/&gt;
 *     &lt;enumeration value="E_INV_1_1"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "InvoiceFormatType")
@XmlEnum
public enum InvoiceFormatType {
  AXAPTA,
  E_INV_1_1;

  public String value() {
    return name();
  }

  public static InvoiceFormatType fromValue(String v) {
    return valueOf(v);
  }
}
