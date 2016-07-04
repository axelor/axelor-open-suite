package com.axelor.studio.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.google.common.base.MoreObjects;

/**
 * This is main class to store all edited views.

 * At present four view types form,grid,chart and dasbhoard supported.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_VIEW_BUILDER")
public class ViewBuilder extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_VIEW_BUILDER_SEQ")
	@SequenceGenerator(name = "STUDIO_VIEW_BUILDER_SEQ", sequenceName = "STUDIO_VIEW_BUILDER_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Model")
	@Index(name = "STUDIO_VIEW_BUILDER_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "Panels")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "viewBuilder", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("panelLevel")
	private List<ViewPanel> viewPanelList;

	@Widget(title = "Side panels")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "viewBuilderSideBar", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("panelLevel")
	private List<ViewPanel> viewSidePanelList;

	@Widget(title = "View items")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "viewBuilder", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequence")
	private List<ViewItem> viewItemList;

	@Widget(title = "Title")
	@NotNull
	private String title;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_VIEW_BUILDER_NAME_IDX")
	private String name;

	@Widget(title = "Model")
	private String model;

	@Widget(title = "Extends")
	@Index(name = "STUDIO_VIEW_BUILDER_META_VIEW_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaView metaView;

	@Widget(title = "View generated")
	@Index(name = "STUDIO_VIEW_BUILDER_META_VIEW_GENERATED_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaView metaViewGenerated;

	@Widget(title = "View type")
	private String viewType;

	@Widget(title = "Edited")
	private Boolean edited = Boolean.TRUE;

	private Boolean recorded = Boolean.FALSE;

	@Widget(title = "Toolbar")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "viewBuilderToolbar", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequence")
	private List<ViewItem> toolbar;

	@Widget(title = "On save")
	private String onSave;

	@Widget(title = "On new")
	private String onNew;

	private Boolean clearWkf = Boolean.FALSE;

	@Widget(title = "Add stream")
	private Boolean addStream = Boolean.FALSE;

	@Widget(title = "Filters")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "viewBuilder", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Filter> filterList;

	@Widget(title = "Aggregate On")
	@Index(name = "STUDIO_VIEW_BUILDER_AGGREGATE_ON_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField aggregateOn;

	@Widget(title = "Target")
	private String aggregateOnTarget;

	@Widget(selection = "studio.chart.builder.date.type")
	private String aggregateDateType;

	@Widget(title = "Group By")
	@Index(name = "STUDIO_VIEW_BUILDER_GROUP_ON_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField groupOn;

	@Widget(title = "Target")
	private String groupOnTarget;

	@Widget(selection = "studio.chart.builder.date.type")
	private String groupDateType;

	@Widget(title = "Display")
	@Index(name = "STUDIO_VIEW_BUILDER_DISPLAY_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField displayField;

	@Widget(selection = "studio.chart.builder.display.type")
	private Integer displayType = 0;

	@Widget(selection = "studio.chart.builder.chart.type")
	private String chartType = "bar";

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dashboard", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequence")
	private List<DashletBuilder> dashletBuilderList;

	public ViewBuilder() {
	}

	public ViewBuilder(String name) {
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

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public List<ViewPanel> getViewPanelList() {
		return viewPanelList;
	}

	public void setViewPanelList(List<ViewPanel> viewPanelList) {
		this.viewPanelList = viewPanelList;
	}

	/**
	 * Add the given {@link ViewPanel} item to the {@code viewPanelList}.
	 *
	 * <p>
	 * It sets {@code item.viewBuilder = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addViewPanelListItem(ViewPanel item) {
		if (viewPanelList == null) {
			viewPanelList = new ArrayList<ViewPanel>();
		}
		viewPanelList.add(item);
		item.setViewBuilder(this);
	}

	/**
	 * Remove the given {@link ViewPanel} item from the {@code viewPanelList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeViewPanelListItem(ViewPanel item) {
		if (viewPanelList == null) {
			return;
		}
		viewPanelList.remove(item);
	}

	/**
	 * Clear the {@code viewPanelList} collection.
	 *
	 * <p>
	 * If you have to query {@link ViewPanel} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearViewPanelList() {
		if (viewPanelList != null) {
			viewPanelList.clear();
		}
	}

	public List<ViewPanel> getViewSidePanelList() {
		return viewSidePanelList;
	}

	public void setViewSidePanelList(List<ViewPanel> viewSidePanelList) {
		this.viewSidePanelList = viewSidePanelList;
	}

	/**
	 * Add the given {@link ViewPanel} item to the {@code viewSidePanelList}.
	 *
	 * <p>
	 * It sets {@code item.viewBuilderSideBar = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addViewSidePanelListItem(ViewPanel item) {
		if (viewSidePanelList == null) {
			viewSidePanelList = new ArrayList<ViewPanel>();
		}
		viewSidePanelList.add(item);
		item.setViewBuilderSideBar(this);
	}

	/**
	 * Remove the given {@link ViewPanel} item from the {@code viewSidePanelList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeViewSidePanelListItem(ViewPanel item) {
		if (viewSidePanelList == null) {
			return;
		}
		viewSidePanelList.remove(item);
	}

	/**
	 * Clear the {@code viewSidePanelList} collection.
	 *
	 * <p>
	 * If you have to query {@link ViewPanel} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearViewSidePanelList() {
		if (viewSidePanelList != null) {
			viewSidePanelList.clear();
		}
	}

	public List<ViewItem> getViewItemList() {
		return viewItemList;
	}

	public void setViewItemList(List<ViewItem> viewItemList) {
		this.viewItemList = viewItemList;
	}

	/**
	 * Add the given {@link ViewItem} item to the {@code viewItemList}.
	 *
	 * <p>
	 * It sets {@code item.viewBuilder = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addViewItemListItem(ViewItem item) {
		if (viewItemList == null) {
			viewItemList = new ArrayList<ViewItem>();
		}
		viewItemList.add(item);
		item.setViewBuilder(this);
	}

	/**
	 * Remove the given {@link ViewItem} item from the {@code viewItemList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeViewItemListItem(ViewItem item) {
		if (viewItemList == null) {
			return;
		}
		viewItemList.remove(item);
	}

	/**
	 * Clear the {@code viewItemList} collection.
	 *
	 * <p>
	 * If you have to query {@link ViewItem} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearViewItemList() {
		if (viewItemList != null) {
			viewItemList.clear();
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public MetaView getMetaView() {
		return metaView;
	}

	public void setMetaView(MetaView metaView) {
		this.metaView = metaView;
	}

	public MetaView getMetaViewGenerated() {
		return metaViewGenerated;
	}

	public void setMetaViewGenerated(MetaView metaViewGenerated) {
		this.metaViewGenerated = metaViewGenerated;
	}

	public String getViewType() {
		return viewType;
	}

	public void setViewType(String viewType) {
		this.viewType = viewType;
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

	public List<ViewItem> getToolbar() {
		return toolbar;
	}

	public void setToolbar(List<ViewItem> toolbar) {
		this.toolbar = toolbar;
	}

	/**
	 * Add the given {@link ViewItem} item to the {@code toolbar}.
	 *
	 * <p>
	 * It sets {@code item.viewBuilderToolbar = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addToolbar(ViewItem item) {
		if (toolbar == null) {
			toolbar = new ArrayList<ViewItem>();
		}
		toolbar.add(item);
		item.setViewBuilderToolbar(this);
	}

	/**
	 * Remove the given {@link ViewItem} item from the {@code toolbar}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeToolbar(ViewItem item) {
		if (toolbar == null) {
			return;
		}
		toolbar.remove(item);
	}

	/**
	 * Clear the {@code toolbar} collection.
	 *
	 * <p>
	 * If you have to query {@link ViewItem} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearToolbar() {
		if (toolbar != null) {
			toolbar.clear();
		}
	}

	public String getOnSave() {
		return onSave;
	}

	public void setOnSave(String onSave) {
		this.onSave = onSave;
	}

	public String getOnNew() {
		return onNew;
	}

	public void setOnNew(String onNew) {
		this.onNew = onNew;
	}

	public Boolean getClearWkf() {
		return clearWkf == null ? Boolean.FALSE : clearWkf;
	}

	public void setClearWkf(Boolean clearWkf) {
		this.clearWkf = clearWkf;
	}

	public Boolean getAddStream() {
		return addStream == null ? Boolean.FALSE : addStream;
	}

	public void setAddStream(Boolean addStream) {
		this.addStream = addStream;
	}

	public List<Filter> getFilterList() {
		return filterList;
	}

	public void setFilterList(List<Filter> filterList) {
		this.filterList = filterList;
	}

	/**
	 * Add the given {@link Filter} item to the {@code filterList}.
	 *
	 * <p>
	 * It sets {@code item.viewBuilder = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addFilterListItem(Filter item) {
		if (filterList == null) {
			filterList = new ArrayList<Filter>();
		}
		filterList.add(item);
		item.setViewBuilder(this);
	}

	/**
	 * Remove the given {@link Filter} item from the {@code filterList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeFilterListItem(Filter item) {
		if (filterList == null) {
			return;
		}
		filterList.remove(item);
	}

	/**
	 * Clear the {@code filterList} collection.
	 *
	 * <p>
	 * If you have to query {@link Filter} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearFilterList() {
		if (filterList != null) {
			filterList.clear();
		}
	}

	public MetaField getAggregateOn() {
		return aggregateOn;
	}

	public void setAggregateOn(MetaField aggregateOn) {
		this.aggregateOn = aggregateOn;
	}

	public String getAggregateOnTarget() {
		return aggregateOnTarget;
	}

	public void setAggregateOnTarget(String aggregateOnTarget) {
		this.aggregateOnTarget = aggregateOnTarget;
	}

	public String getAggregateDateType() {
		return aggregateDateType;
	}

	public void setAggregateDateType(String aggregateDateType) {
		this.aggregateDateType = aggregateDateType;
	}

	public MetaField getGroupOn() {
		return groupOn;
	}

	public void setGroupOn(MetaField groupOn) {
		this.groupOn = groupOn;
	}

	public String getGroupOnTarget() {
		return groupOnTarget;
	}

	public void setGroupOnTarget(String groupOnTarget) {
		this.groupOnTarget = groupOnTarget;
	}

	public String getGroupDateType() {
		return groupDateType;
	}

	public void setGroupDateType(String groupDateType) {
		this.groupDateType = groupDateType;
	}

	public MetaField getDisplayField() {
		return displayField;
	}

	public void setDisplayField(MetaField displayField) {
		this.displayField = displayField;
	}

	public Integer getDisplayType() {
		return displayType == null ? 0 : displayType;
	}

	public void setDisplayType(Integer displayType) {
		this.displayType = displayType;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}

	public List<DashletBuilder> getDashletBuilderList() {
		return dashletBuilderList;
	}

	public void setDashletBuilderList(List<DashletBuilder> dashletBuilderList) {
		this.dashletBuilderList = dashletBuilderList;
	}

	/**
	 * Add the given {@link DashletBuilder} item to the {@code dashletBuilderList}.
	 *
	 * <p>
	 * It sets {@code item.dashboard = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addDashletBuilderListItem(DashletBuilder item) {
		if (dashletBuilderList == null) {
			dashletBuilderList = new ArrayList<DashletBuilder>();
		}
		dashletBuilderList.add(item);
		item.setDashboard(this);
	}

	/**
	 * Remove the given {@link DashletBuilder} item from the {@code dashletBuilderList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeDashletBuilderListItem(DashletBuilder item) {
		if (dashletBuilderList == null) {
			return;
		}
		dashletBuilderList.remove(item);
	}

	/**
	 * Clear the {@code dashletBuilderList} collection.
	 *
	 * <p>
	 * If you have to query {@link DashletBuilder} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearDashletBuilderList() {
		if (dashletBuilderList != null) {
			dashletBuilderList.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ViewBuilder)) return false;

		final ViewBuilder other = (ViewBuilder) obj;
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
		tsh.add("title", this.getTitle());
		tsh.add("name", this.getName());
		tsh.add("model", this.getModel());
		tsh.add("viewType", this.getViewType());
		tsh.add("edited", this.getEdited());
		tsh.add("recorded", this.getRecorded());
		tsh.add("onSave", this.getOnSave());
		tsh.add("onNew", this.getOnNew());
		tsh.add("clearWkf", this.getClearWkf());
		tsh.add("addStream", this.getAddStream());
		tsh.add("aggregateOnTarget", this.getAggregateOnTarget());

		return tsh.omitNullValues().toString();
	}
}
