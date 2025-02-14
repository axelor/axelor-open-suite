
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BuyInvoiceEventCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BuyInvoiceEventCodeType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="RECEIVED"/&gt;
 *     &lt;enumeration value="BEING_VERIFIED"/&gt;
 *     &lt;enumeration value="VERIFIED"/&gt;
 *     &lt;enumeration value="RETURNED_TO_SENDER"/&gt;
 *     &lt;enumeration value="PAYMENT_RECEIVED"/&gt;
 *     &lt;enumeration value="PAID"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "BuyInvoiceEventCodeType")
@XmlEnum
public enum BuyInvoiceEventCodeType {

    RECEIVED,
    BEING_VERIFIED,
    VERIFIED,
    RETURNED_TO_SENDER,
    PAYMENT_RECEIVED,
    PAID;

    public String value() {
        return name();
    }

    public static BuyInvoiceEventCodeType fromValue(String v) {
        return valueOf(v);
    }

}
