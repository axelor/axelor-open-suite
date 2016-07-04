package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

/**
 * This object store group and permission for model and fields. Used by field and model builder.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_RIGHT_MANAGEMENT")
public class RightManagement extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_RIGHT_MANAGEMENT_SEQ")
	@SequenceGenerator(name = "STUDIO_RIGHT_MANAGEMENT_SEQ", sequenceName = "STUDIO_RIGHT_MANAGEMENT_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Group")
	@Index(name = "STUDIO_RIGHT_MANAGEMENT_AUTH_GROUP_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Group authGroup;

	@Widget(title = "Role")
	@Index(name = "STUDIO_RIGHT_MANAGEMENT_AUTH_ROLE_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Role authRole;

	@Widget(title = "Read", help = "Whether to grant read access.")
	private Boolean canRead = Boolean.FALSE;

	@Widget(title = "Write", help = "Whether to grant write access.")
	private Boolean canWrite = Boolean.FALSE;

	@Widget(title = "Create", help = "Whether to grant create access.")
	private Boolean canCreate = Boolean.FALSE;

	@Widget(title = "Remove", help = "Whether to grant remove access.")
	private Boolean canRemove = Boolean.FALSE;

	@Widget(title = "Export", help = "Whether to grant export access.")
	private Boolean canExport = Boolean.FALSE;

	@Widget(help = "Domain filter as condition.")
	@Column(name = "condition_value")
	private String condition;

	@Widget(help = "Comma separated list of params for the condition.")
	private String conditionParams;

	@Index(name = "STUDIO_RIGHT_MANAGEMENT_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Index(name = "STUDIO_RIGHT_MANAGEMENT_META_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField metaField;

	private String readonlyIf;

	private String hideIf;

	private Boolean edited = Boolean.TRUE;

	@Widget(title = "Name")
	@Index(name = "STUDIO_RIGHT_MANAGEMENT_NAME_IDX")
	private String name;

	public RightManagement() {
	}

	public RightManagement(String name) {
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

	public Group getAuthGroup() {
		return authGroup;
	}

	public void setAuthGroup(Group authGroup) {
		this.authGroup = authGroup;
	}

	public Role getAuthRole() {
		return authRole;
	}

	public void setAuthRole(Role authRole) {
		this.authRole = authRole;
	}

	/**
	 * Whether to grant read access.
	 *
	 * @return the property value
	 */
	public Boolean getCanRead() {
		return canRead == null ? Boolean.FALSE : canRead;
	}

	public void setCanRead(Boolean canRead) {
		this.canRead = canRead;
	}

	/**
	 * Whether to grant write access.
	 *
	 * @return the property value
	 */
	public Boolean getCanWrite() {
		return canWrite == null ? Boolean.FALSE : canWrite;
	}

	public void setCanWrite(Boolean canWrite) {
		this.canWrite = canWrite;
	}

	/**
	 * Whether to grant create access.
	 *
	 * @return the property value
	 */
	public Boolean getCanCreate() {
		return canCreate == null ? Boolean.FALSE : canCreate;
	}

	public void setCanCreate(Boolean canCreate) {
		this.canCreate = canCreate;
	}

	/**
	 * Whether to grant remove access.
	 *
	 * @return the property value
	 */
	public Boolean getCanRemove() {
		return canRemove == null ? Boolean.FALSE : canRemove;
	}

	public void setCanRemove(Boolean canRemove) {
		this.canRemove = canRemove;
	}

	/**
	 * Whether to grant export access.
	 *
	 * @return the property value
	 */
	public Boolean getCanExport() {
		return canExport == null ? Boolean.FALSE : canExport;
	}

	public void setCanExport(Boolean canExport) {
		this.canExport = canExport;
	}

	/**
	 * Domain filter as condition.
	 *
	 * @return the property value
	 */
	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * Comma separated list of params for the condition.
	 *
	 * @return the property value
	 */
	public String getConditionParams() {
		return conditionParams;
	}

	public void setConditionParams(String conditionParams) {
		this.conditionParams = conditionParams;
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public MetaField getMetaField() {
		return metaField;
	}

	public void setMetaField(MetaField metaField) {
		this.metaField = metaField;
	}

	public String getReadonlyIf() {
		return readonlyIf;
	}

	public void setReadonlyIf(String readonlyIf) {
		this.readonlyIf = readonlyIf;
	}

	public String getHideIf() {
		return hideIf;
	}

	public void setHideIf(String hideIf) {
		this.hideIf = hideIf;
	}

	public Boolean getEdited() {
		return edited == null ? Boolean.FALSE : edited;
	}

	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof RightManagement)) return false;

		final RightManagement other = (RightManagement) obj;
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
		tsh.add("canRead", this.getCanRead());
		tsh.add("canWrite", this.getCanWrite());
		tsh.add("canCreate", this.getCanCreate());
		tsh.add("canRemove", this.getCanRemove());
		tsh.add("canExport", this.getCanExport());
		tsh.add("condition", this.getCondition());
		tsh.add("conditionParams", this.getConditionParams());
		tsh.add("readonlyIf", this.getReadonlyIf());
		tsh.add("hideIf", this.getHideIf());
		tsh.add("edited", this.getEdited());
		tsh.add("name", this.getName());

		return tsh.omitNullValues().toString();
	}
}
