
package com.qas.web_2005_02;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Country" type="{http://www.qas.com/web-2005-02}ISOType"/>
 *         &lt;element name="Engine" type="{http://www.qas.com/web-2005-02}EngineType" minOccurs="0"/>
 *         &lt;element name="PromptSet" type="{http://www.qas.com/web-2005-02}PromptSetType"/>
 *         &lt;element name="QAConfig" type="{http://www.qas.com/web-2005-02}QAConfigType" minOccurs="0"/>
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
    "country",
    "engine",
    "promptSet",
    "qaConfig"
})
@XmlRootElement(name = "QAGetPromptSet")
public class QAGetPromptSet {

    @XmlElement(name = "Country", required = true)
    protected String country;
    @XmlElement(name = "Engine")
    protected EngineType engine;
    @XmlElement(name = "PromptSet", required = true)
    protected PromptSetType promptSet;
    @XmlElement(name = "QAConfig")
    protected QAConfigType qaConfig;

    /**
     * Obtient la valeur de la propriété country.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCountry() {
        return country;
    }

    /**
     * Définit la valeur de la propriété country.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCountry(String value) {
        this.country = value;
    }

    /**
     * Obtient la valeur de la propriété engine.
     * 
     * @return
     *     possible object is
     *     {@link EngineType }
     *     
     */
    public EngineType getEngine() {
        return engine;
    }

    /**
     * Définit la valeur de la propriété engine.
     * 
     * @param value
     *     allowed object is
     *     {@link EngineType }
     *     
     */
    public void setEngine(EngineType value) {
        this.engine = value;
    }

    /**
     * Obtient la valeur de la propriété promptSet.
     * 
     * @return
     *     possible object is
     *     {@link PromptSetType }
     *     
     */
    public PromptSetType getPromptSet() {
        return promptSet;
    }

    /**
     * Définit la valeur de la propriété promptSet.
     * 
     * @param value
     *     allowed object is
     *     {@link PromptSetType }
     *     
     */
    public void setPromptSet(PromptSetType value) {
        this.promptSet = value;
    }

    /**
     * Obtient la valeur de la propriété qaConfig.
     * 
     * @return
     *     possible object is
     *     {@link QAConfigType }
     *     
     */
    public QAConfigType getQAConfig() {
        return qaConfig;
    }

    /**
     * Définit la valeur de la propriété qaConfig.
     * 
     * @param value
     *     allowed object is
     *     {@link QAConfigType }
     *     
     */
    public void setQAConfig(QAConfigType value) {
        this.qaConfig = value;
    }

}
