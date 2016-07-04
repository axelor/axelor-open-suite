package com.axelor.apps.message.db;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "MESSAGE_MESSAGE")
public class Message extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_MESSAGE_SEQ")
	@SequenceGenerator(name = "MESSAGE_MESSAGE_SEQ", sequenceName = "MESSAGE_MESSAGE_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Type", help = "true", readonly = true, selection = "message.type.select")
	private Integer typeSelect = 2;

	@Widget(title = "Subject")
	private String subject;

	@Widget(title = "Content")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String content;

	@Widget(title = "Sent date", help = "true", readonly = true)
	private LocalDateTime sentDateT;

	@Widget(title = "Forecasted Sent Date")
	private LocalDate sendScheduleDate;

	@Widget(title = "Related to", selection = "message.related.to.select")
	private String relatedTo1Select;

	private Integer relatedTo1SelectId = 0;

	@Widget(title = "Related to", selection = "message.related.to.select")
	private String relatedTo2Select;

	private Integer relatedTo2SelectId = 0;

	@Widget(title = "Status", readonly = true, selection = "message.status.select")
	private Integer statusSelect = 1;

	@Widget(title = "Media Type", help = "true", selection = "message.media.type.select")
	private Integer mediaTypeSelect = 0;

	@Widget(title = "Address Block", help = "true", multiline = true)
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String addressBlock;

	@Widget(title = "From")
	@Index(name = "MESSAGE_MESSAGE_FROM_EMAIL_ADDRESS_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private EmailAddress fromEmailAddress;

	@Widget(title = "Reply to")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<EmailAddress> replyToEmailAddressSet;

	@Widget(title = "To")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<EmailAddress> toEmailAddressSet;

	@Widget(title = "Cc", help = "true")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<EmailAddress> ccEmailAddressSet;

	@Widget(title = "Bcc", help = "true")
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<EmailAddress> bccEmailAddressSet;

	@Widget(title = "Sent by email")
	private Boolean sentByEmail = Boolean.FALSE;

	@Widget(title = "Mail account")
	@Index(name = "MESSAGE_MESSAGE_MAIL_ACCOUNT_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MailAccount mailAccount;

	@Widget(title = "Sender (User)", readonly = true)
	@Index(name = "MESSAGE_MESSAGE_SENDER_USER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User senderUser;

	@Widget(title = "Recipient")
	@Index(name = "MESSAGE_MESSAGE_RECIPIENT_USER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User recipientUser;

	public Message() {
	}

	public Message(Integer typeSelect, String subject, String content, Integer statusSelect, Integer mediaTypeSelect, String addressBlock, EmailAddress fromEmailAddress, Set<EmailAddress> replyToEmailAddressSet, Set<EmailAddress> toEmailAddressSet, Set<EmailAddress> ccEmailAddressSet, Set<EmailAddress> bccEmailAddressSet, Boolean sentByEmail, MailAccount mailAccount) {
		this.typeSelect = typeSelect;
		this.subject = subject;
		this.content = content;
		this.statusSelect = statusSelect;
		this.mediaTypeSelect = mediaTypeSelect;
		this.addressBlock = addressBlock;
		this.fromEmailAddress = fromEmailAddress;
		this.replyToEmailAddressSet = replyToEmailAddressSet;
		this.toEmailAddressSet = toEmailAddressSet;
		this.ccEmailAddressSet = ccEmailAddressSet;
		this.bccEmailAddressSet = bccEmailAddressSet;
		this.sentByEmail = sentByEmail;
		this.mailAccount = mailAccount;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Integer getTypeSelect() {
		return typeSelect == null ? 0 : typeSelect;
	}

	public void setTypeSelect(Integer typeSelect) {
		this.typeSelect = typeSelect;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public LocalDateTime getSentDateT() {
		return sentDateT;
	}

	public void setSentDateT(LocalDateTime sentDateT) {
		this.sentDateT = sentDateT;
	}

	public LocalDate getSendScheduleDate() {
		return sendScheduleDate;
	}

	public void setSendScheduleDate(LocalDate sendScheduleDate) {
		this.sendScheduleDate = sendScheduleDate;
	}

	public String getRelatedTo1Select() {
		return relatedTo1Select;
	}

	public void setRelatedTo1Select(String relatedTo1Select) {
		this.relatedTo1Select = relatedTo1Select;
	}

	public Integer getRelatedTo1SelectId() {
		return relatedTo1SelectId == null ? 0 : relatedTo1SelectId;
	}

	public void setRelatedTo1SelectId(Integer relatedTo1SelectId) {
		this.relatedTo1SelectId = relatedTo1SelectId;
	}

	public String getRelatedTo2Select() {
		return relatedTo2Select;
	}

	public void setRelatedTo2Select(String relatedTo2Select) {
		this.relatedTo2Select = relatedTo2Select;
	}

	public Integer getRelatedTo2SelectId() {
		return relatedTo2SelectId == null ? 0 : relatedTo2SelectId;
	}

	public void setRelatedTo2SelectId(Integer relatedTo2SelectId) {
		this.relatedTo2SelectId = relatedTo2SelectId;
	}

	public Integer getStatusSelect() {
		return statusSelect == null ? 0 : statusSelect;
	}

	public void setStatusSelect(Integer statusSelect) {
		this.statusSelect = statusSelect;
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

	public EmailAddress getFromEmailAddress() {
		return fromEmailAddress;
	}

	public void setFromEmailAddress(EmailAddress fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}

	public Set<EmailAddress> getReplyToEmailAddressSet() {
		return replyToEmailAddressSet;
	}

	public void setReplyToEmailAddressSet(Set<EmailAddress> replyToEmailAddressSet) {
		this.replyToEmailAddressSet = replyToEmailAddressSet;
	}

	/**
	 * Add the given {@link EmailAddress} item to the {@code replyToEmailAddressSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addReplyToEmailAddressSetItem(EmailAddress item) {
		if (replyToEmailAddressSet == null) {
			replyToEmailAddressSet = new HashSet<EmailAddress>();
		}
		replyToEmailAddressSet.add(item);
	}

	/**
	 * Remove the given {@link EmailAddress} item from the {@code replyToEmailAddressSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeReplyToEmailAddressSetItem(EmailAddress item) {
		if (replyToEmailAddressSet == null) {
			return;
		}
		replyToEmailAddressSet.remove(item);
	}

	/**
	 * Clear the {@code replyToEmailAddressSet} collection.
	 *
	 */
	public void clearReplyToEmailAddressSet() {
		if (replyToEmailAddressSet != null) {
			replyToEmailAddressSet.clear();
		}
	}

	public Set<EmailAddress> getToEmailAddressSet() {
		return toEmailAddressSet;
	}

	public void setToEmailAddressSet(Set<EmailAddress> toEmailAddressSet) {
		this.toEmailAddressSet = toEmailAddressSet;
	}

	/**
	 * Add the given {@link EmailAddress} item to the {@code toEmailAddressSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addToEmailAddressSetItem(EmailAddress item) {
		if (toEmailAddressSet == null) {
			toEmailAddressSet = new HashSet<EmailAddress>();
		}
		toEmailAddressSet.add(item);
	}

	/**
	 * Remove the given {@link EmailAddress} item from the {@code toEmailAddressSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeToEmailAddressSetItem(EmailAddress item) {
		if (toEmailAddressSet == null) {
			return;
		}
		toEmailAddressSet.remove(item);
	}

	/**
	 * Clear the {@code toEmailAddressSet} collection.
	 *
	 */
	public void clearToEmailAddressSet() {
		if (toEmailAddressSet != null) {
			toEmailAddressSet.clear();
		}
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Set<EmailAddress> getCcEmailAddressSet() {
		return ccEmailAddressSet;
	}

	public void setCcEmailAddressSet(Set<EmailAddress> ccEmailAddressSet) {
		this.ccEmailAddressSet = ccEmailAddressSet;
	}

	/**
	 * Add the given {@link EmailAddress} item to the {@code ccEmailAddressSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addCcEmailAddressSetItem(EmailAddress item) {
		if (ccEmailAddressSet == null) {
			ccEmailAddressSet = new HashSet<EmailAddress>();
		}
		ccEmailAddressSet.add(item);
	}

	/**
	 * Remove the given {@link EmailAddress} item from the {@code ccEmailAddressSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeCcEmailAddressSetItem(EmailAddress item) {
		if (ccEmailAddressSet == null) {
			return;
		}
		ccEmailAddressSet.remove(item);
	}

	/**
	 * Clear the {@code ccEmailAddressSet} collection.
	 *
	 */
	public void clearCcEmailAddressSet() {
		if (ccEmailAddressSet != null) {
			ccEmailAddressSet.clear();
		}
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Set<EmailAddress> getBccEmailAddressSet() {
		return bccEmailAddressSet;
	}

	public void setBccEmailAddressSet(Set<EmailAddress> bccEmailAddressSet) {
		this.bccEmailAddressSet = bccEmailAddressSet;
	}

	/**
	 * Add the given {@link EmailAddress} item to the {@code bccEmailAddressSet}.
	 *
	 * @param item
	 *            the item to add
	 */
	public void addBccEmailAddressSetItem(EmailAddress item) {
		if (bccEmailAddressSet == null) {
			bccEmailAddressSet = new HashSet<EmailAddress>();
		}
		bccEmailAddressSet.add(item);
	}

	/**
	 * Remove the given {@link EmailAddress} item from the {@code bccEmailAddressSet}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeBccEmailAddressSetItem(EmailAddress item) {
		if (bccEmailAddressSet == null) {
			return;
		}
		bccEmailAddressSet.remove(item);
	}

	/**
	 * Clear the {@code bccEmailAddressSet} collection.
	 *
	 */
	public void clearBccEmailAddressSet() {
		if (bccEmailAddressSet != null) {
			bccEmailAddressSet.clear();
		}
	}

	public Boolean getSentByEmail() {
		return sentByEmail == null ? Boolean.FALSE : sentByEmail;
	}

	public void setSentByEmail(Boolean sentByEmail) {
		this.sentByEmail = sentByEmail;
	}

	public MailAccount getMailAccount() {
		return mailAccount;
	}

	public void setMailAccount(MailAccount mailAccount) {
		this.mailAccount = mailAccount;
	}

	public User getSenderUser() {
		return senderUser;
	}

	public void setSenderUser(User senderUser) {
		this.senderUser = senderUser;
	}

	public User getRecipientUser() {
		return recipientUser;
	}

	public void setRecipientUser(User recipientUser) {
		this.recipientUser = recipientUser;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof Message)) return false;

		final Message other = (Message) obj;
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
		tsh.add("typeSelect", this.getTypeSelect());
		tsh.add("subject", this.getSubject());
		tsh.add("sentDateT", this.getSentDateT());
		tsh.add("sendScheduleDate", this.getSendScheduleDate());
		tsh.add("relatedTo1Select", this.getRelatedTo1Select());
		tsh.add("relatedTo1SelectId", this.getRelatedTo1SelectId());
		tsh.add("relatedTo2Select", this.getRelatedTo2Select());
		tsh.add("relatedTo2SelectId", this.getRelatedTo2SelectId());
		tsh.add("statusSelect", this.getStatusSelect());
		tsh.add("mediaTypeSelect", this.getMediaTypeSelect());
		tsh.add("sentByEmail", this.getSentByEmail());

		return tsh.omitNullValues().toString();
	}
}
