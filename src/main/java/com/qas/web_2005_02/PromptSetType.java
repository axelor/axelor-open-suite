/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour PromptSetType.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * <p>
 * <pre>
 * &lt;simpleType name="PromptSetType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="OneLine"/>
 *     &lt;enumeration value="Default"/>
 *     &lt;enumeration value="Generic"/>
 *     &lt;enumeration value="Optimal"/>
 *     &lt;enumeration value="Alternate"/>
 *     &lt;enumeration value="Alternate2"/>
 *     &lt;enumeration value="Alternate3"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "PromptSetType")
@XmlEnum
public enum PromptSetType {

    @XmlEnumValue("OneLine")
    ONE_LINE("OneLine"),
    @XmlEnumValue("Default")
    DEFAULT("Default"),
    @XmlEnumValue("Generic")
    GENERIC("Generic"),
    @XmlEnumValue("Optimal")
    OPTIMAL("Optimal"),
    @XmlEnumValue("Alternate")
    ALTERNATE("Alternate"),
    @XmlEnumValue("Alternate2")
    ALTERNATE_2("Alternate2"),
    @XmlEnumValue("Alternate3")
    ALTERNATE_3("Alternate3");
    private final String value;

    PromptSetType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PromptSetType fromValue(String v) {
        for (PromptSetType c: PromptSetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
