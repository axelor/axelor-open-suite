
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RejectTypeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RejectTypeType"&gt;
 *   &lt;restriction base="{http://www.pangaliit.ee/arveldused/e-arve/}NormalTextType"&gt;
 *     &lt;enumeration value="REJECT"/&gt;
 *     &lt;enumeration value="REJECT_AND_RESEND"/&gt;
 *     &lt;enumeration value="REJECT_AND_RESEND_TO_LAST"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "RejectTypeType")
@XmlEnum
public enum RejectTypeType {

    REJECT,
    REJECT_AND_RESEND,
    REJECT_AND_RESEND_TO_LAST;

    public String value() {
        return name();
    }

    public static RejectTypeType fromValue(String v) {
        return valueOf(v);
    }

}
