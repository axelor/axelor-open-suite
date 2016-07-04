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
import com.axelor.meta.db.MetaView;
import com.google.common.base.MoreObjects;

@Entity
@Cacheable
@Table(name = "STUDIO_DASHLET_BUILDER")
public class DashletBuilder extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_DASHLET_BUILDER_SEQ")
	@SequenceGenerator(name = "STUDIO_DASHLET_BUILDER_SEQ", sequenceName = "STUDIO_DASHLET_BUILDER_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_DASHLET_BUILDER_NAME_IDX")
	private String name;

	@Widget(title = "View builder")
	@Index(name = "STUDIO_DASHLET_BUILDER_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "View")
	@Index(name = "STUDIO_DASHLET_BUILDER_META_VIEW_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaView metaView;

	@Index(name = "STUDIO_DASHLET_BUILDER_DASHBOARD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder dashboard;

	@Widget(title = "Sequence")
	private Integer sequence = 0;

	@Widget(title = "Type", selection = "view.type.selection")
	private String viewType;

	@Widget(title = "Colspan")
	private Integer colspan = 0;

	@Widget(title = "Pagination limit")
	private Integer paginationLimit = 0;

	public DashletBuilder() {
	}

	public DashletBuilder(String name) {
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

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public MetaView getMetaView() {
		return metaView;
	}

	public void setMetaView(MetaView metaView) {
		this.metaView = metaView;
	}

	public ViewBuilder getDashboard() {
		return dashboard;
	}

	public void setDashboard(ViewBuilder dashboard) {
		this.dashboard = dashboard;
	}

	public Integer getSequence() {
		return sequence == null ? 0 : sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
	}

	public Integer getColspan() {
		return colspan == null ? 0 : colspan;
	}

	public void setColspan(Integer colspan) {
		this.colspan = colspan;
	}

	public Integer getPaginationLimit() {
		return paginationLimit == null ? 0 : paginationLimit;
	}

	public void setPaginationLimit(Integer paginationLimit) {
		this.paginationLimit = paginationLimit;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof DashletBuilder)) return false;

		final DashletBuilder other = (DashletBuilder) obj;
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
		tsh.add("sequence", this.getSequence());
		tsh.add("viewType", this.getViewType());
		tsh.add("colspan", this.getColspan());
		tsh.add("paginationLimit", this.getPaginationLimit());

		return tsh.omitNullValues().toString();
	}
}
