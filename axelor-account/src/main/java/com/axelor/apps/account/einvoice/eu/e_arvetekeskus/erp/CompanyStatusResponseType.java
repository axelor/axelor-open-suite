
package com.axelor.apps.account.einvoice.eu.e_arvetekeskus.erp;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for CompanyStatusResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CompanyStatusResponseType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence maxOccurs="100" minOccurs="0"&gt;
 *         &lt;element name="CompanyActive" type="{http://e-arvetekeskus.eu/erp}CompanyActiveType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompanyStatusResponseType", propOrder = {
    "companyActive"
})
public class CompanyStatusResponseType {

    @XmlElement(name = "CompanyActive")
    protected List<CompanyActiveType> companyActive;

    /**
     * Gets the value of the companyActive property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the companyActive property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCompanyActive().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CompanyActiveType }
     * 
     * 
     */
    public List<CompanyActiveType> getCompanyActive() {
        if (companyActive == null) {
            companyActive = new ArrayList<CompanyActiveType>();
        }
        return this.companyActive;
    }

}
