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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaField;
import com.google.common.base.MoreObjects;

/**
 * Object to store filters/conditions used in chart and ViewCalcuation object.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_FILTER")
public class Filter extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_FILTER_SEQ")
	@SequenceGenerator(name = "STUDIO_FILTER_SEQ", sequenceName = "STUDIO_FILTER_SEQ", allocationSize = 1)
	private Long id;

	private Integer importId = 0;

	@Widget(title = "Module")
	private String moduleName;

	@Index(name = "STUDIO_FILTER_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "Field")
	@NotNull
	@Index(name = "STUDIO_FILTER_META_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField metaField;

	@Index(name = "STUDIO_FILTER_WKF_TRANSITION_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private WkfTransition wkfTransition;

	@Widget(title = "Operator")
	@NotNull
	@Index(name = "STUDIO_FILTER_FILTER_OPERATOR_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private FilterOperator filterOperator;

	@Index(name = "STUDIO_FILTER_ACTION_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ActionBuilder actionBuilder;

	@Widget(title = "Target field")
	private String targetField;

	@Widget(title = "Target type")
	private String targetType;

	@Widget(title = "Value")
	private String value;

	@Widget(title = "Default Value")
	private String defaultValue;

	@Widget(title = "Is parameter ?")
	private Boolean isParameter = Boolean.FALSE;

	@Widget(title = "Logic operator", selection = "studio.chart.filter.logic.operator")
	private Integer logicOp = 0;

	public Filter() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Integer getImportId() {
		return importId == null ? 0 : importId;
	}

	public void setImportId(Integer importId) {
		this.importId = importId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public MetaField getMetaField() {
		return metaField;
	}

	public void setMetaField(MetaField metaField) {
		this.metaField = metaField;
	}

	public WkfTransition getWkfTransition() {
		return wkfTransition;
	}

	public void setWkfTransition(WkfTransition wkfTransition) {
		this.wkfTransition = wkfTransition;
	}

	public FilterOperator getFilterOperator() {
		return filterOperator;
	}

	public void setFilterOperator(FilterOperator filterOperator) {
		this.filterOperator = filterOperator;
	}

	public ActionBuilder getActionBuilder() {
		return actionBuilder;
	}

	public void setActionBuilder(ActionBuilder actionBuilder) {
		this.actionBuilder = actionBuilder;
	}

	public String getTargetField() {
		return targetField;
	}

	public void setTargetField(String targetField) {
		this.targetField = targetField;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean getIsParameter() {
		return isParameter == null ? Boolean.FALSE : isParameter;
	}

	public void setIsParameter(Boolean isParameter) {
		this.isParameter = isParameter;
	}

	public Integer getLogicOp() {
		return logicOp == null ? 0 : logicOp;
	}

	public void setLogicOp(Integer logicOp) {
		this.logicOp = logicOp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Filter)) return false;

		final Filter other = (Filter) obj;
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
		tsh.add("importId", this.getImportId());
		tsh.add("moduleName", this.getModuleName());
		tsh.add("targetField", this.getTargetField());
		tsh.add("targetType", this.getTargetType());
		tsh.add("value", this.getValue());
		tsh.add("defaultValue", this.getDefaultValue());
		tsh.add("isParameter", this.getIsParameter());
		tsh.add("logicOp", this.getLogicOp());

		return tsh.omitNullValues().toString();
	}
}
