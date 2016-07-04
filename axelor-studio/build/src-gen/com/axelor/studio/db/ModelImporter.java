package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.MoreObjects;

/**
 * This class is use to import models in xlsx format.

 * It will also generate view if boolean generateView checked.
 */
@Entity
@Table(name = "STUDIO_MODEL_IMPORTER")
public class ModelImporter extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_MODEL_IMPORTER_SEQ")
	@SequenceGenerator(name = "STUDIO_MODEL_IMPORTER_SEQ", sequenceName = "STUDIO_MODEL_IMPORTER_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Select File")
	@NotNull
	@Index(name = "STUDIO_MODEL_IMPORTER_META_FILE_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile metaFile;

	@Widget(title = "Log file")
	@Index(name = "STUDIO_MODEL_IMPORTER_LOG_FILE_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaFile logFile;

	public ModelImporter() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public MetaFile getMetaFile() {
		return metaFile;
	}

	public void setMetaFile(MetaFile metaFile) {
		this.metaFile = metaFile;
	}

	public MetaFile getLogFile() {
		return logFile;
	}

	public void setLogFile(MetaFile logFile) {
		this.logFile = logFile;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ModelImporter)) return false;

		final ModelImporter other = (ModelImporter) obj;
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

		return tsh.omitNullValues().toString();
	}
}
