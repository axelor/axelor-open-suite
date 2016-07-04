package com.axelor.studio.db;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

/**
 * This object store workflow realted tracking information for an object.

 * It will store information like number of time some status changed, or changed by which user with date and time.
 * Entry will be created on onSave of related object.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_WKF_TRACKING")
public class WkfTracking extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_WKF_TRACKING_SEQ")
	@SequenceGenerator(name = "STUDIO_WKF_TRACKING_SEQ", sequenceName = "STUDIO_WKF_TRACKING_SEQ", allocationSize = 1)
	private Long id;

	@NotNull
	@Index(name = "STUDIO_WKF_TRACKING_WKF_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Wkf wkf;

	@Widget(title = "Record model")
	@NotNull
	private String recordModel;

	@Widget(title = "Record Id")
	@NotNull
	private Integer recordId = 0;

	@Widget(title = "Record name")
	private String recordName;

	@Widget(title = "Tracking lines")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkfTracking", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WkfTrackingLine> wkfTrackingLines;

	@Widget(title = "Tracking total")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkfTracking", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WkfTrackingTotal> totalLines;

	@Widget(title = "Tracking time")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "wkfTracking", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<WkfTrackingTime> totalTimeLines;

	public WkfTracking() {
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Wkf getWkf() {
		return wkf;
	}

	public void setWkf(Wkf wkf) {
		this.wkf = wkf;
	}

	public String getRecordModel() {
		return recordModel;
	}

	public void setRecordModel(String recordModel) {
		this.recordModel = recordModel;
	}

	public Integer getRecordId() {
		return recordId == null ? 0 : recordId;
	}

	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}

	public String getRecordName() {
		return recordName;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	public List<WkfTrackingLine> getWkfTrackingLines() {
		return wkfTrackingLines;
	}

	public void setWkfTrackingLines(List<WkfTrackingLine> wkfTrackingLines) {
		this.wkfTrackingLines = wkfTrackingLines;
	}

	/**
	 * Add the given {@link WkfTrackingLine} item to the {@code wkfTrackingLines}.
	 *
	 * <p>
	 * It sets {@code item.wkfTracking = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addWkfTrackingLine(WkfTrackingLine item) {
		if (wkfTrackingLines == null) {
			wkfTrackingLines = new ArrayList<WkfTrackingLine>();
		}
		wkfTrackingLines.add(item);
		item.setWkfTracking(this);
	}

	/**
	 * Remove the given {@link WkfTrackingLine} item from the {@code wkfTrackingLines}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeWkfTrackingLine(WkfTrackingLine item) {
		if (wkfTrackingLines == null) {
			return;
		}
		wkfTrackingLines.remove(item);
	}

	/**
	 * Clear the {@code wkfTrackingLines} collection.
	 *
	 * <p>
	 * If you have to query {@link WkfTrackingLine} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearWkfTrackingLines() {
		if (wkfTrackingLines != null) {
			wkfTrackingLines.clear();
		}
	}

	public List<WkfTrackingTotal> getTotalLines() {
		return totalLines;
	}

	public void setTotalLines(List<WkfTrackingTotal> totalLines) {
		this.totalLines = totalLines;
	}

	/**
	 * Add the given {@link WkfTrackingTotal} item to the {@code totalLines}.
	 *
	 * <p>
	 * It sets {@code item.wkfTracking = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addTotalLine(WkfTrackingTotal item) {
		if (totalLines == null) {
			totalLines = new ArrayList<WkfTrackingTotal>();
		}
		totalLines.add(item);
		item.setWkfTracking(this);
	}

	/**
	 * Remove the given {@link WkfTrackingTotal} item from the {@code totalLines}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeTotalLine(WkfTrackingTotal item) {
		if (totalLines == null) {
			return;
		}
		totalLines.remove(item);
	}

	/**
	 * Clear the {@code totalLines} collection.
	 *
	 * <p>
	 * If you have to query {@link WkfTrackingTotal} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearTotalLines() {
		if (totalLines != null) {
			totalLines.clear();
		}
	}

	public List<WkfTrackingTime> getTotalTimeLines() {
		return totalTimeLines;
	}

	public void setTotalTimeLines(List<WkfTrackingTime> totalTimeLines) {
		this.totalTimeLines = totalTimeLines;
	}

	/**
	 * Add the given {@link WkfTrackingTime} item to the {@code totalTimeLines}.
	 *
	 * <p>
	 * It sets {@code item.wkfTracking = this} to ensure the proper relationship.
	 * </p>
	 *
	 * @param item
	 *            the item to add
	 */
	public void addTotalTimeLine(WkfTrackingTime item) {
		if (totalTimeLines == null) {
			totalTimeLines = new ArrayList<WkfTrackingTime>();
		}
		totalTimeLines.add(item);
		item.setWkfTracking(this);
	}

	/**
	 * Remove the given {@link WkfTrackingTime} item from the {@code totalTimeLines}.
	 *
 	 * @param item
	 *            the item to remove
	 */
	public void removeTotalTimeLine(WkfTrackingTime item) {
		if (totalTimeLines == null) {
			return;
		}
		totalTimeLines.remove(item);
	}

	/**
	 * Clear the {@code totalTimeLines} collection.
	 *
	 * <p>
	 * If you have to query {@link WkfTrackingTime} records in same transaction, make
	 * sure to call {@link javax.persistence.EntityManager#flush() } to avoid
	 * unexpected errors.
	 * </p>
	 */
	public void clearTotalTimeLines() {
		if (totalTimeLines != null) {
			totalTimeLines.clear();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof WkfTracking)) return false;

		final WkfTracking other = (WkfTracking) obj;
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
		tsh.add("recordModel", this.getRecordModel());
		tsh.add("recordId", this.getRecordId());
		tsh.add("recordName", this.getRecordName());

		return tsh.omitNullValues().toString();
	}
}
