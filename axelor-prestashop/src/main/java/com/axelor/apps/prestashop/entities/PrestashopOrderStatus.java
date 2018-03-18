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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="order_state")
public class PrestashopOrderStatus extends PrestashopIdentifiableEntity {
	private Boolean unremovable;
	private Boolean delivered;
	private Boolean hidden;
	private Boolean sendEmail;
	private String moduleName;
	private Boolean invoiced;
	private String color;
	private Boolean loggable;
	private Boolean shipped;
	private Boolean paid;
	private Boolean pdfDelivery;
	private Boolean pdfInvoice;
	private Boolean deleted;
	private PrestashopTranslatableString name;
	private PrestashopTranslatableString template;

	public Boolean getUnremovable() {
		return unremovable;
	}

	public void setUnremovable(Boolean unremovable) {
		this.unremovable = unremovable;
	}

	@XmlElement(name="delivery")
	public Boolean getDelivered() {
		return delivered;
	}

	public void setDelivered(Boolean delivered) {
		this.delivered = delivered;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	@XmlElement(name="send_email")
	public Boolean getSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(Boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	@XmlElement(name="module_name")
	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	@XmlElement(name="invoice")
	public Boolean getInvoiced() {
		return invoiced;
	}

	public void setInvoiced(Boolean invoiced) {
		this.invoiced = invoiced;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@XmlElement(name="logable")
	public Boolean getLoggable() {
		return loggable;
	}

	public void setLoggable(Boolean loggable) {
		this.loggable = loggable;
	}

	public Boolean getShipped() {
		return shipped;
	}

	public void setShipped(Boolean shipped) {
		this.shipped = shipped;
	}

	public Boolean getPaid() {
		return paid;
	}

	public void setPaid(Boolean paid) {
		this.paid = paid;
	}

	@XmlElement(name="pdf_delivery")
	public Boolean getPdfDelivery() {
		return pdfDelivery;
	}

	public void setPdfDelivery(Boolean pdfDelivery) {
		this.pdfDelivery = pdfDelivery;
	}

	@XmlElement(name="pdf_invoice")
	public Boolean getPdfInvoice() {
		return pdfInvoice;
	}

	public void setPdfInvoice(Boolean pdfInvoice) {
		this.pdfInvoice = pdfInvoice;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public PrestashopTranslatableString getName() {
		return name;
	}

	public void setName(PrestashopTranslatableString name) {
		this.name = name;
	}

	public PrestashopTranslatableString getTemplate() {
		return template;
	}

	public void setTemplate(PrestashopTranslatableString template) {
		this.template = template;
	}
}
