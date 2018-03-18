/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.prestashop.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

@XmlRootElement(name="customer")
public class PrestashopCustomer extends PrestashopIdentifiableEntity {
	public static final int GENDER_MALE = 1;
	public static final int GENDER_FEMALE = 2;

	private static final List<String> READONLY_ELEMENTS = Arrays.asList(
			"last_passwd_gen"
	);

	private Integer defaultGroupId = 3; // FIXME shouldn't be hardcoded, 3 is customers
	private Integer languageId;
	private LocalDateTime newsletterSubscriptionDate;
	private String newsletterRegistrationIP;
	private String secureKey; // Prestashop innovation: this is readonly field that'll be set to null if missing… yes… really… no… nothing more to say
	private boolean deleted;
	private String password; // clear text on add, hashed on update
	private String lastname;
	private String firstname;
	private String email;
	private Integer genderId; // default is 1 for men, 2 for women. This can be changed by user but we cannot fetch them
	private LocalDate birthday;
	private boolean newsletter;
	private boolean optin;
	private String website;
	private String company;
	private String siret;
	private String ape;
	private BigDecimal allowedOutstandingAmount;
	private boolean showPublicPrices;
	private Integer riskId;
	private Integer maxPaymentDays;
	private boolean active = true;
	private String note;
	private boolean isGuest;
	private Integer shopId;
	private Integer shopGroupId;
	private LocalDateTime addDate = LocalDateTime.now();
	private LocalDateTime updateDate = LocalDateTime.now();
	private String resetPasswordToken;
	private LocalDateTime resetPasswordValidityDate;
	private Associations associations;
	private List<Element> additionalProperties = new LinkedList<>();

	@XmlElement(name="id_default_group")
	public Integer getDefaultGroupId() {
		return defaultGroupId;
	}

	public void setDefaultGroupId(Integer defaultGroupId) {
		this.defaultGroupId = defaultGroupId;
	}

	@XmlElement(name="id_lang")
	public Integer getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}

	@XmlElement(name="newsletter_date_add")
	public LocalDateTime getNewsletterSubscriptionDate() {
		return newsletterSubscriptionDate;
	}

	public void setNewsletterSubscriptionDate(LocalDateTime newsletterSubscriptionDate) {
		this.newsletterSubscriptionDate = newsletterSubscriptionDate;
	}

	@XmlElement(name="ip_registration_newsletter")
	public String getNewsletterRegistrationIP() {
		return newsletterRegistrationIP;
	}

	public void setNewsletterRegistrationIP(String newsletterRegistrationIP) {
		this.newsletterRegistrationIP = newsletterRegistrationIP;
	}

	@XmlElement(name="secure_key")
	public String getSecureKey() {
		return secureKey;
	}

	public void setSecureKey(String secureKey) {
		this.secureKey = secureKey;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@XmlElement(name="passwd")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@XmlElement(name="id_gender")
	public Integer getGenderId() {
		return genderId;
	}

	public void setGenderId(Integer genderId) {
		this.genderId = genderId;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthDate) {
		this.birthday = birthDate;
	}

	public boolean isNewsletter() {
		return newsletter;
	}

	public void setNewsletter(boolean newsletter) {
		this.newsletter = newsletter;
	}

	public boolean isOptin() {
		return optin;
	}

	public void setOptin(boolean optin) {
		this.optin = optin;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getSiret() {
		return siret;
	}

	public void setSiret(String siret) {
		this.siret = siret;
	}

	public String getApe() {
		return ape;
	}

	public void setApe(String ape) {
		this.ape = ape;
	}

	@XmlElement(name="outstanding_allow_amount")
	public BigDecimal getAllowedOutstandingAmount() {
		return allowedOutstandingAmount;
	}

	public void setAllowedOutstandingAmount(BigDecimal allowedOutstandingAmount) {
		this.allowedOutstandingAmount = allowedOutstandingAmount;
	}

	@XmlElement(name="show_public_prices")
	public boolean isShowPublicPrices() {
		return showPublicPrices;
	}

	public void setShowPublicPrices(boolean showPublicPrices) {
		this.showPublicPrices = showPublicPrices;
	}

	@XmlElement(name="id_risk")
	public Integer getRiskId() {
		return riskId;
	}

	public void setRiskId(Integer riskId) {
		this.riskId = riskId;
	}

	@XmlElement(name="max_payment_days")
	public Integer getMaxPaymentDays() {
		return maxPaymentDays;
	}

	public void setMaxPaymentDays(Integer maxPaymentDays) {
		this.maxPaymentDays = maxPaymentDays;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@XmlElement(name="is_guest")
	public boolean isGuest() {
		return isGuest;
	}

	public void setGuest(boolean isGuest) {
		this.isGuest = isGuest;
	}

	@XmlElement(name="id_shop")
	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	@XmlElement(name="id_shop_group")
	public Integer getShopGroupId() {
		return shopGroupId;
	}

	public void setShopGroupId(Integer shopGroupId) {
		this.shopGroupId = shopGroupId;
	}

	@XmlElement(name="date_add")
	public LocalDateTime getAddDate() {
		return addDate;
	}

	public void setAddDate(LocalDateTime addDate) {
		this.addDate = addDate;
	}

	@XmlElement(name="date_upd")
	public LocalDateTime getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDateTime updateDate) {
		this.updateDate = updateDate;
	}

	@XmlElement(name="reset_password_token")
	public String getResetPasswordToken() {
		return resetPasswordToken;
	}

	public void setResetPasswordToken(String resetPasswordToken) {
		this.resetPasswordToken = resetPasswordToken;
	}

	@XmlElement(name="reset_password_validity")
	public LocalDateTime getResetPasswordValidityDate() {
		return resetPasswordValidityDate;
	}

	public void setResetPasswordValidityDate(LocalDateTime resetPasswordValidityDate) {
		this.resetPasswordValidityDate = resetPasswordValidityDate;
	}

	public Associations getAssociations() {
		return associations;
	}

	public void setAssociations(Associations associations) {
		this.associations = associations;
	}

	@XmlAnyElement
	public List<Element> getAdditionalProperties() {
		for(ListIterator<Element> it = additionalProperties.listIterator() ; it.hasNext() ; ) {
			if(READONLY_ELEMENTS.contains(it.next().getTagName())) it.remove();
		}
		return additionalProperties;
	}

	public void setAdditionalProperties(List<Element> additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

	/**
	 * Helper method returning the company or firstname + lastname depending on
	 * which is defined
	 * @return A string to use as a readable name for the customer
	 */
	@XmlTransient
	public String getFullname() {
		if(StringUtils.isNotBlank(company)) return company;
		return StringUtils.isBlank(firstname) ? lastname : firstname + (StringUtils.isBlank(lastname) ? "" : " " + lastname);
	}
}
