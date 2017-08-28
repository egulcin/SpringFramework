package com.springrestapi.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This entity represents the table used to save the meta-data of the uploaded
 * files. Contents of the uploaded files will not be saved on the database since
 * we are using an in-memory database. Content can be saved into the database
 * using a byte array in case of having a more sophisticated database than
 * HSQLDB.
 *
 * The contents will be located on the disk location indicated by the 'path'
 * field in this entity.
 *
 *
 * @author oymakeg
 *
 */
@Entity
public class FileMetaData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column
	private String name;

	@Column
	private String originalName;

	@Column
	private String description;

	@Column
	private String path;

	@Column
	private Date createdDate;

	@Column
	private String contentType;

	@Column
	private Long size;

	@Column
	private Date lastUpdatedDate;

	@Column
	private Date lastAccessDate;

	@Column
	private boolean directory;

	@Column
	private boolean other;

	@Column
	private boolean regularFile;

	@Column
	private boolean symbolicLink;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public Date getLastAccessDate() {
		return lastAccessDate;
	}

	public void setLastAccessDate(Date lastAccessDate) {
		this.lastAccessDate = lastAccessDate;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public boolean isOther() {
		return other;
	}

	public void setOther(boolean other) {
		this.other = other;
	}

	public boolean isRegularFile() {
		return regularFile;
	}

	public void setRegularFile(boolean regularFile) {
		this.regularFile = regularFile;
	}

	public boolean isSymbolicLink() {
		return symbolicLink;
	}

	public void setSymbolicLink(boolean symbolicLink) {
		this.symbolicLink = symbolicLink;
	}

}
