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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Role;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaMenu;
import com.google.common.base.MoreObjects;

/**
 * This is workflow node and it represents status of object linked with workflow.

 * It is also used to generate menu entry based on status.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_WKF_NODE")
public class WkfNode extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_WKF_NODE_SEQ")
	@SequenceGenerator(name = "STUDIO_WKF_NODE_SEQ", sequenceName = "STUDIO_WKF_NODE_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_WKF_NODE_NAME_IDX")
	private String name;

	@Widget(title = "Title")
	@NotNull
	private String title;

	@Widget(title = "xmlId")
	private String xmlId;

	@Index(name = "STUDIO_WKF_NODE_WKF_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Wkf wkf;

	@Widget(title = "Status menu entry")
	private Boolean statusMenuEntry = Boolean.FALSE;

	@Widget(title = "Status menu label")
	private String statusMenuLabel;

	@Widget(title = "My menu entry")
	private Boolean myMenuEntry = Boolean.FALSE;

	@Widget(title = "Field for 'My menu'")
	@Index(name = "STUDIO_WKF_NODE_META_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField metaField;

	@Widget(title = "My menu label")
	private String myMenuLabel;

	@Widget(title = "Parent menu")
	@Index(name = "STUDIO_WKF_NODE_PARENT_MENU_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaMenu parentMenu;

	@Widget(title = "Parent menu builder")
	@Index(name = "STUDIO_WKF_NODE_PARENT_MENU_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MenuBuilder parentMenuBuilder;

	@Widget(title = "Incomming")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<WkfTransition> incomming;

	@Widget(title = "Outgoing")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<WkfTransition> outgoing;

	@Widget(title = "ReadOnly For")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Role> roleSet;

	@Widget(title = "Sequence")
	private Integer sequence = 0;

	@Widget(title = "Start node ? ")
	private Boolean startNode = Boolean.FALSE;

	@Widget(title = "End node ?")
	private Boolean endNode = Boolean.FALSE;

	@Widget(title = "View builder")
	@Index(name = "STUDIO_WKF_NODE_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "Status menu")
	@Index(name = "STUDIO_WKF_NODE_STATUS_MENU_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MenuBuilder statusMenu;

	@Widget(title = "My Status menu")
	@Index(name = "STUDIO_WKF_NODE_MY_STATUS_MENU_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MenuBuilder myStatusMenu;

	@Widget(title = "Actions")
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private List<ActionSelector> actionSelectorList;

	public WkfNode() {
	}

	public WkfNode(String name) {
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

	public String getXmlId() {
		return xmlId;
	}

	public void setXmlId(String xmlId) {
		this.xmlId = xmlId;
	}

	public Wkf getWkf() {
		return wkf;
	}

	public void setWkf(Wkf wkf) {
		this.wkf = wkf;
	}

	public Boolean getStatusMenuEntry() {
		return statusMenuEntry == null ? Boolean.FALSE : statusMenuEntry;
	}

	public void setStatusMenuEntry(Boolean statusMenuEntry) {
		this.statusMenuEntry = statusMenuEntry;
	}

	public String getStatusMenuLabel() {
		return statusMenuLabel;
	}

	public void setStatusMenuLabel(String statusMenuLabel) {
		this.statusMenuLabel = statusMenuLabel;
	}

	public Boolean getMyMenuEntry() {
		return myMenuEntry == null ? Boolean.FALSE : myMenuEntry;
	}

	public void setMyMenuEntry(Boolean myMenuEntry) {
		this.myMenuEntry = myMenuEntry;
	}

	public MetaField getMetaField() {
		return metaField;
	}

	public void setMetaField(MetaField metaField) {
		this.metaField = metaField;
	}

	public String getMyMenuLabel() {
		return myMenuLabel;
	}

	public void setMyMenuLabel(String myMenuLabel) {
		this.myMenuLabel = myMenuLabel;
	}

	public MetaMenu getParentMenu() {
		return parentMenu;
	}

	public void setParentMenu(MetaMenu parentMenu) {
		this.parentMenu = parentMenu;
	}

	public MenuBuilder getParentMenuBuilder() {
		return parentMenuBuilder;
	}

	public void setParentMenuBuilder(MenuBuilder parentMenuBuilder) {
		this.parentMenuBuilder = parentMenuBuilder;
	}

	public Set<WkfTransition> getIncomming() {
		return incomming;
	}

	public void setIncomming(Set<WkfTransition> incomming) {
		this.incomming = incomming;
	}

	/**
	 * Add the given {@link WkfTransition} item to the {@code incomming}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addIncomming(WkfTransition item) {
		if (incomming == null) {
			incomming = new HashSet<WkfTransition>();
		}
		incomming.add(item);
	}

	/**
	 * Remove the given {@link WkfTransition} item from the {@code incomming}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeIncomming(WkfTransition item) {
		if (incomming == null) {
			return;
		}
		incomming.remove(item);
	}

	/**
	 * Clear the {@code incomming} collection.
	 *
	 */
	public void clearIncomming() {
		if (incomming != null) {
			incomming.clear();
		}
	}

	public Set<WkfTransition> getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(Set<WkfTransition> outgoing) {
		this.outgoing = outgoing;
	}

	/**
	 * Add the given {@link WkfTransition} item to the {@code outgoing}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addOutgoing(WkfTransition item) {
		if (outgoing == null) {
			outgoing = new HashSet<WkfTransition>();
		}
		outgoing.add(item);
	}

	/**
	 * Remove the given {@link WkfTransition} item from the {@code outgoing}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeOutgoing(WkfTransition item) {
		if (outgoing == null) {
			return;
		}
		outgoing.remove(item);
	}

	/**
	 * Clear the {@code outgoing} collection.
	 *
	 */
	public void clearOutgoing() {
		if (outgoing != null) {
			outgoing.clear();
		}
	}

	public Set<Role> getRoleSet() {
		return roleSet;
	}

	public void setRoleSet(Set<Role> roleSet) {
		this.roleSet = roleSet;
	}

	/**
	 * Add the given {@link Role} item to the {@code roleSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addRoleSetItem(Role item) {
		if (roleSet == null) {
			roleSet = new HashSet<Role>();
		}
		roleSet.add(item);
	}

	/**
	 * Remove the given {@link Role} item from the {@code roleSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeRoleSetItem(Role item) {
		if (roleSet == null) {
			return;
		}
		roleSet.remove(item);
	}

	/**
	 * Clear the {@code roleSet} collection.
	 *
	 */
	public void clearRoleSet() {
		if (roleSet != null) {
			roleSet.clear();
		}
	}

	public Integer getSequence() {
		return sequence == null ? 0 : sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Boolean getStartNode() {
		return startNode == null ? Boolean.FALSE : startNode;
	}

	public void setStartNode(Boolean startNode) {
		this.startNode = startNode;
	}

	public Boolean getEndNode() {
		return endNode == null ? Boolean.FALSE : endNode;
	}

	public void setEndNode(Boolean endNode) {
		this.endNode = endNode;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public MenuBuilder getStatusMenu() {
		return statusMenu;
	}

	public void setStatusMenu(MenuBuilder statusMenu) {
		this.statusMenu = statusMenu;
	}

	public MenuBuilder getMyStatusMenu() {
		return myStatusMenu;
	}

	public void setMyStatusMenu(MenuBuilder myStatusMenu) {
		this.myStatusMenu = myStatusMenu;
	}

	public List<ActionSelector> getActionSelectorList() {
		return actionSelectorList;
	}

	public void setActionSelectorList(List<ActionSelector> actionSelectorList) {
		this.actionSelectorList = actionSelectorList;
	}

	/**
	 * Add the given {@link ActionSelector} item to the {@code actionSelectorList}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addActionSelectorListItem(ActionSelector item) {
		if (actionSelectorList == null) {
			actionSelectorList = new ArrayList<ActionSelector>();
		}
		actionSelectorList.add(item);
	}

	/**
	 * Remove the given {@link ActionSelector} item from the {@code actionSelectorList}.
	 *
	 * <p>
	 * It sets {@code item.null = null} to break the relationship.
	 * </p>
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeActionSelectorListItem(ActionSelector item) {
		if (actionSelectorList == null) {
			return;
		}
		actionSelectorList.remove(item);
	}

	/**
	 * Clear the {@code actionSelectorList} collection.
	 *
	 * <p>
	 * It sets {@code item.null = null} to break the relationship.
	 * </p>
	 */
	public void clearActionSelectorList() {
		if (actionSelectorList != null) {
			actionSelectorList.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WkfNode)) return false;

		final WkfNode other = (WkfNode) obj;
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
		tsh.add("xmlId", this.getXmlId());
		tsh.add("statusMenuEntry", this.getStatusMenuEntry());
		tsh.add("statusMenuLabel", this.getStatusMenuLabel());
		tsh.add("myMenuEntry", this.getMyMenuEntry());
		tsh.add("myMenuLabel", this.getMyMenuLabel());
		tsh.add("sequence", this.getSequence());
		tsh.add("startNode", this.getStartNode());
		tsh.add("endNode", this.getEndNode());

		return tsh.omitNullValues().toString();
	}
}
