
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TotalNumberInvoices" type="{http://www.w3.org/2001/XMLSchema}positiveInteger"/>
 *         &lt;element name="TotalAmount" type="{}Decimal2FractionDigitsType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "totalNumberInvoices",
    "totalAmount"
})
@XmlRootElement(name = "Footer")
public class Footer {

    @XmlElement(name = "TotalNumberInvoices", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger totalNumberInvoices;
    @XmlElement(name = "TotalAmount", required = true)
    protected BigDecimal totalAmount;

    /**
     * Gets the value of the totalNumberInvoices property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTotalNumberInvoices() {
        return totalNumberInvoices;
    }

    /**
     * Sets the value of the totalNumberInvoices property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTotalNumberInvoices(BigInteger value) {
        this.totalNumberInvoices = value;
    }

    /**
     * Gets the value of the totalAmount property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * Sets the value of the totalAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setTotalAmount(BigDecimal value) {
        this.totalAmount = value;
    }

}
