
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour EngineIntensityType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="EngineIntensityType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Exact"/>
 *     &lt;enumeration value="Close"/>
 *     &lt;enumeration value="Extensive"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EngineIntensityType")
@XmlEnum
public enum EngineIntensityType {

    @XmlEnumValue("Exact")
    EXACT("Exact"),
    @XmlEnumValue("Close")
    CLOSE("Close"),
    @XmlEnumValue("Extensive")
    EXTENSIVE("Extensive");
    private final String value;

    EngineIntensityType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EngineIntensityType fromValue(String v) {
        for (EngineIntensityType c: EngineIntensityType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
