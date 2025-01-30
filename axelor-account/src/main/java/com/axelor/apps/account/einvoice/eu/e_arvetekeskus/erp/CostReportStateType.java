
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CostReportStateType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CostReportStateType"&gt;
 *   &lt;restriction base="{http://e-arvetekeskus.eu/erp}ShortTextType"&gt;
 *     &lt;enumeration value="COST_REPORT_STATE_CREATED"/&gt;
 *     &lt;enumeration value="COST_REPORT_STATE_BEING_VERIFIED"/&gt;
 *     &lt;enumeration value="COST_REPORT_STATE_VERIFIED"/&gt;
 *     &lt;enumeration value="COST_REPORT_STATE_DECLINED"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "CostReportStateType")
@XmlEnum
public enum CostReportStateType {

    COST_REPORT_STATE_CREATED,
    COST_REPORT_STATE_BEING_VERIFIED,
    COST_REPORT_STATE_VERIFIED,
    COST_REPORT_STATE_DECLINED;

    public String value() {
        return name();
    }

    public static CostReportStateType fromValue(String v) {
        return valueOf(v);
    }

}
