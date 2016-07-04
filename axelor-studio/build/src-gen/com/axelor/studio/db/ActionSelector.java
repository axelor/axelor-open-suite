package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaAction;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "STUDIO_ACTION_SELECTOR")
public class ActionSelector extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_ACTION_SELECTOR_SEQ")
	@SequenceGenerator(name = "STUDIO_ACTION_SELECTOR_SEQ", sequenceName = "STUDIO_ACTION_SELECTOR_SEQ", allocationSize = 1)
	private Long id;

	private Integer sequence = 0;

	@Widget(title = "Meta Action")
	@Index(name = "STUDIO_ACTION_SELECTOR_META_ACTION_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaAction metaAction;

	@Widget(title = "Action Builder")
	@Index(name = "STUDIO_ACTION_SELECTOR_ACTION_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ActionBuilder actionBuilder;

	@Widget(title = "Name")
	@Index(name = "STUDIO_ACTION_SELECTOR_NAME_IDX")
	private String name;

	public ActionSelector() {
	}

	public ActionSelector(String name) {
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

	public Integer getSequence() {
		return sequence == null ? 0 : sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public MetaAction getMetaAction() {
		return metaAction;
	}

	public void setMetaAction(MetaAction metaAction) {
		this.metaAction = metaAction;
	}

	public ActionBuilder getActionBuilder() {
		return actionBuilder;
	}

	public void setActionBuilder(ActionBuilder actionBuilder) {
		this.actionBuilder = actionBuilder;
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
		if (!(obj instanceof ActionSelector)) return false;

		final ActionSelector other = (ActionSelector) obj;
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
		tsh.add("sequence", this.getSequence());
		tsh.add("name", this.getName());

		return tsh.omitNullValues().toString();
	}
}
