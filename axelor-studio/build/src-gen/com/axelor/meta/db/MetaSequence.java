package com.axelor.meta.db;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.google.common.base.MoreObjects;

/**
 * This object store custom sequences.
 */
@Entity
@Table(name = "META_SEQUENCE")
public class MetaSequence extends AuditableModel {

	@HashKey
	@NotNull
	@Size(min = 2)
	@Column(unique = true)
	private String name;

	private String prefix;

	private String suffix;

	@NotNull
	private Integer padding = 0;

	@NotNull
	@Column(name = "increment_by")
	private Integer increment = 1;

	@NotNull
	@Column(name = "initial_value")
	private Long initial = 0L;

	@NotNull
	@Column(name = "next_value")
	private Long next = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Index(name = "META_SEQUENCE_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	public MetaSequence() {
	}

	public MetaSequence(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public Integer getPadding() {
		return padding == null ? 0 : padding;
	}

	public void setPadding(Integer padding) {
		this.padding = padding;
	}

	public Integer getIncrement() {
		return increment == null ? 0 : increment;
	}

	public void setIncrement(Integer increment) {
		this.increment = increment;
	}

	public Long getInitial() {
		return initial == null ? 0L : initial;
	}

	public void setInitial(Long initial) {
		this.initial = initial;
	}

	public Long getNext() {
		return next == null ? 0L : next;
	}

	public void setNext(Long next) {
		this.next = next;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MetaSequence)) return false;

		final MetaSequence other = (MetaSequence) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getName(), other.getName())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(1110012166, this.getName());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("name", this.getName());
		tsh.add("prefix", this.getPrefix());
		tsh.add("suffix", this.getSuffix());
		tsh.add("padding", this.getPadding());
		tsh.add("increment", this.getIncrement());
		tsh.add("initial", this.getInitial());
		tsh.add("next", this.getNext());

		return tsh.omitNullValues().toString();
	}
}
