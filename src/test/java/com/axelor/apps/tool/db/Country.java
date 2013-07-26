package com.axelor.apps.tool.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

@Entity
@Table(name = "CONTACT_COUNTRY")
public class Country extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_COUNTRY_SEQ")
	@SequenceGenerator(name = "CONTACT_COUNTRY_SEQ", sequenceName = "CONTACT_COUNTRY_SEQ", allocationSize = 1)
	private Long id;
	
	@NotNull
	private String code;

	@NotNull
	private String name;

	public Country() {
	}

	public Country(String name, String code) {
		this.name = name;
		this.code = code;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	@Override
	public String toString() {
		ToStringHelper tsh = Objects.toStringHelper(getClass());
		
		tsh.add("id", getId());
		tsh.add("code", code);
		tsh.add("name", name);
		
		return tsh.omitNullValues().toString();
	}
	
	public static Query<Country> all() {
		return JPA.all(Country.class);
	}
	
}
