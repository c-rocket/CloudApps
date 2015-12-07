package com.oracle.iot.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OracleDocsFolder {
	String name;
	List<OracleDocsItem> items;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<OracleDocsItem> getItems() {
		return items;
	}

	public void setItems(List<OracleDocsItem> items) {
		this.items = items;
	}

}
