package com.springrestapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.springrestapi.model.FileMetaData;

@RepositoryRestResource
public interface FileRepository extends JpaRepository<FileMetaData, Integer> {
	List<FileMetaData> findByName(@Param("name") String name);
}
