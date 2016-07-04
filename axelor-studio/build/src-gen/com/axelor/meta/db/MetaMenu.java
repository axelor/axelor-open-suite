package com.axelor.meta.db;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * This object store the menus.
 */
@Entity
@Cacheable
@Table(name = "META_MENU")
public class MetaMenu extends AuditableModel {

	private Integer priority = 0;

	@Column(name = "order_seq")
	private Integer order = 0;

	@HashKey
	@Column(unique = true)
	private String xmlId;

	@NotNull
	@Index(name = "META_MENU_NAME_IDX")
	private String name;

	@NameColumn
	@NotNull
	@Index(name = "META_MENU_TITLE_IDX")
	private String title;

	private String icon;

	private String iconBackground;

	private String module;

	private String tag;

	private String tagGet;

	private Boolean tagCount = Boolean.FALSE;

	@Widget(selection = "label.style.selection")
	private String tagStyle;

	@Column(name = "top_menu")
	private Boolean top = Boolean.FALSE;

	@Column(name = "left_menu")
	private Boolean left = Boolean.TRUE;

	@Column(name = "mobile_menu")
	private Boolean mobile = Boolean.FALSE;

	private Boolean hidden = Boolean.FALSE;

	private String link;

	@Index(name = "META_MENU_PARENT_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaMenu parent;

	@Index(name = "META_MENU_ACTION_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaAction action;

	@Index(name = "META_MENU_USER_ID_IDX")
	@JoinColumn(name = "user_id")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User user;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Group> groups;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Role> roles;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_MENU_SEQ")
	@SequenceGenerator(name = "META_MENU_SEQ", sequenceName = "META_MENU_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Remove menu")
	private Boolean removeMenu = Boolean.FALSE;

	public MetaMenu() {
	}

	public MetaMenu(String name) {
		this.name = name;
	}

	public Integer getPriority() {
		return priority == null ? 0 : priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getOrder() {
		return order == null ? 0 : order;
	}

	public void setOrder(Integer order) {
		this.order = order;
	}

	public String getXmlId() {
		return xmlId;
	}

	public void setXmlId(String xmlId) {
		this.xmlId = xmlId;
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

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getTagGet() {
		return tagGet;
	}

	public void setTagGet(String tagGet) {
		this.tagGet = tagGet;
	}

	public Boolean getTagCount() {
		return tagCount == null ? Boolean.FALSE : tagCount;
	}

	public void setTagCount(Boolean tagCount) {
		this.tagCount = tagCount;
	}

	public String getTagStyle() {
		return tagStyle;
	}

	public void setTagStyle(String tagStyle) {
		this.tagStyle = tagStyle;
	}

	public Boolean getTop() {
		return top == null ? Boolean.FALSE : top;
	}

	public void setTop(Boolean top) {
		this.top = top;
	}

	public Boolean getLeft() {
		return left == null ? Boolean.FALSE : left;
	}

	public void setLeft(Boolean left) {
		this.left = left;
	}

	public Boolean getMobile() {
		return mobile == null ? Boolean.FALSE : mobile;
	}

	public void setMobile(Boolean mobile) {
		this.mobile = mobile;
	}

	public Boolean getHidden() {
		return hidden == null ? Boolean.FALSE : hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public MetaMenu getParent() {
		return parent;
	}

	public void setParent(MetaMenu parent) {
		this.parent = parent;
	}

	public MetaAction getAction() {
		return action;
	}

	public void setAction(MetaAction action) {
		this.action = action;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	/**
	 * Add the given {@link Role} item to the {@code roles}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addRole(Role item) {
		if (roles == null) {
			roles = new HashSet<Role>();
		}
		roles.add(item);
	}

	/**
	 * Remove the given {@link Role} item from the {@code roles}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeRole(Role item) {
		if (roles == null) {
			return;
		}
		roles.remove(item);
	}

	/**
	 * Clear the {@code roles} collection.
	 *
	 */
	public void clearRoles() {
		if (roles != null) {
			roles.clear();
		}
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getRemoveMenu() {
		return removeMenu == null ? Boolean.FALSE : removeMenu;
	}

	public void setRemoveMenu(Boolean removeMenu) {
		this.removeMenu = removeMenu;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MetaMenu)) return false;

		final MetaMenu other = (MetaMenu) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getXmlId(), other.getXmlId())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(-386041564, this.getXmlId());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("priority", this.getPriority());
		tsh.add("order", this.getOrder());
		tsh.add("xmlId", this.getXmlId());
		tsh.add("name", this.getName());
		tsh.add("title", this.getTitle());
		tsh.add("icon", this.getIcon());
		tsh.add("iconBackground", this.getIconBackground());
		tsh.add("module", this.getModule());
		tsh.add("tag", this.getTag());
		tsh.add("tagGet", this.getTagGet());
		tsh.add("tagCount", this.getTagCount());

		return tsh.omitNullValues().toString();
	}
}
