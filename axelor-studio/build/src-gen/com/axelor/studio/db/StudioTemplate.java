package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.MoreObjects;

/**
 * Object to export and import studio template in zip format.

 * Studio template contains all work done by user using axelor-studio.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_STUDIO_TEMPLATE")
public class StudioTemplate extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_STUDIO_TEMPLATE_SEQ")
	@SequenceGenerator(name = "STUDIO_STUDIO_TEMPLATE_SEQ", sequenceName = "STUDIO_STUDIO_TEMPLATE_SEQ", allocationSize = 1)
	private Long id;

	@HashKey
	@Widget(title = "Name")
	@NotNull
	@Column(unique = true)
	private String name;

	@Widget(title = "Template file")
	@NotNull
	@Index(name = "STUDIO_STUDIO_TEMPLATE_META_FILE_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile metaFile;

	@Widget(title = "Imported", readonly = true)
	private Boolean imported = Boolean.FALSE;

	@Widget(title = "Depends on")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String dependsOn;

	public StudioTemplate() {
	}

	public StudioTemplate(String name) {
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

	public MetaFile getMetaFile() {
		return metaFile;
	}

	public void setMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	public Boolean getImported() {
		return imported == null ? Boolean.FALSE : imported;
	}

	public void setImported(Boolean imported) {
		this.imported = imported;
	}

	public String getDependsOn() {
		return dependsOn;
	}

	public void setDependsOn(String dependsOn) {
		this.dependsOn = dependsOn;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof StudioTemplate)) return false;

		final StudioTemplate other = (StudioTemplate) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getName(), other.getName())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(749629584, this.getName());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("name", this.getName());
		tsh.add("imported", this.getImported());

		return tsh.omitNullValues().toString();
	}
}
