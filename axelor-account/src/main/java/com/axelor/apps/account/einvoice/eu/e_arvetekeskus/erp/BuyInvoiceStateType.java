
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BuyInvoiceStateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BuyInvoiceStateType"&gt;
 *   &lt;restriction base="{http://www.pangaliit.ee/arveldused/e-arve/}ShortTextType"&gt;
 *     &lt;enumeration value="RECEIVED"/&gt;
 *     &lt;enumeration value="BEING_VERIFIED"/&gt;
 *     &lt;enumeration value="VERIFIED"/&gt;
 *     &lt;enumeration value="FORPAY"/&gt;
 *     &lt;enumeration value="DECLINED"/&gt;
 *     &lt;enumeration value="RETURNED_TO_SENDER"/&gt;
 *     &lt;enumeration value="PAID"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "BuyInvoiceStateType")
@XmlEnum
public enum BuyInvoiceStateType {

    RECEIVED,
    BEING_VERIFIED,
    VERIFIED,
    FORPAY,
    DECLINED,
    RETURNED_TO_SENDER,
    PAID;

    public String value() {
        return name();
    }

    public static BuyInvoiceStateType fromValue(String v) {
        return valueOf(v);
    }

}
