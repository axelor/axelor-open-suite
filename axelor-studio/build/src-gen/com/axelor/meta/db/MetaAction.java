package com.axelor.meta.db;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * This object store the xml actions.
 */
@Entity
@Cacheable
@Table(name = "META_ACTION")
public class MetaAction extends AuditableModel {

	private Integer priority = 0;

	@HashKey
	@Column(unique = true)
	private String xmlId;

	@NotNull
	@Index(name = "META_ACTION_NAME_IDX")
	private String name;

	@Widget(selection = "action.type.selection")
	@NotNull
	private String type;

	@Widget(title = "Used as home action", help = "Specify whether this action can be used as home action.")
	private Boolean home = Boolean.FALSE;

	private String model;

	private String module;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@NotNull
	private String xml;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_ACTION_SEQ")
	@SequenceGenerator(name = "META_ACTION_SEQ", sequenceName = "META_ACTION_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Remove action")
	private Boolean removeAction = Boolean.FALSE;

	public MetaAction() {
	}

	public MetaAction(String name) {
		this.name = name;
	}

	public Integer getPriority() {
		return priority == null ? 0 : priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Specify whether this action can be used as home action.
	 *
	 * @return the property value
	 */
	public Boolean getHome() {
		return home == null ? Boolean.FALSE : home;
	}

	public void setHome(Boolean home) {
		this.home = home;
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

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getRemoveAction() {
		return removeAction == null ? Boolean.FALSE : removeAction;
	}

	public void setRemoveAction(Boolean removeAction) {
		this.removeAction = removeAction;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MetaAction)) return false;

		final MetaAction other = (MetaAction) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getXmlId(), other.getXmlId())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(-1963981637, this.getXmlId());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("priority", this.getPriority());
		tsh.add("xmlId", this.getXmlId());
		tsh.add("name", this.getName());
		tsh.add("type", this.getType());
		tsh.add("home", this.getHome());
		tsh.add("model", this.getModel());
		tsh.add("module", this.getModule());
		tsh.add("removeAction", this.getRemoveAction());

		return tsh.omitNullValues().toString();
	}
}
