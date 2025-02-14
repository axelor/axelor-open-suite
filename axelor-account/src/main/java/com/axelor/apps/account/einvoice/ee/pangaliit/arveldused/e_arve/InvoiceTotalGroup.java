
package com.axelor.apps.account.einvoice.ee.pangaliit.arveldused.e_arve;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element ref="{}ItemEntry" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}GroupEntry" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="groupId" type="{}ShortTextType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "itemEntry",
    "groupEntry"
})
@XmlRootElement(name = "InvoiceTotalGroup")
public class InvoiceTotalGroup {

    @XmlElement(name = "ItemEntry")
    protected List<ItemEntry> itemEntry;
    @XmlElement(name = "GroupEntry")
    protected GroupEntry groupEntry;
    @XmlAttribute(name = "groupId")
    protected String groupId;

    /**
     * Gets the value of the itemEntry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the itemEntry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItemEntry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ItemEntry }
     * 
     * 
     */
    public List<ItemEntry> getItemEntry() {
        if (itemEntry == null) {
            itemEntry = new ArrayList<ItemEntry>();
        }
        return this.itemEntry;
    }

    /**
     * Gets the value of the groupEntry property.
     * 
     * @return
     *     possible object is
     *     {@link GroupEntry }
     *     
     */
    public GroupEntry getGroupEntry() {
        return groupEntry;
    }

    /**
     * Sets the value of the groupEntry property.
     * 
     * @param value
     *     allowed object is
     *     {@link GroupEntry }
     *     
     */
    public void setGroupEntry(GroupEntry value) {
        this.groupEntry = value;
    }

    /**
     * Gets the value of the groupId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the value of the groupId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroupId(String value) {
        this.groupId = value;
    }

}
