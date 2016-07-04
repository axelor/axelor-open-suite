package com.axelor.studio.db;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * Object to store operators like =, !=, >, < ..etc with fields types in which they can be use.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_FILTER_OPERATOR")
public class FilterOperator extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_FILTER_OPERATOR_SEQ")
	@SequenceGenerator(name = "STUDIO_FILTER_OPERATOR_SEQ", sequenceName = "STUDIO_FILTER_OPERATOR_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@Index(name = "STUDIO_FILTER_OPERATOR_NAME_IDX")
	private String name;

	@Widget(title = "Value")
	private String value;

	@Widget(title = "Field types")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<FieldType> fieldTypeSet;

	public FilterOperator() {
	}

	public FilterOperator(String name) {
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Set<FieldType> getFieldTypeSet() {
		return fieldTypeSet;
	}

	public void setFieldTypeSet(Set<FieldType> fieldTypeSet) {
		this.fieldTypeSet = fieldTypeSet;
	}

	/**
	 * Add the given {@link FieldType} item to the {@code fieldTypeSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addFieldTypeSetItem(FieldType item) {
		if (fieldTypeSet == null) {
			fieldTypeSet = new HashSet<FieldType>();
		}
		fieldTypeSet.add(item);
	}

	/**
	 * Remove the given {@link FieldType} item from the {@code fieldTypeSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeFieldTypeSetItem(FieldType item) {
		if (fieldTypeSet == null) {
			return;
		}
		fieldTypeSet.remove(item);
	}

	/**
	 * Clear the {@code fieldTypeSet} collection.
	 *
	 */
	public void clearFieldTypeSet() {
		if (fieldTypeSet != null) {
			fieldTypeSet.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof FilterOperator)) return false;

		final FilterOperator other = (FilterOperator) obj;
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
		tsh.add("value", this.getValue());

		return tsh.omitNullValues().toString();
	}
}
