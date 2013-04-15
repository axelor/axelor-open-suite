
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour EngineEnumType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="EngineEnumType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Singleline"/>
 *     &lt;enumeration value="Typedown"/>
 *     &lt;enumeration value="Verification"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EngineEnumType")
@XmlEnum
public enum EngineEnumType {

    @XmlEnumValue("Singleline")
    SINGLELINE("Singleline"),
    @XmlEnumValue("Typedown")
    TYPEDOWN("Typedown"),
    @XmlEnumValue("Verification")
    VERIFICATION("Verification");
    private final String value;

    EngineEnumType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EngineEnumType fromValue(String v) {
        for (EngineEnumType c: EngineEnumType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
