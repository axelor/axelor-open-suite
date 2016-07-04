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
import com.google.common.base.MoreObjects;

/**
 * This object store transition information of nodes of workflow.

 * Transition can be on button or on condition. So it will be used to generate button and actions.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_WKF_TRANSITION")
public class WkfTransition extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_WKF_TRANSITION_SEQ")
	@SequenceGenerator(name = "STUDIO_WKF_TRANSITION_SEQ", sequenceName = "STUDIO_WKF_TRANSITION_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "STUDIO_WKF_TRANSITION_NAME_IDX")
	private String name;

	@Widget(title = "xmlId")
	private String xmlId;

	@Widget(title = "Button ?")
	private Boolean isButton = Boolean.FALSE;

	@Widget(title = "Button title")
	private String buttonTitle;

	@Widget(title = "Restricted Roles")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Role> roleSet;

	@Widget(title = "Conditions")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkfTransition", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Filter> conditions;

	@Index(name = "STUDIO_WKF_TRANSITION_WKF_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Wkf wkf;

	@Widget(title = "From")
	@Index(name = "STUDIO_WKF_TRANSITION_SOURCE_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private WkfNode source;

	@Widget(title = "To")
	@Index(name = "STUDIO_WKF_TRANSITION_TARGET_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private WkfNode target;

	@Widget(title = "Alert or Blocking condition ?", selection = "studio.condition.alert.type.select")
	private Integer alertTypeSelect = 0;

	@Widget(title = "Message if fail (alert or blocking)")
	private String alertMsg;

	@Widget(title = "Message if succuss")
	private String successMsg;

	public WkfTransition() {
	}

	public WkfTransition(String name) {
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

	public String getXmlId() {
		return xmlId;
	}

	public void setXmlId(String xmlId) {
		this.xmlId = xmlId;
	}

	public Boolean getIsButton() {
		return isButton == null ? Boolean.FALSE : isButton;
	}

	public void setIsButton(Boolean isButton) {
		this.isButton = isButton;
	}

	public String getButtonTitle() {
		return buttonTitle;
	}

	public void setButtonTitle(String buttonTitle) {
		this.buttonTitle = buttonTitle;
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

	public List<Filter> getConditions() {
		return conditions;
	}

	public void setConditions(List<Filter> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Add the given {@link Filter} item to the {@code conditions}.
	 *
	 * <p>
	 * It sets {@code item.wkfTransition = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addCondition(Filter item) {
		if (conditions == null) {
			conditions = new ArrayList<Filter>();
		}
		conditions.add(item);
		item.setWkfTransition(this);
	}

	/**
	 * Remove the given {@link Filter} item from the {@code conditions}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeCondition(Filter item) {
		if (conditions == null) {
			return;
		}
		conditions.remove(item);
	}

	/**
	 * Clear the {@code conditions} collection.
	 *
	 * <p>
	 * If you have to query {@link Filter} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearConditions() {
		if (conditions != null) {
			conditions.clear();
		}
	}

	public Wkf getWkf() {
		return wkf;
	}

	public void setWkf(Wkf wkf) {
		this.wkf = wkf;
	}

	public WkfNode getSource() {
		return source;
	}

	public void setSource(WkfNode source) {
		this.source = source;
	}

	public WkfNode getTarget() {
		return target;
	}

	public void setTarget(WkfNode target) {
		this.target = target;
	}

	public Integer getAlertTypeSelect() {
		return alertTypeSelect == null ? 0 : alertTypeSelect;
	}

	public void setAlertTypeSelect(Integer alertTypeSelect) {
		this.alertTypeSelect = alertTypeSelect;
	}

	public String getAlertMsg() {
		return alertMsg;
	}

	public void setAlertMsg(String alertMsg) {
		this.alertMsg = alertMsg;
	}

	public String getSuccessMsg() {
		return successMsg;
	}

	public void setSuccessMsg(String successMsg) {
		this.successMsg = successMsg;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WkfTransition)) return false;

		final WkfTransition other = (WkfTransition) obj;
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
		tsh.add("xmlId", this.getXmlId());
		tsh.add("isButton", this.getIsButton());
		tsh.add("buttonTitle", this.getButtonTitle());
		tsh.add("alertTypeSelect", this.getAlertTypeSelect());
		tsh.add("alertMsg", this.getAlertMsg());
		tsh.add("successMsg", this.getSuccessMsg());

		return tsh.omitNullValues().toString();
	}
}
