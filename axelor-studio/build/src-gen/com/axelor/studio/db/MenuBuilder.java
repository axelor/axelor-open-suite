package com.axelor.studio.db;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

/**
 * This object store edited menu with menu attributes.

 * MetaMenu and 'menuitem' will be generated through this object.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_MENU_BUILDER")
public class MenuBuilder extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_MENU_BUILDER_SEQ")
	@SequenceGenerator(name = "STUDIO_MENU_BUILDER_SEQ", sequenceName = "STUDIO_MENU_BUILDER_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Module")
	private String moduleName;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_MENU_BUILDER_NAME_IDX")
	private String name;

	@Widget(title = "Title")
	@NotNull
	private String title;

	@Widget(title = "Parent menu")
	@Index(name = "STUDIO_MENU_BUILDER_META_MENU_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaMenu metaMenu;

	@Widget(title = "Parent builder")
	@Index(name = "STUDIO_MENU_BUILDER_MENU_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MenuBuilder menuBuilder;

	@Widget(title = "Is parent ?")
	private Boolean isParent = Boolean.FALSE;

	@Widget(title = "Model")
	@Index(name = "STUDIO_MENU_BUILDER_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "Groups")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Group> groups;

	@Widget(title = "Top")
	private Boolean top = Boolean.FALSE;

	@Widget(title = "Parent")
	private String parent;

	@Widget(title = "Model")
	private String model;

	@Widget(title = "Icon")
	private String icon;

	@Widget(title = "Icon background")
	private String iconBackground;

	private Boolean edited = Boolean.TRUE;

	private Boolean recorded = Boolean.FALSE;

	@Widget(title = "Dashboard builder")
	@Index(name = "STUDIO_MENU_BUILDER_DASHBOARD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder dashboard;

	@Widget(title = "Domain")
	private String domain;

	@Widget(title = "Order")
	@Column(name = "order_seq")
	private Integer order = 0;

	@Widget(title = "Menu generated")
	@Index(name = "STUDIO_MENU_BUILDER_MENU_GENERATED_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaMenu menuGenerated;

	@Widget(title = "Action generated")
	@Index(name = "STUDIO_MENU_BUILDER_ACTION_GENERATED_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaAction actionGenerated;

	private Boolean deleteMenu = Boolean.FALSE;

	public MenuBuilder() {
	}

	public MenuBuilder(String name) {
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
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

	public MetaMenu getMetaMenu() {
		return metaMenu;
	}

	public void setMetaMenu(MetaMenu metaMenu) {
		this.metaMenu = metaMenu;
	}

	public MenuBuilder getMenuBuilder() {
		return menuBuilder;
	}

	public void setMenuBuilder(MenuBuilder menuBuilder) {
		this.menuBuilder = menuBuilder;
	}

	public Boolean getIsParent() {
		return isParent == null ? Boolean.FALSE : isParent;
	}

	public void setIsParent(Boolean isParent) {
		this.isParent = isParent;
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	/**
	 * Add the given {@link Group} item to the {@code groups}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addGroup(Group item) {
		if (groups == null) {
			groups = new HashSet<Group>();
		}
		groups.add(item);
	}

	/**
	 * Remove the given {@link Group} item from the {@code groups}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeGroup(Group item) {
		if (groups == null) {
			return;
		}
		groups.remove(item);
	}

	/**
	 * Clear the {@code groups} collection.
	 *
	 */
	public void clearGroups() {
		if (groups != null) {
			groups.clear();
		}
	}

	public Boolean getTop() {
		return top == null ? Boolean.FALSE : top;
	}

	public void setTop(Boolean top) {
		this.top = top;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIconBackground() {
		return iconBackground;
	}

	public void setIconBackground(String iconBackground) {
		this.iconBackground = iconBackground;
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

	public ViewBuilder getDashboard() {
		return dashboard;
	}

	public void setDashboard(ViewBuilder dashboard) {
		this.dashboard = dashboard;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Integer getOrder() {
		return order == null ? 0 : order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public MetaMenu getMenuGenerated() {
		return menuGenerated;
	}

	public void setMenuGenerated(MetaMenu menuGenerated) {
		this.menuGenerated = menuGenerated;
	}

	public MetaAction getActionGenerated() {
		return actionGenerated;
	}

	public void setActionGenerated(MetaAction actionGenerated) {
		this.actionGenerated = actionGenerated;
	}

	public Boolean getDeleteMenu() {
		return deleteMenu == null ? Boolean.FALSE : deleteMenu;
	}

	public void setDeleteMenu(Boolean deleteMenu) {
		this.deleteMenu = deleteMenu;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MenuBuilder)) return false;

		final MenuBuilder other = (MenuBuilder) obj;
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
		tsh.add("moduleName", this.getModuleName());
		tsh.add("name", this.getName());
		tsh.add("title", this.getTitle());
		tsh.add("isParent", this.getIsParent());
		tsh.add("top", this.getTop());
		tsh.add("parent", this.getParent());
		tsh.add("model", this.getModel());
		tsh.add("icon", this.getIcon());
		tsh.add("iconBackground", this.getIconBackground());
		tsh.add("edited", this.getEdited());
		tsh.add("recorded", this.getRecorded());

		return tsh.omitNullValues().toString();
	}
}
