/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.qas.web_2005_02;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Classe Java pour QALicensedSet complex type.
 *
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 *
 * <pre>
 * &lt;complexType name="QALicensedSet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Copyright" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Version" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="BaseCountry" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Status" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Server" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="WarningLevel" type="{http://www.qas.com/web-2005-02}LicenceWarningLevel"/>
 *         &lt;element name="DaysLeft" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="DataDaysLeft" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *         &lt;element name="LicenceDaysLeft" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
  name = "QALicensedSet",
  propOrder = {
    "id",
    "description",
    "copyright",
    "version",
    "baseCountry",
    "status",
    "server",
    "warningLevel",
    "daysLeft",
    "dataDaysLeft",
    "licenceDaysLeft"
  }
)
public class QALicensedSet {

  @XmlElement(name = "ID", required = true)
  protected String id;

  @XmlElement(name = "Description", required = true)
  protected String description;

  @XmlElement(name = "Copyright", required = true)
  protected String copyright;

  @XmlElement(name = "Version", required = true)
  protected String version;

  @XmlElement(name = "BaseCountry", required = true)
  protected String baseCountry;

  @XmlElement(name = "Status", required = true)
  protected String status;

  @XmlElement(name = "Server", required = true)
  protected String server;

  @XmlElement(name = "WarningLevel", required = true)
  protected LicenceWarningLevel warningLevel;

  @XmlElement(name = "DaysLeft", required = true)
  @XmlSchemaType(name = "nonNegativeInteger")
  protected BigInteger daysLeft;

  @XmlElement(name = "DataDaysLeft", required = true)
  @XmlSchemaType(name = "nonNegativeInteger")
  protected BigInteger dataDaysLeft;

  @XmlElement(name = "LicenceDaysLeft", required = true)
  @XmlSchemaType(name = "nonNegativeInteger")
  protected BigInteger licenceDaysLeft;

  /**
   * Obtient la valeur de la propriété id.
   *
   * @return possible object is {@link String }
   */
  public String getID() {
    return id;
  }

  /**
   * Définit la valeur de la propriété id.
   *
   * @param value allowed object is {@link String }
   */
  public void setID(String value) {
    this.id = value;
  }

  /**
   * Obtient la valeur de la propriété description.
   *
   * @return possible object is {@link String }
   */
  public String getDescription() {
    return description;
  }

  /**
   * Définit la valeur de la propriété description.
   *
   * @param value allowed object is {@link String }
   */
  public void setDescription(String value) {
    this.description = value;
  }

  /**
   * Obtient la valeur de la propriété copyright.
   *
   * @return possible object is {@link String }
   */
  public String getCopyright() {
    return copyright;
  }

  /**
   * Définit la valeur de la propriété copyright.
   *
   * @param value allowed object is {@link String }
   */
  public void setCopyright(String value) {
    this.copyright = value;
  }

  /**
   * Obtient la valeur de la propriété version.
   *
   * @return possible object is {@link String }
   */
  public String getVersion() {
    return version;
  }

  /**
   * Définit la valeur de la propriété version.
   *
   * @param value allowed object is {@link String }
   */
  public void setVersion(String value) {
    this.version = value;
  }

  /**
   * Obtient la valeur de la propriété baseCountry.
   *
   * @return possible object is {@link String }
   */
  public String getBaseCountry() {
    return baseCountry;
  }

  /**
   * Définit la valeur de la propriété baseCountry.
   *
   * @param value allowed object is {@link String }
   */
  public void setBaseCountry(String value) {
    this.baseCountry = value;
  }

  /**
   * Obtient la valeur de la propriété status.
   *
   * @return possible object is {@link String }
   */
  public String getStatus() {
    return status;
  }

  /**
   * Définit la valeur de la propriété status.
   *
   * @param value allowed object is {@link String }
   */
  public void setStatus(String value) {
    this.status = value;
  }

  /**
   * Obtient la valeur de la propriété server.
   *
   * @return possible object is {@link String }
   */
  public String getServer() {
    return server;
  }

  /**
   * Définit la valeur de la propriété server.
   *
   * @param value allowed object is {@link String }
   */
  public void setServer(String value) {
    this.server = value;
  }

  /**
   * Obtient la valeur de la propriété warningLevel.
   *
   * @return possible object is {@link LicenceWarningLevel }
   */
  public LicenceWarningLevel getWarningLevel() {
    return warningLevel;
  }

  /**
   * Définit la valeur de la propriété warningLevel.
   *
   * @param value allowed object is {@link LicenceWarningLevel }
   */
  public void setWarningLevel(LicenceWarningLevel value) {
    this.warningLevel = value;
  }

  /**
   * Obtient la valeur de la propriété daysLeft.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getDaysLeft() {
    return daysLeft;
  }

  /**
   * Définit la valeur de la propriété daysLeft.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setDaysLeft(BigInteger value) {
    this.daysLeft = value;
  }

  /**
   * Obtient la valeur de la propriété dataDaysLeft.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getDataDaysLeft() {
    return dataDaysLeft;
  }

  /**
   * Définit la valeur de la propriété dataDaysLeft.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setDataDaysLeft(BigInteger value) {
    this.dataDaysLeft = value;
  }

  /**
   * Obtient la valeur de la propriété licenceDaysLeft.
   *
   * @return possible object is {@link BigInteger }
   */
  public BigInteger getLicenceDaysLeft() {
    return licenceDaysLeft;
  }

  /**
   * Définit la valeur de la propriété licenceDaysLeft.
   *
   * @param value allowed object is {@link BigInteger }
   */
  public void setLicenceDaysLeft(BigInteger value) {
    this.licenceDaysLeft = value;
  }
}
