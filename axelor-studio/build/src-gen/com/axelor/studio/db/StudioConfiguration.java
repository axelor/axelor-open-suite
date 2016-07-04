package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "STUDIO_STUDIO_CONFIGURATION")
public class StudioConfiguration extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_STUDIO_CONFIGURATION_SEQ")
	@SequenceGenerator(name = "STUDIO_STUDIO_CONFIGURATION_SEQ", sequenceName = "STUDIO_STUDIO_CONFIGURATION_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Module name")
	@NotNull
	@Index(name = "STUDIO_STUDIO_CONFIGURATION_NAME_IDX")
	private String name;

	@Widget(title = "Depends")
	private String depends;

	@Widget(title = "Build command")
	private String buildCmd;

	public StudioConfiguration() {
	}

	public StudioConfiguration(String name) {
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

	public String getDepends() {
		return depends;
	}

	public void setDepends(String depends) {
		this.depends = depends;
	}

	public String getBuildCmd() {
		return buildCmd;
	}

	public void setBuildCmd(String buildCmd) {
		this.buildCmd = buildCmd;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof StudioConfiguration)) return false;

		final StudioConfiguration other = (StudioConfiguration) obj;
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
		tsh.add("depends", this.getDepends());
		tsh.add("buildCmd", this.getBuildCmd());

		return tsh.omitNullValues().toString();
	}
}
