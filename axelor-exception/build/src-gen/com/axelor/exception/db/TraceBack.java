package com.axelor.exception.db;

import java.util.Objects;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.VirtualColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "EXCEPTION_TRACE_BACK")
public class TraceBack extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EXCEPTION_TRACE_BACK_SEQ")
	@SequenceGenerator(name = "EXCEPTION_TRACE_BACK_SEQ", sequenceName = "EXCEPTION_TRACE_BACK_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Anomaly")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	@NotNull
	private String exception;

	@Widget(title = "Type", selection = "trace.back.type.select")
	private Integer typeSelect = 0;

	@Widget(title = "Category", selection = "trace.back.category.select")
	private Integer categorySelect = 0;

	@Widget(title = "Origin", selection = "trace.back.origin.select")
	private String origin;

	@Widget(title = "Date")
	@NotNull
	@Column(name = "date_val")
	private DateTime date;

	@Widget(title = "User")
	@Index(name = "EXCEPTION_TRACE_BACK_INTERNAL_USER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User internalUser;

	@Widget(title = "Error")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String error;

	@Widget(title = "Cause")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String cause;

	@Widget(title = "Message")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String message;

	@Widget(title = "Trace")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String trace;

	@Widget(title = "Batch")
	private Long batchId = 0L;

	@Widget(search = { "id", "date" })
	@NameColumn
	@VirtualColumn
	@Access(AccessType.PROPERTY)
	@Index(name = "EXCEPTION_TRACE_BACK_NAME_IDX")
	private String name;

	public TraceBack() {
	}

	public TraceBack(String name) {
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

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	public Integer getTypeSelect() {
		return typeSelect == null ? 0 : typeSelect;
	}

	public void setTypeSelect(Integer typeSelect) {
		this.typeSelect = typeSelect;
	}

	public Integer getCategorySelect() {
		return categorySelect == null ? 0 : categorySelect;
	}

	public void setCategorySelect(Integer categorySelect) {
		this.categorySelect = categorySelect;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public User getInternalUser() {
		return internalUser;
	}

	public void setInternalUser(User internalUser) {
		this.internalUser = internalUser;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	public Long getBatchId() {
		return batchId == null ? 0L : batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public String getName() {
		try {
			name = computeName();
		} catch (NullPointerException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("NPE in function field: getName()", e);
		}
		return name;
	}

	protected String computeName() {
		return this.id + " : " + this.date;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof TraceBack)) return false;

		final TraceBack other = (TraceBack) obj;
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
		tsh.add("categorySelect", this.getCategorySelect());
		tsh.add("origin", this.getOrigin());
		tsh.add("date", this.getDate());
		tsh.add("batchId", this.getBatchId());

		return tsh.omitNullValues().toString();
	}
}
