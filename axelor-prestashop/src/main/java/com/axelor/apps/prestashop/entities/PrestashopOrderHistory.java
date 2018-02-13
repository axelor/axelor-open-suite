package com.axelor.apps.prestashop.entities;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="order_history")
public class PrestashopOrderHistory extends PrestashopIdentifiableEntity {
	private Integer employeeId;
	private int orderStateId;
	private int orderId;
	private LocalDateTime addDate = LocalDateTime.now();

	@XmlElement(name="id_employee")
	public Integer getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	@XmlElement(name="id_order_state")
	public int getOrderStateId() {
		return orderStateId;
	}

	public void setOrderStateId(int orderStateId) {
		this.orderStateId = orderStateId;
	}

	@XmlElement(name="id_order")
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	@XmlElement(name="date_add")
	public LocalDateTime getAddDate() {
		return addDate;
	}

	public void setAddDate(LocalDateTime addDate) {
		this.addDate = addDate;
	}
}
