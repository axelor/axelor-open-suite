package com.axelor.studio.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

/**
 * This class is main workflow class. Workflow is linked with model and its formview.

 * Workflow digram will be created using bpmnEditor widget and stored in bpmn xml format.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_WKF")
public class Wkf extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_WKF_SEQ")
	@SequenceGenerator(name = "STUDIO_WKF_SEQ", sequenceName = "STUDIO_WKF_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_WKF_NAME_IDX")
	private String name;

	@Widget(title = "Object")
	@NotNull
	@Index(name = "STUDIO_WKF_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "ViewBuilder")
	@Index(name = "STUDIO_WKF_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "Process display type", selection = "studio.business.wkf.display.type.select")
	private Integer displayTypeSelect = 0;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkf", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequence")
	private List<WkfNode> nodes;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkf", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WkfTransition> transitions;

	@Widget(title = "Bpmn xml")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String bpmnXml;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkf", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WkfTracking> wkfTrackings;

	@Widget(title = "Edited")
	private Boolean edited = Boolean.TRUE;

	public Wkf() {
	}

	public Wkf(String name) {
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

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public Integer getDisplayTypeSelect() {
		return displayTypeSelect == null ? 0 : displayTypeSelect;
	}

	public void setDisplayTypeSelect(Integer displayTypeSelect) {
		this.displayTypeSelect = displayTypeSelect;
	}

	public List<WkfNode> getNodes() {
		return nodes;
	}

	public void setNodes(List<WkfNode> nodes) {
		this.nodes = nodes;
	}

	/**
	 * Add the given {@link WkfNode} item to the {@code nodes}.
	 *
	 * <p>
	 * It sets {@code item.wkf = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addNode(WkfNode item) {
		if (nodes == null) {
			nodes = new ArrayList<WkfNode>();
		}
		nodes.add(item);
		item.setWkf(this);
	}

	/**
	 * Remove the given {@link WkfNode} item from the {@code nodes}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeNode(WkfNode item) {
		if (nodes == null) {
			return;
		}
		nodes.remove(item);
	}

	/**
	 * Clear the {@code nodes} collection.
	 *
	 * <p>
	 * If you have to query {@link WkfNode} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearNodes() {
		if (nodes != null) {
			nodes.clear();
		}
	}

	public List<WkfTransition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<WkfTransition> transitions) {
		this.transitions = transitions;
	}

	/**
	 * Add the given {@link WkfTransition} item to the {@code transitions}.
	 *
	 * <p>
	 * It sets {@code item.wkf = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addTransition(WkfTransition item) {
		if (transitions == null) {
			transitions = new ArrayList<WkfTransition>();
		}
		transitions.add(item);
		item.setWkf(this);
	}

	/**
	 * Remove the given {@link WkfTransition} item from the {@code transitions}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeTransition(WkfTransition item) {
		if (transitions == null) {
			return;
		}
		transitions.remove(item);
	}

	/**
	 * Clear the {@code transitions} collection.
	 *
	 * <p>
	 * If you have to query {@link WkfTransition} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearTransitions() {
		if (transitions != null) {
			transitions.clear();
		}
	}

	public String getBpmnXml() {
		return bpmnXml;
	}

	public void setBpmnXml(String bpmnXml) {
		this.bpmnXml = bpmnXml;
	}

	public List<WkfTracking> getWkfTrackings() {
		return wkfTrackings;
	}

	public void setWkfTrackings(List<WkfTracking> wkfTrackings) {
		this.wkfTrackings = wkfTrackings;
	}

	/**
	 * Add the given {@link WkfTracking} item to the {@code wkfTrackings}.
	 *
	 * <p>
	 * It sets {@code item.wkf = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addWkfTracking(WkfTracking item) {
		if (wkfTrackings == null) {
			wkfTrackings = new ArrayList<WkfTracking>();
		}
		wkfTrackings.add(item);
		item.setWkf(this);
	}

	/**
	 * Remove the given {@link WkfTracking} item from the {@code wkfTrackings}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeWkfTracking(WkfTracking item) {
		if (wkfTrackings == null) {
			return;
		}
		wkfTrackings.remove(item);
	}

	/**
	 * Clear the {@code wkfTrackings} collection.
	 *
	 * <p>
	 * If you have to query {@link WkfTracking} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearWkfTrackings() {
		if (wkfTrackings != null) {
			wkfTrackings.clear();
		}
	}

	public Boolean getEdited() {
		return edited == null ? Boolean.FALSE : edited;
	}

	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Wkf)) return false;

		final Wkf other = (Wkf) obj;
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
		tsh.add("displayTypeSelect", this.getDisplayTypeSelect());
		tsh.add("edited", this.getEdited());

		return tsh.omitNullValues().toString();
	}
}
