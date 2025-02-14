
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://e-arvetekeskus.eu/erp}SimpleResponseType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ConnectionErrors" type="{http://e-arvetekeskus.eu/erp}DimensionRegistryConnectionErrorsType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "connectionErrors"
})
@XmlRootElement(name = "DimensionRegistryResponse")
public class DimensionRegistryResponse
    extends SimpleResponseType
{

    @XmlElement(name = "ConnectionErrors")
    protected DimensionRegistryConnectionErrorsType connectionErrors;

    /**
     * Gets the value of the connectionErrors property.
     * 
     * @return
     *     possible object is
     *     {@link DimensionRegistryConnectionErrorsType }
     *     
     */
    public DimensionRegistryConnectionErrorsType getConnectionErrors() {
        return connectionErrors;
    }

    /**
     * Sets the value of the connectionErrors property.
     * 
     * @param value
     *     allowed object is
     *     {@link DimensionRegistryConnectionErrorsType }
     *     
     */
    public void setConnectionErrors(DimensionRegistryConnectionErrorsType value) {
        this.connectionErrors = value;
    }

}
