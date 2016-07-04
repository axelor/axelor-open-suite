package com.axelor.apps.message.db;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "MESSAGE_TEMPLATE")
public class Template extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_TEMPLATE_SEQ")
	@SequenceGenerator(name = "MESSAGE_TEMPLATE_SEQ", sequenceName = "MESSAGE_TEMPLATE_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NameColumn
	@NotNull
	@Index(name = "MESSAGE_TEMPLATE_NAME_IDX")
	private String name;

	@Widget(title = "Content", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String content;

	@Widget(title = "Suject", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String subject;

	@Widget(title = "From", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String fromAdress;

	@Widget(title = "Reply to", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String replyToRecipients;

	@Widget(title = "To", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String toRecipients;

	@Widget(title = "Cc", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String ccRecipients;

	@Widget(title = "Bcc", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String bccRecipients;

	@Widget(title = "Target receptor")
	private String target;

	@Widget(title = "Media Type", help = "true", selection = "message.media.type.select")
	@NotNull
	private Integer mediaTypeSelect = 0;

	@Widget(title = "Address Block", help = "true", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String addressBlock;

	@Widget(title = "Model", help = "true")
	@Index(name = "MESSAGE_TEMPLATE_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "Default ?", help = "true")
	private Boolean isDefault = Boolean.FALSE;

	@Widget(title = "System ?", help = "true")
	private Boolean isSystem = Boolean.FALSE;

	public Template() {
	}

	public Template(String name) {
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getFromAdress() {
		return fromAdress;
	}

	public void setFromAdress(String fromAdress) {
		this.fromAdress = fromAdress;
	}

	public String getReplyToRecipients() {
		return replyToRecipients;
	}

	public void setReplyToRecipients(String replyToRecipients) {
		this.replyToRecipients = replyToRecipients;
	}

	public String getToRecipients() {
		return toRecipients;
	}

	public void setToRecipients(String toRecipients) {
		this.toRecipients = toRecipients;
	}

	public String getCcRecipients() {
		return ccRecipients;
	}

	public void setCcRecipients(String ccRecipients) {
		this.ccRecipients = ccRecipients;
	}

	public String getBccRecipients() {
		return bccRecipients;
	}

	public void setBccRecipients(String bccRecipients) {
		this.bccRecipients = bccRecipients;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Integer getMediaTypeSelect() {
		return mediaTypeSelect == null ? 0 : mediaTypeSelect;
	}

	public void setMediaTypeSelect(Integer mediaTypeSelect) {
		this.mediaTypeSelect = mediaTypeSelect;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public String getAddressBlock() {
		return addressBlock;
	}

	public void setAddressBlock(String addressBlock) {
		this.addressBlock = addressBlock;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Boolean getIsDefault() {
		return isDefault == null ? Boolean.FALSE : isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Boolean getIsSystem() {
		return isSystem == null ? Boolean.FALSE : isSystem;
	}

	public void setIsSystem(Boolean isSystem) {
		this.isSystem = isSystem;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Template)) return false;

		final Template other = (Template) obj;
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
		tsh.add("target", this.getTarget());
		tsh.add("mediaTypeSelect", this.getMediaTypeSelect());
		tsh.add("isDefault", this.getIsDefault());
		tsh.add("isSystem", this.getIsSystem());

		return tsh.omitNullValues().toString();
	}
}
