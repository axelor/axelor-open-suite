package com.axelor.apps.message.db;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.NameColumn;
import com.axelor.db.annotations.Widget;
import com.google.common.base.MoreObjects;

@Entity
@Table(name = "MESSAGE_MAIL_ACCOUNT")
public class MailAccount extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MESSAGE_MAIL_ACCOUNT_SEQ")
	@SequenceGenerator(name = "MESSAGE_MAIL_ACCOUNT_SEQ", sequenceName = "MESSAGE_MAIL_ACCOUNT_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Name")
	@NameColumn
	@NotNull
	@Index(name = "MESSAGE_MAIL_ACCOUNT_NAME_IDX")
	private String name;

	@Widget(title = "Server Type", help = "true", selection = "mail.account.server.type.select")
	@NotNull
	private Integer serverTypeSelect = 1;

	@Widget(title = "Login", help = "true")
	private String login;

	@Widget(title = "Password")
	private String password;

	@Widget(title = "Host", help = "true")
	@NotNull
	private String host;

	@Widget(title = "Port")
	@Min(1)
	private Integer port;

	@Widget(title = "SSL/STARTTLS", help = "true", selection = "mail.account.security.select")
	private Integer securitySelect = 0;

	@Widget(title = "Default account ?", help = "true")
	private Boolean isDefault = Boolean.FALSE;

	@Widget(title = "Valid ?", help = "true")
	private Boolean isValid = Boolean.FALSE;

	@Widget(title = "Signature")
	private String signature;

	public MailAccount() {
	}

	public MailAccount(String name) {
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

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Integer getServerTypeSelect() {
		return serverTypeSelect == null ? 0 : serverTypeSelect;
	}

	public void setServerTypeSelect(Integer serverTypeSelect) {
		this.serverTypeSelect = serverTypeSelect;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public Integer getSecuritySelect() {
		return securitySelect == null ? 0 : securitySelect;
	}

	public void setSecuritySelect(Integer securitySelect) {
		this.securitySelect = securitySelect;
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
	public Boolean getIsValid() {
		return isValid == null ? Boolean.FALSE : isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof MailAccount)) return false;

		final MailAccount other = (MailAccount) obj;
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
		tsh.add("serverTypeSelect", this.getServerTypeSelect());
		tsh.add("login", this.getLogin());
		tsh.add("password", this.getPassword());
		tsh.add("host", this.getHost());
		tsh.add("port", this.getPort());
		tsh.add("securitySelect", this.getSecuritySelect());
		tsh.add("isDefault", this.getIsDefault());
		tsh.add("isValid", this.getIsValid());
		tsh.add("signature", this.getSignature());

		return tsh.omitNullValues().toString();
	}
}
