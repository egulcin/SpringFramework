package com.springrestapi.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springrestapi.model.FileMetaData;
import com.springrestapi.repository.FileRepository;
import com.springrestapi.util.FileUploadUtil;

@RestController
public class FileController {

	@Autowired
	private FileRepository repo;

	@Autowired
	private FileUploadUtil fileUploadUtil;

	/**
	 * This method is to fetch the meta-data of all uploaded files from the
	 * database
	 *
	 * @return
	 */
	@RequestMapping(value = "/uploadedfiles", method = RequestMethod.GET)
	public List<FileMetaData> findFiles() {
		return repo.findAll();
	}

	/**
	 * API to get file meta-data
	 *
	 * This method is to get FileMetadaData object with the given 'id' from the
	 * database
	 *
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/filemetadata/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getFileMetaData(@PathVariable Integer id) {
		FileMetaData fileMetaData = repo.findOne(id);

		if (fileMetaData != null) {
			try {
				Path path = Paths.get(fileMetaData.getPath());
				BasicFileAttributes attr = Files.readAttributes(path,
						BasicFileAttributes.class);
				if (attr != null) {
					Date lastUpdatedDate = null;
					Date lastAccessDate = null;
					if (attr.lastModifiedTime() != null) {
						lastUpdatedDate = new Date(attr.lastModifiedTime()
								.toMillis());
					}
					if (attr.lastAccessTime() != null) {
						lastAccessDate = new Date(attr.lastAccessTime()
								.toMillis());
					}
					fileMetaData.setLastUpdatedDate(lastUpdatedDate);
					fileMetaData.setLastAccessDate(lastAccessDate);
					fileMetaData.setDirectory(attr.isDirectory());
					fileMetaData.setOther(attr.isOther());
					fileMetaData.setRegularFile(attr.isRegularFile());
					fileMetaData.setSymbolicLink(attr.isSymbolicLink());
					fileMetaData.setSize(attr.size());
					repo.saveAndFlush(fileMetaData);

					return new ResponseEntity<FileMetaData>(fileMetaData,
							HttpStatus.OK);
				}

			} catch (IOException e) {
				return new ResponseEntity<Object>(
						"IOException: Get file meta-data process has failed.",
						HttpStatus.BAD_REQUEST);
			}

			catch (Exception e) {
				return new ResponseEntity<Object>(
						"Exception: Request has failed.",
						HttpStatus.BAD_REQUEST);
			}
		}

		return new ResponseEntity<Object>("No FileMetaData found with id: "
				+ id, HttpStatus.BAD_REQUEST);
	}

	/**
	 * API to upload a file with a few meta-data fields. Persist meta-data in
	 * persistence store (In memory DB or file system and store the content on a
	 * file system).
	 *
	 *
	 * This method is to write the uploaded file content onto the disk and to
	 * save the meta-data of the file to the database
	 *
	 * @param file
	 * @param name
	 *            : Name has to include the file extension. Otherwise we will
	 *            get trouble while generating the full path later.
	 * @param descr
	 * @param saveToPath
	 * @return
	 */
	@RequestMapping(value = "/uploadfile", method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<?> uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "descr", required = false) String descr,
			@RequestParam(value = "saveToPath", required = false) String saveToPath) {

		if (file.isEmpty()) {
			return new ResponseEntity<Object>(
					"Please select a file to upload.", HttpStatus.BAD_REQUEST);
		}

		try {
			/**
			 * Write file content onto the disk
			 */

			String fileName = fileUploadUtil.assignFileName(name,
					file.getOriginalFilename());
			if (!StringUtils.isEmpty(fileName)) {
				List<FileMetaData> metaDataList = repo.findByName(name);
				if (metaDataList != null && metaDataList.size() > 0) {
					return new ResponseEntity<Object>(
							"Duplicate file name - FileMetaData found for the given name: "
									+ name, HttpStatus.BAD_REQUEST);
				}
			}

			String fullFilePath = fileUploadUtil.saveFileToDisk(file, name,
					saveToPath);

			/**
			 * Save the meta-data in database
			 */
			FileMetaData fileMetaData = fileUploadUtil.createFileMetaDataFrom(
					name, descr, file, fullFilePath);
			repo.saveAndFlush(fileMetaData);
		}

		catch (NoSuchFileException e) {
			return new ResponseEntity<Object>(
					"NoSuchFileException: File upload process has failed.",
					HttpStatus.BAD_REQUEST);
		}

		catch (IOException e) {
			return new ResponseEntity<Object>(
					"IOException: File upload process has failed.",
					HttpStatus.BAD_REQUEST);
		}

		catch (SQLException e) {
			return new ResponseEntity<Object>(
					"SQLException: File upload process has failed.",
					HttpStatus.BAD_REQUEST);
		}

		catch (Exception e) {
			return new ResponseEntity<Object>(
					"Exception: File upload process has failed.",
					HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<Object>("File successfully uploaded.",
				new HttpHeaders(), HttpStatus.OK);
	}

	/**
	 * API to download content stream (Optional)
	 *
	 * This method is to download the content of a file whose meta-data is saved
	 * in our database.
	 *
	 * @param id
	 *            : We will use this id to find the meta-data of the file in our
	 *            database. From there, we will get the path to the file and
	 *            download it.
	 * @return
	 */
	@RequestMapping(value = "/downloadfile/{id}", method = RequestMethod.GET, produces = {
			"text/plain", "application/pdf", "image/jpeg", "image/png" })
	public ResponseEntity<?> downloadFile(@PathVariable("id") Integer id) {
		/**
		 * Get file meta-data from the database using the 'id'
		 */
		FileMetaData fileMetaData = repo.findOne(id);
		if (fileMetaData != null) {
			try {
				/**
				 * Create a File object.
				 */
				File file = new File(fileMetaData.getPath());

				HttpHeaders headers = new HttpHeaders();
				/**
				 * We keep the content types of the uploaded files in our
				 * database to be able to use them in download process.
				 */
				headers.setContentType(MediaType.parseMediaType(fileMetaData
						.getContentType()));
				headers.add("Access-Control-Allow-Origin", "*");
				headers.add("Access-Control-Allow-Methods", "GET, POST, PUT");
				headers.add("Access-Control-Allow-Headers", "Content-Type");
				/**
				 * file.getName() will give us the name of the file with its
				 * extension Example: file_name.txt or file_name.docx
				 */
				headers.add("Content-Disposition", "filename=" + file.getName());
				headers.add("Cache-Control",
						"no-cache, no-store, must-revalidate");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");

				headers.setContentLength(file.length());
				ResponseEntity<InputStreamResource> response = new ResponseEntity<InputStreamResource>(
						new InputStreamResource(new FileInputStream(file)),
						headers, HttpStatus.OK);
				return response;
			} catch (IOException e) {
				return new ResponseEntity<Object>(
						"IOException: File not found with path: "
								+ fileMetaData.getPath(),
						HttpStatus.BAD_REQUEST);
			}

			catch (Exception e) {
				return new ResponseEntity<Object>(
						"Exception: Download file process has failed",
						HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<Object>("File not found with id: " + id,
				HttpStatus.BAD_REQUEST);
	}

}
