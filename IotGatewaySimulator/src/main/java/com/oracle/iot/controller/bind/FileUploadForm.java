package com.oracle.iot.controller.bind;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadForm {
	private Boolean share;
	private String industry;
	private List<MultipartFile> files;

	public List<MultipartFile> getFiles() {
		return files;
	}

	public void setFiles(List<MultipartFile> files) {
		this.files = files;
	}

	public Boolean getShare() {
		return share;
	}

	public void setShare(Boolean share) {
		this.share = share;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

}
