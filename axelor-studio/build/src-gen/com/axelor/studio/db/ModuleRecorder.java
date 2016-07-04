package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * General object to apply changes done in view and models.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_MODULE_RECORDER")
public class ModuleRecorder extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_MODULE_RECORDER_SEQ")
	@SequenceGenerator(name = "STUDIO_MODULE_RECORDER_SEQ", sequenceName = "STUDIO_MODULE_RECORDER_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Log")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String logText;

	private Boolean lastRunOk = Boolean.TRUE;

	public ModuleRecorder() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getLogText() {
		return logText;
	}

	public void setLogText(String logText) {
		this.logText = logText;
	}

	public Boolean getLastRunOk() {
		return lastRunOk == null ? Boolean.FALSE : lastRunOk;
	}

	public void setLastRunOk(Boolean lastRunOk) {
		this.lastRunOk = lastRunOk;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ModuleRecorder)) return false;

		final ModuleRecorder other = (ModuleRecorder) obj;
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
		tsh.add("lastRunOk", this.getLastRunOk());

		return tsh.omitNullValues().toString();
	}
}
