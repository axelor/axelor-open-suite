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
@Table(name = "CONTACT_GROUP")
public class Group extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTACT_GROUP_SEQ")
	@SequenceGenerator(name = "CONTACT_GROUP_SEQ", sequenceName = "CONTACT_GROUP_SEQ", allocationSize = 1)
	private Long id;
	
	@NotNull
	private String name;

	@NotNull
	private String title;

	public Group() {
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Group(String name, String title) {
		this.name = name;
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		ToStringHelper tsh = Objects.toStringHelper(getClass());
		
		tsh.add("id", getId());
		tsh.add("name", getName());
		tsh.add("title", getTitle());
		
		return tsh.omitNullValues().toString();
	}
	
	public static Query<Group> all() {
		return JPA.all(Group.class);
	}

}
