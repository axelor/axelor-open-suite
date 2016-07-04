package com.axelor.studio.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.apps.message.db.Template;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

/**
 * This object store custom action.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_ACTION_BUILDER", uniqueConstraints = { @UniqueConstraint(name = "actionModelConstraint", columnNames = { "meta_model", "name" }) })
public class ActionBuilder extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_ACTION_BUILDER_SEQ")
	@SequenceGenerator(name = "STUDIO_ACTION_BUILDER_SEQ", sequenceName = "STUDIO_ACTION_BUILDER_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_ACTION_BUILDER_NAME_IDX")
	private String name;

	@Widget(title = "Object")
	@Index(name = "STUDIO_ACTION_BUILDER_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "Type", selection = "studio.action.builder.type.select")
	private Integer typeSelect = 0;

	@Widget(title = "Target object")
	@Index(name = "STUDIO_ACTION_BUILDER_TARGET_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel targetModel;

	@Widget(title = "Loop on ?")
	@Index(name = "STUDIO_ACTION_BUILDER_LOOP_ON_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField loopOnField;

	@Widget(title = "Fields")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "actionBuilder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ActionBuilderLine> lines;

	@Widget(title = "Edited")
	private Boolean edited = Boolean.FALSE;

	@Widget(title = "Recorded")
	private Boolean recorded = Boolean.FALSE;

	@Widget(title = "View builder")
	@Index(name = "STUDIO_ACTION_BUILDER_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "Popup")
	private Boolean popup = Boolean.FALSE;

	@Widget(title = "Target field")
	@Index(name = "STUDIO_ACTION_BUILDER_TARGET_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField targetField;

	@Widget(title = "First group by")
	private String firstGroupBy;

	@Widget(title = "Second group by")
	private String secondGroupBy;

	@Widget(title = "Filters")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "actionBuilder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Filter> filters;

	@Widget(title = "Report builders")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<ReportBuilder> reportBuilderSet;

	@Widget(title = "Template")
	@Index(name = "STUDIO_ACTION_BUILDER_EMAIL_TEMPLATE_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Template emailTemplate;

	public ActionBuilder() {
	}

	public ActionBuilder(String name) {
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

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public Integer getTypeSelect() {
		return typeSelect == null ? 0 : typeSelect;
	}

	public void setTypeSelect(Integer typeSelect) {
		this.typeSelect = typeSelect;
	}

	public MetaModel getTargetModel() {
		return targetModel;
	}

	public void setTargetModel(MetaModel targetModel) {
		this.targetModel = targetModel;
	}

	public MetaField getLoopOnField() {
		return loopOnField;
	}

	public void setLoopOnField(MetaField loopOnField) {
		this.loopOnField = loopOnField;
	}

	public List<ActionBuilderLine> getLines() {
		return lines;
	}

	public void setLines(List<ActionBuilderLine> lines) {
		this.lines = lines;
	}

	/**
	 * Add the given {@link ActionBuilderLine} item to the {@code lines}.
	 *
	 * <p>
	 * It sets {@code item.actionBuilder = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addLine(ActionBuilderLine item) {
		if (lines == null) {
			lines = new ArrayList<ActionBuilderLine>();
		}
		lines.add(item);
		item.setActionBuilder(this);
	}

	/**
	 * Remove the given {@link ActionBuilderLine} item from the {@code lines}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeLine(ActionBuilderLine item) {
		if (lines == null) {
			return;
		}
		lines.remove(item);
	}

	/**
	 * Clear the {@code lines} collection.
	 *
	 * <p>
	 * If you have to query {@link ActionBuilderLine} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearLines() {
		if (lines != null) {
			lines.clear();
		}
	}

	public Boolean getEdited() {
		return edited == null ? Boolean.FALSE : edited;
	}

	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	public Boolean getRecorded() {
		return recorded == null ? Boolean.FALSE : recorded;
	}

	public void setRecorded(Boolean recorded) {
		this.recorded = recorded;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public Boolean getPopup() {
		return popup == null ? Boolean.FALSE : popup;
	}

	public void setPopup(Boolean popup) {
		this.popup = popup;
	}

	public MetaField getTargetField() {
		return targetField;
	}

	public void setTargetField(MetaField targetField) {
		this.targetField = targetField;
	}

	public String getFirstGroupBy() {
		return firstGroupBy;
	}

	public void setFirstGroupBy(String firstGroupBy) {
		this.firstGroupBy = firstGroupBy;
	}

	public String getSecondGroupBy() {
		return secondGroupBy;
	}

	public void setSecondGroupBy(String secondGroupBy) {
		this.secondGroupBy = secondGroupBy;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	/**
	 * Add the given {@link Filter} item to the {@code filters}.
	 *
	 * <p>
	 * It sets {@code item.actionBuilder = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addFilter(Filter item) {
		if (filters == null) {
			filters = new ArrayList<Filter>();
		}
		filters.add(item);
		item.setActionBuilder(this);
	}

	/**
	 * Remove the given {@link Filter} item from the {@code filters}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeFilter(Filter item) {
		if (filters == null) {
			return;
		}
		filters.remove(item);
	}

	/**
	 * Clear the {@code filters} collection.
	 *
	 * <p>
	 * If you have to query {@link Filter} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearFilters() {
		if (filters != null) {
			filters.clear();
		}
	}

	public Set<ReportBuilder> getReportBuilderSet() {
		return reportBuilderSet;
	}

	public void setReportBuilderSet(Set<ReportBuilder> reportBuilderSet) {
		this.reportBuilderSet = reportBuilderSet;
	}

	/**
	 * Add the given {@link ReportBuilder} item to the {@code reportBuilderSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addReportBuilderSetItem(ReportBuilder item) {
		if (reportBuilderSet == null) {
			reportBuilderSet = new HashSet<ReportBuilder>();
		}
		reportBuilderSet.add(item);
	}

	/**
	 * Remove the given {@link ReportBuilder} item from the {@code reportBuilderSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeReportBuilderSetItem(ReportBuilder item) {
		if (reportBuilderSet == null) {
			return;
		}
		reportBuilderSet.remove(item);
	}

	/**
	 * Clear the {@code reportBuilderSet} collection.
	 *
	 */
	public void clearReportBuilderSet() {
		if (reportBuilderSet != null) {
			reportBuilderSet.clear();
		}
	}

	public Template getEmailTemplate() {
		return emailTemplate;
	}

	public void setEmailTemplate(Template emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ActionBuilder)) return false;

		final ActionBuilder other = (ActionBuilder) obj;
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
		tsh.add("typeSelect", this.getTypeSelect());
		tsh.add("edited", this.getEdited());
		tsh.add("recorded", this.getRecorded());
		tsh.add("popup", this.getPopup());
		tsh.add("firstGroupBy", this.getFirstGroupBy());
		tsh.add("secondGroupBy", this.getSecondGroupBy());

		return tsh.omitNullValues().toString();
	}
}
