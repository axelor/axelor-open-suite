package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.google.common.base.MoreObjects;

/**
 * Object to store field types like OneToMany,ManyToOne,BigDecimal,..etc.
 * Planning to replace this objec with select.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_FIELD_TYPE")
public class FieldType extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_FIELD_TYPE_SEQ")
	@SequenceGenerator(name = "STUDIO_FIELD_TYPE_SEQ", sequenceName = "STUDIO_FIELD_TYPE_SEQ", allocationSize = 1)
	private Long id;

	@HashKey
	@NotNull
	@Column(unique = true)
	private String name;

	public FieldType() {
	}

	public FieldType(String name) {
		this.name = name;
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

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof FieldType)) return false;

		final FieldType other = (FieldType) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getName(), other.getName())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(-1025788108, this.getName());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("name", this.getName());

		return tsh.omitNullValues().toString();
	}
}
