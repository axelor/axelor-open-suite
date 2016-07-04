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
import com.axelor.meta.db.MetaField;
import com.google.common.base.MoreObjects;

/**
 * This object store custom action lines.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_ACTION_BUILDER_LINE")
public class ActionBuilderLine extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_ACTION_BUILDER_LINE_SEQ")
	@SequenceGenerator(name = "STUDIO_ACTION_BUILDER_LINE_SEQ", sequenceName = "STUDIO_ACTION_BUILDER_LINE_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Action Builder")
	@Index(name = "STUDIO_ACTION_BUILDER_LINE_ACTION_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ActionBuilder actionBuilder;

	@Widget(title = "Field")
	@Index(name = "STUDIO_ACTION_BUILDER_LINE_META_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField metaField;

	@Widget(title = "Target field")
	private String targetField;

	@Widget(title = "Value")
	private String value;

	@Widget(title = "Condition")
	private String conditionText;

	@Widget(title = "Filter")
	private String filter;

	@Widget(title = "Validation type", selection = "studio.action.builder.line.validation.type.select")
	private String validationTypeSelect;

	@Widget(title = "Message")
	private String validationMsg;

	public ActionBuilderLine() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public ActionBuilder getActionBuilder() {
		return actionBuilder;
	}

	public void setActionBuilder(ActionBuilder actionBuilder) {
		this.actionBuilder = actionBuilder;
	}

	public MetaField getMetaField() {
		return metaField;
	}

	public void setMetaField(MetaField metaField) {
		this.metaField = metaField;
	}

	public String getTargetField() {
		return targetField;
	}

	public void setTargetField(String targetField) {
		this.targetField = targetField;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getConditionText() {
		return conditionText;
	}

	public void setConditionText(String conditionText) {
		this.conditionText = conditionText;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getValidationTypeSelect() {
		return validationTypeSelect;
	}

	public void setValidationTypeSelect(String validationTypeSelect) {
		this.validationTypeSelect = validationTypeSelect;
	}

	public String getValidationMsg() {
		return validationMsg;
	}

	public void setValidationMsg(String validationMsg) {
		this.validationMsg = validationMsg;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ActionBuilderLine)) return false;

		final ActionBuilderLine other = (ActionBuilderLine) obj;
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
		tsh.add("targetField", this.getTargetField());
		tsh.add("value", this.getValue());
		tsh.add("conditionText", this.getConditionText());
		tsh.add("filter", this.getFilter());
		tsh.add("validationTypeSelect", this.getValidationTypeSelect());
		tsh.add("validationMsg", this.getValidationMsg());

		return tsh.omitNullValues().toString();
	}
}
