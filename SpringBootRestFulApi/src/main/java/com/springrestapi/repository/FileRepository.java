package com.springrestapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.springrestapi.model.FileMetaData;

@RepositoryRestResource
public interface FileRepository extends JpaRepository<FileMetaData, Integer> {

}
