
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegistryFormatType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RegistryFormatType"&gt;
 *   &lt;restriction base="{http://e-arvetekeskus.eu/erp}ShortTextType"&gt;
 *     &lt;enumeration value="STANDARD"/&gt;
 *     &lt;enumeration value="AXAPTA"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "RegistryFormatType")
@XmlEnum
public enum RegistryFormatType {

    STANDARD,
    AXAPTA;

    public String value() {
        return name();
    }

    public static RegistryFormatType fromValue(String v) {
        return valueOf(v);
    }

}
