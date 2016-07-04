package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * It store number of time(count) particular status used for an object.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_WKF_TRACKING_TOTAL")
public class WkfTrackingTotal extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_WKF_TRACKING_TOTAL_SEQ")
	@SequenceGenerator(name = "STUDIO_WKF_TRACKING_TOTAL_SEQ", sequenceName = "STUDIO_WKF_TRACKING_TOTAL_SEQ", allocationSize = 1)
	private Long id;

	@NotNull
	@Index(name = "STUDIO_WKF_TRACKING_TOTAL_WKF_TRACKING_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private WkfTracking wkfTracking;

	@Widget(title = "Status")
	@NotNull
	private String status;

	@Widget(title = "Nb of times")
	private Integer totalCount = 0;

	public WkfTrackingTotal() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public WkfTracking getWkfTracking() {
		return wkfTracking;
	}

	public void setWkfTracking(WkfTracking wkfTracking) {
		this.wkfTracking = wkfTracking;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTotalCount() {
		return totalCount == null ? 0 : totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WkfTrackingTotal)) return false;

		final WkfTrackingTotal other = (WkfTrackingTotal) obj;
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
		tsh.add("status", this.getStatus());
		tsh.add("totalCount", this.getTotalCount());

		return tsh.omitNullValues().toString();
	}
}
