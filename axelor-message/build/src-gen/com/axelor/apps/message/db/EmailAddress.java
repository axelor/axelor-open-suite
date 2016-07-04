package com.axelor.apps.message.db;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "MESSAGE_EMAIL_ADDRESS")
public class EmailAddress extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_EMAIL_ADDRESS_SEQ")
	@SequenceGenerator(name = "MESSAGE_EMAIL_ADDRESS_SEQ", sequenceName = "MESSAGE_EMAIL_ADDRESS_SEQ", allocationSize = 1)
	private Long id;

	private String importId;

	@Widget(title = "Address", help = "true")
	@NameColumn
	@Index(name = "MESSAGE_EMAIL_ADDRESS_ADDRESS_IDX")
	private String address;

	public EmailAddress() {
	}

	public EmailAddress(String address) {
		this.address = address;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getImportId() {
		return importId;
	}

	public void setImportId(String importId) {
		this.importId = importId;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof EmailAddress)) return false;

		final EmailAddress other = (EmailAddress) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("importId", this.getImportId());
		tsh.add("address", this.getAddress());

		return tsh.omitNullValues().toString();
	}
}
