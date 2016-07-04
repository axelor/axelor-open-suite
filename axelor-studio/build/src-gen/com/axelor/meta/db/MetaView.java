package com.axelor.meta.db;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * This object store the xml views.
 */
@Entity
@Cacheable
@Table(name = "META_VIEW")
public class MetaView extends AuditableModel {

	@NotNull
	@Index(name = "META_VIEW_NAME_IDX")
	private String name;

	@NameColumn
	@NotNull
	@Index(name = "META_VIEW_TITLE_IDX")
	private String title;

	@Widget(selection = "view.type.selection")
	@NotNull
	private String type;

	@NotNull
	private Integer priority = 20;

	private String model;

	private String module;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@NotNull
	private String xml;

	@HashKey
	@Column(unique = true)
	private String xmlId;

	private String helpLink;

	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Group> groups;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_VIEW_SEQ")
	@SequenceGenerator(name = "META_VIEW_SEQ", sequenceName = "META_VIEW_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Remove view")
	private Boolean removeView = Boolean.FALSE;

	public MetaView() {
	}

	public MetaView(String name) {
		this.name = name;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPriority() {
		return priority == null ? 0 : priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getXmlId() {
		return xmlId;
	}

	public void setXmlId(String xmlId) {
		this.xmlId = xmlId;
	}

	public String getHelpLink() {
		return helpLink;
	}

	public void setHelpLink(String helpLink) {
		this.helpLink = helpLink;
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

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getRemoveView() {
		return removeView == null ? Boolean.FALSE : removeView;
	}

	public void setRemoveView(Boolean removeView) {
		this.removeView = removeView;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MetaView)) return false;

		final MetaView other = (MetaView) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getXmlId(), other.getXmlId())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(-385769878, this.getXmlId());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("name", this.getName());
		tsh.add("title", this.getTitle());
		tsh.add("type", this.getType());
		tsh.add("priority", this.getPriority());
		tsh.add("model", this.getModel());
		tsh.add("module", this.getModule());
		tsh.add("xmlId", this.getXmlId());
		tsh.add("helpLink", this.getHelpLink());
		tsh.add("removeView", this.getRemoveView());

		return tsh.omitNullValues().toString();
	}
}
