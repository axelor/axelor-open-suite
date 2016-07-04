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
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * This object store panels of form view.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_VIEW_PANEL")
public class ViewPanel extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_VIEW_PANEL_SEQ")
	@SequenceGenerator(name = "STUDIO_VIEW_PANEL_SEQ", sequenceName = "STUDIO_VIEW_PANEL_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@Index(name = "STUDIO_VIEW_PANEL_NAME_IDX")
	private String name;

	@Widget(title = "Title")
	private String title;

	@Widget(title = "Items")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "viewPanel", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequence")
	private List<ViewItem> viewItemList;

	@Widget(title = "Place", selection = "form.view.builder.place.select")
	private Integer place = 0;

	@Index(name = "STUDIO_VIEW_PANEL_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Index(name = "STUDIO_VIEW_PANEL_VIEW_BUILDER_SIDE_BAR_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilderSideBar;

	@Widget(title = "Level")
	@NameColumn
	@NotNull
	@Index(name = "STUDIO_VIEW_PANEL_PANEL_LEVEL_IDX")
	private String panelLevel;

	@Widget(title = "Panel tab ?")
	private Boolean isPanelTab = Boolean.FALSE;

	@Widget(title = "Notebook ?")
	private Boolean isNotebook = Boolean.FALSE;

	public ViewPanel() {
	}

	public ViewPanel(String name) {
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
	 * It sets {@code item.viewPanel = this} to ensure the proper relationship.
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
		item.setViewPanel(this);
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

	public Integer getPlace() {
		return place == null ? 0 : place;
	}

	public void setPlace(Integer place) {
		this.place = place;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public ViewBuilder getViewBuilderSideBar() {
		return viewBuilderSideBar;
	}

	public void setViewBuilderSideBar(ViewBuilder viewBuilderSideBar) {
		this.viewBuilderSideBar = viewBuilderSideBar;
	}

	public String getPanelLevel() {
		return panelLevel;
	}

	public void setPanelLevel(String panelLevel) {
		this.panelLevel = panelLevel;
	}

	public Boolean getIsPanelTab() {
		return isPanelTab == null ? Boolean.FALSE : isPanelTab;
	}

	public void setIsPanelTab(Boolean isPanelTab) {
		this.isPanelTab = isPanelTab;
	}

	public Boolean getIsNotebook() {
		return isNotebook == null ? Boolean.FALSE : isNotebook;
	}

	public void setIsNotebook(Boolean isNotebook) {
		this.isNotebook = isNotebook;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ViewPanel)) return false;

		final ViewPanel other = (ViewPanel) obj;
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
		tsh.add("title", this.getTitle());
		tsh.add("place", this.getPlace());
		tsh.add("panelLevel", this.getPanelLevel());
		tsh.add("isPanelTab", this.getIsPanelTab());
		tsh.add("isNotebook", this.getIsNotebook());

		return tsh.omitNullValues().toString();
	}
}
