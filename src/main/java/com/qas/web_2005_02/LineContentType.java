
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour LineContentType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="LineContentType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Address"/>
 *     &lt;enumeration value="Name"/>
 *     &lt;enumeration value="Ancillary"/>
 *     &lt;enumeration value="DataPlus"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LineContentType")
@XmlEnum
public enum LineContentType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Address")
    ADDRESS("Address"),
    @XmlEnumValue("Name")
    NAME("Name"),
    @XmlEnumValue("Ancillary")
    ANCILLARY("Ancillary"),
    @XmlEnumValue("DataPlus")
    DATA_PLUS("DataPlus");
    private final String value;

    LineContentType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LineContentType fromValue(String v) {
        for (LineContentType c: LineContentType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
