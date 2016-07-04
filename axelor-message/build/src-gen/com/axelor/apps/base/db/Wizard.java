package com.axelor.apps.base.db;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "BASE_WIZARD")
public class Wizard extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BASE_WIZARD_SEQ")
	@SequenceGenerator(name = "BASE_WIZARD_SEQ", sequenceName = "BASE_WIZARD_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@Index(name = "BASE_WIZARD_NAME_IDX")
	private String name;

	@Widget(title = "Code")
	@Index(name = "BASE_WIZARD_CODE_IDX")
	private String code;

	public Wizard() {
	}

	public Wizard(String name, String code) {
		this.name = name;
		this.code = code;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Wizard)) return false;

		final Wizard other = (Wizard) obj;
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
		tsh.add("name", this.getName());
		tsh.add("code", this.getCode());

		return tsh.omitNullValues().toString();
	}
}
