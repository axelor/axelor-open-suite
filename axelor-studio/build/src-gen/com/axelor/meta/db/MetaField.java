package com.axelor.meta.db;

import java.math.BigDecimal;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.studio.db.RightManagement;
import com.google.common.base.MoreObjects;

/**
 * This object store the fields.
 */
@Entity
@Cacheable
@Table(name = "META_FIELD", uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "meta_model" }) })
public class MetaField extends AuditableModel {

	@Index(name = "META_FIELD_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "Name")
	@NotNull
	@Index(name = "META_FIELD_NAME_IDX")
	private String name;

	@Widget(title = "Package")
	private String packageName;

	@Widget(title = "Type")
	@NotNull
	private String typeName;

	@Widget(title = "Label")
	private String label;

	@Widget(title = "Relationship", selection = "relationship.field.selection")
	private String relationship;

	@Widget(title = "Mapped by")
	private String mappedBy;

	@Widget(title = "Description")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String description;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "META_FIELD_SEQ")
	@SequenceGenerator(name = "META_FIELD_SEQ", sequenceName = "META_FIELD_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Right management")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "metaField", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RightManagement> rightManagementList;

	@Widget(title = "Type", selection = "field.type.selection")
	private String fieldType;

	@Widget(title = "Default")
	private Boolean defaultBoolean;

	@Widget(title = "Default")
	private BigDecimal defaultDecimal;

	@Widget(title = "Min")
	private BigDecimal decimalMin;

	@Widget(title = "Max")
	private BigDecimal decimalMax;

	@Widget(title = "Default")
	private Integer defaultInteger;

	@Widget(title = "Min")
	private Integer integerMin;

	@Widget(title = "Max")
	private Integer integerMax;

	@Widget(title = "Default")
	private String defaultString;

	@Widget(title = "Large")
	private Boolean large = Boolean.FALSE;

	@Widget(title = "Selection")
	@Index(name = "META_FIELD_META_SELECT_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaSelect metaSelect;

	@Widget(title = "Reference Model")
	@Index(name = "META_FIELD_META_MODEL_REF_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModelRef;

	private Boolean readonly = Boolean.FALSE;

	private Boolean hidden = Boolean.FALSE;

	private Boolean required = Boolean.FALSE;

	private Boolean multiselect = Boolean.FALSE;

	@Widget(title = "Sequence")
	private Integer sequence = 0;

	private Boolean customised = Boolean.FALSE;

	@Widget(title = "Duration ?")
	private Boolean isDuration = Boolean.FALSE;

	@Widget(title = "URL ?")
	private Boolean isUrl = Boolean.FALSE;

	@Widget(title = "Help")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String helpText;

	@Widget(title = "Track")
	private Boolean track = Boolean.FALSE;

	@Widget(title = "Name column")
	private Boolean nameColumn = Boolean.FALSE;

	@Widget(title = "Meta sequence")
	private String metaSequence;

	public MetaField() {
	}

	public MetaField(String name, Boolean nameColumn) {
		this.name = name;
		this.nameColumn = nameColumn;
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
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

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public String getMappedBy() {
		return mappedBy;
	}

	public void setMappedBy(String mappedBy) {
		this.mappedBy = mappedBy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
	 * It sets {@code item.metaField = this} to ensure the proper relationship.
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
		item.setMetaField(this);
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

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public Boolean getDefaultBoolean() {
		return defaultBoolean;
	}

	public void setDefaultBoolean(Boolean defaultBoolean) {
		this.defaultBoolean = defaultBoolean;
	}

	public BigDecimal getDefaultDecimal() {
		return defaultDecimal;
	}

	public void setDefaultDecimal(BigDecimal defaultDecimal) {
		this.defaultDecimal = defaultDecimal;
	}

	public BigDecimal getDecimalMin() {
		return decimalMin;
	}

	public void setDecimalMin(BigDecimal decimalMin) {
		this.decimalMin = decimalMin;
	}

	public BigDecimal getDecimalMax() {
		return decimalMax;
	}

	public void setDecimalMax(BigDecimal decimalMax) {
		this.decimalMax = decimalMax;
	}

	public Integer getDefaultInteger() {
		return defaultInteger;
	}

	public void setDefaultInteger(Integer defaultInteger) {
		this.defaultInteger = defaultInteger;
	}

	public Integer getIntegerMin() {
		return integerMin;
	}

	public void setIntegerMin(Integer integerMin) {
		this.integerMin = integerMin;
	}

	public Integer getIntegerMax() {
		return integerMax;
	}

	public void setIntegerMax(Integer integerMax) {
		this.integerMax = integerMax;
	}

	public String getDefaultString() {
		return defaultString;
	}

	public void setDefaultString(String defaultString) {
		this.defaultString = defaultString;
	}

	public Boolean getLarge() {
		return large == null ? Boolean.FALSE : large;
	}

	public void setLarge(Boolean large) {
		this.large = large;
	}

	public MetaSelect getMetaSelect() {
		return metaSelect;
	}

	public void setMetaSelect(MetaSelect metaSelect) {
		this.metaSelect = metaSelect;
	}

	public MetaModel getMetaModelRef() {
		return metaModelRef;
	}

	public void setMetaModelRef(MetaModel metaModelRef) {
		this.metaModelRef = metaModelRef;
	}

	public Boolean getReadonly() {
		return readonly == null ? Boolean.FALSE : readonly;
	}

	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	public Boolean getHidden() {
		return hidden == null ? Boolean.FALSE : hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getRequired() {
		return required == null ? Boolean.FALSE : required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getMultiselect() {
		return multiselect == null ? Boolean.FALSE : multiselect;
	}

	public void setMultiselect(Boolean multiselect) {
		this.multiselect = multiselect;
	}

	public Integer getSequence() {
		return sequence == null ? 0 : sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public Boolean getCustomised() {
		return customised == null ? Boolean.FALSE : customised;
	}

	public void setCustomised(Boolean customised) {
		this.customised = customised;
	}

	public Boolean getIsDuration() {
		return isDuration == null ? Boolean.FALSE : isDuration;
	}

	public void setIsDuration(Boolean isDuration) {
		this.isDuration = isDuration;
	}

	public Boolean getIsUrl() {
		return isUrl == null ? Boolean.FALSE : isUrl;
	}

	public void setIsUrl(Boolean isUrl) {
		this.isUrl = isUrl;
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public Boolean getTrack() {
		return track == null ? Boolean.FALSE : track;
	}

	public void setTrack(Boolean track) {
		this.track = track;
	}

	public Boolean getNameColumn() {
		return nameColumn == null ? Boolean.FALSE : nameColumn;
	}

	public void setNameColumn(Boolean nameColumn) {
		this.nameColumn = nameColumn;
	}

	public String getMetaSequence() {
		return metaSequence;
	}

	public void setMetaSequence(String metaSequence) {
		this.metaSequence = metaSequence;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MetaField)) return false;

		final MetaField other = (MetaField) obj;
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
		tsh.add("typeName", this.getTypeName());
		tsh.add("label", this.getLabel());
		tsh.add("relationship", this.getRelationship());
		tsh.add("mappedBy", this.getMappedBy());
		tsh.add("fieldType", this.getFieldType());
		tsh.add("defaultBoolean", this.getDefaultBoolean());
		tsh.add("defaultDecimal", this.getDefaultDecimal());
		tsh.add("decimalMin", this.getDecimalMin());
		tsh.add("decimalMax", this.getDecimalMax());

		return tsh.omitNullValues().toString();
	}
}
