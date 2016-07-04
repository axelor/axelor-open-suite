package com.axelor.meta.db;

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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.studio.db.RightManagement;
import com.google.common.base.MoreObjects;

/**
 * This object store the models.
 */
@Entity
@Cacheable
@Table(name = "META_MODEL")
public class MetaModel extends AuditableModel {

	@Widget(title = "Name")
	@NotNull
	@Index(name = "META_MODEL_NAME_IDX")
	private String name;

	@Widget(title = "Package")
	@NotNull
	private String packageName;

	@Widget(title = "Table")
	private String tableName;

	@Widget(title = "Fields")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metaModel", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MetaField> metaFields;

	@Widget(title = "Fullname")
	private String fullName;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_MODEL_SEQ")
	@SequenceGenerator(name = "META_MODEL_SEQ", sequenceName = "META_MODEL_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Right management")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metaModel", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RightManagement> rightManagementList;

	private Boolean customised = Boolean.FALSE;

	private Boolean edited = Boolean.FALSE;

	@Widget(title = "Title")
	private String title;

	@Widget(title = "Sequences")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metaModel", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MetaSequence> metaSequencList;

	public MetaModel() {
	}

	public MetaModel(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<MetaField> getMetaFields() {
		return metaFields;
	}

	public void setMetaFields(List<MetaField> metaFields) {
		this.metaFields = metaFields;
	}

	/**
	 * Add the given {@link MetaField} item to the {@code metaFields}.
	 *
	 * <p>
	 * It sets {@code item.metaModel = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addMetaField(MetaField item) {
		if (metaFields == null) {
			metaFields = new ArrayList<MetaField>();
		}
		metaFields.add(item);
		item.setMetaModel(this);
	}

	/**
	 * Remove the given {@link MetaField} item from the {@code metaFields}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeMetaField(MetaField item) {
		if (metaFields == null) {
			return;
		}
		metaFields.remove(item);
	}

	/**
	 * Clear the {@code metaFields} collection.
	 *
	 * <p>
	 * If you have to query {@link MetaField} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearMetaFields() {
		if (metaFields != null) {
			metaFields.clear();
		}
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public List<RightManagement> getRightManagementList() {
		return rightManagementList;
	}

	public void setRightManagementList(List<RightManagement> rightManagementList) {
		this.rightManagementList = rightManagementList;
	}

	/**
	 * Add the given {@link RightManagement} item to the {@code rightManagementList}.
	 *
	 * <p>
	 * It sets {@code item.metaModel = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addRightManagementListItem(RightManagement item) {
		if (rightManagementList == null) {
			rightManagementList = new ArrayList<RightManagement>();
		}
		rightManagementList.add(item);
		item.setMetaModel(this);
	}

	/**
	 * Remove the given {@link RightManagement} item from the {@code rightManagementList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeRightManagementListItem(RightManagement item) {
		if (rightManagementList == null) {
			return;
		}
		rightManagementList.remove(item);
	}

	/**
	 * Clear the {@code rightManagementList} collection.
	 *
	 * <p>
	 * If you have to query {@link RightManagement} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearRightManagementList() {
		if (rightManagementList != null) {
			rightManagementList.clear();
		}
	}

	public Boolean getCustomised() {
		return customised == null ? Boolean.FALSE : customised;
	}

	public void setCustomised(Boolean customised) {
		this.customised = customised;
	}

	public Boolean getEdited() {
		return edited == null ? Boolean.FALSE : edited;
	}

	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<MetaSequence> getMetaSequencList() {
		return metaSequencList;
	}

	public void setMetaSequencList(List<MetaSequence> metaSequencList) {
		this.metaSequencList = metaSequencList;
	}

	/**
	 * Add the given {@link MetaSequence} item to the {@code metaSequencList}.
	 *
	 * <p>
	 * It sets {@code item.metaModel = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addMetaSequencListItem(MetaSequence item) {
		if (metaSequencList == null) {
			metaSequencList = new ArrayList<MetaSequence>();
		}
		metaSequencList.add(item);
		item.setMetaModel(this);
	}

	/**
	 * Remove the given {@link MetaSequence} item from the {@code metaSequencList}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeMetaSequencListItem(MetaSequence item) {
		if (metaSequencList == null) {
			return;
		}
		metaSequencList.remove(item);
	}

	/**
	 * Clear the {@code metaSequencList} collection.
	 *
	 * <p>
	 * If you have to query {@link MetaSequence} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearMetaSequencList() {
		if (metaSequencList != null) {
			metaSequencList.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MetaModel)) return false;

		final MetaModel other = (MetaModel) obj;
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
		tsh.add("packageName", this.getPackageName());
		tsh.add("tableName", this.getTableName());
		tsh.add("fullName", this.getFullName());
		tsh.add("customised", this.getCustomised());
		tsh.add("edited", this.getEdited());
		tsh.add("title", this.getTitle());

		return tsh.omitNullValues().toString();
	}
}
