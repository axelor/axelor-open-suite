
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for AuthenticatedRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthenticatedRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="authPhrase" use="required" type="{http://e-arvetekeskus.eu/erp}AuthPhraseType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthenticatedRequest")
@XmlSeeAlso({
    RegistryImportRequest.class
})
public abstract class AuthenticatedRequest {

    @XmlAttribute(name = "authPhrase", required = true)
    protected String authPhrase;

    /**
     * Gets the value of the authPhrase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthPhrase() {
        return authPhrase;
    }

    /**
     * Sets the value of the authPhrase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthPhrase(String value) {
        this.authPhrase = value;
    }

}
