package com.springrestapi.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.springrestapi.model.FileMetaData;

/**
 *
 * @author oymakeg
 *
 */
@Component
public class FileUploadUtil {

	/**
	 * Uploaded file will be saved in this folder
	 */
	private static String DEFAULT_UPLOADED_FOLDER = "C://Upload//";

	/**
	 * This method saves the file content to the disk.
	 *
	 * @param file
	 *            : File that will be saved on the disk
	 * @param name
	 *            : This name (if not blank) will be assigned to the file. It
	 *            has to include the file extension as well.
	 * @param toPath
	 *            : This is the path where the file will be saved to. If it's
	 *            blank, then the DEFAULT_UPLOADED_FOLDER will be used as the
	 *            path. We will assume that the user has to enter the path in
	 *            the pattern of "...//...//"
	 * @param file
	 * @param name
	 * @param toPath
	 *
	 * @return : The full path of the file "...//...//file_name.extension"
	 *
	 * @throws NoSuchFileException
	 * @throws IOException
	 */
	public String saveFileToDisk(MultipartFile file, String name, String toPath)
			throws NoSuchFileException, IOException {

		if (!file.isEmpty()) {
			byte[] bytes = file.getBytes();

			/**
			 * Assign the file name. If 'name' is blank, then use the original
			 * file name. If not, use the given 'name' to rename the file.
			 */
			String fileName = assignFileName(name, file.getOriginalFilename());

			/**
			 * Assign the file path. If toPath is not blank, use it as path. If
			 * toPath is blank, use the DEFAULT_UPLOADED_FOLDER as path
			 */
			String filePath = assignFilePath(toPath, fileName);

			Path path = Paths.get(filePath);
			Files.write(path, bytes);
			return filePath;
		}

		return null;
	}

	/**
	 * This method generates an FileMetaData entity instance using the given
	 * parameters. This instance will be saved to the in-memory database later.
	 *
	 * @param name
	 *            : Name of the file. This name has to include the extension
	 *            inside
	 * @param descr
	 *            : Description for the file
	 * @param file
	 *            : Actual file (This will be used to get the 'originalFileName'
	 *            and 'contentType')
	 * @param toPath
	 *            : The path where the file will be located in the disk
	 *
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public FileMetaData createFileMetaDataFrom(String name, String descr,
			MultipartFile multipartFile, String toPath) throws IOException,
			SQLException {

		String fileName = assignFileName(name,
				multipartFile.getOriginalFilename());

		Path path = Paths.get(toPath);
		BasicFileAttributes attr = Files.readAttributes(path,
				BasicFileAttributes.class);

		FileMetaData fileMetaData = new FileMetaData();
		fileMetaData.setName(fileName);
		fileMetaData.setDescription(descr);
		fileMetaData.setContentType(multipartFile.getContentType());
		fileMetaData.setCreatedDate(Calendar.getInstance().getTime());
		fileMetaData.setPath(toPath);
		fileMetaData.setOriginalName(multipartFile.getOriginalFilename());
		fileMetaData.setSize(multipartFile.getSize());

		if (attr != null) {
			Date lastUpdatedDate = null;
			Date lastAccessDate = null;
			if (attr.lastModifiedTime() != null) {
				lastUpdatedDate = new Date(attr.lastModifiedTime().toMillis());
			}
			if (attr.lastAccessTime() != null) {
				lastAccessDate = new Date(attr.lastAccessTime().toMillis());
			}
			fileMetaData.setLastUpdatedDate(lastUpdatedDate);
			fileMetaData.setLastAccessDate(lastAccessDate);
			fileMetaData.setDirectory(attr.isDirectory());
			fileMetaData.setOther(attr.isOther());
			fileMetaData.setRegularFile(attr.isRegularFile());
			fileMetaData.setSymbolicLink(attr.isSymbolicLink());

		}

		return fileMetaData;
	}

	/**
	 * This method is to determine which name (newName or originalFileName) will
	 * be used to name the file.
	 *
	 * @param newName
	 *            : We will assume that the user has to enter the new name with
	 *            the extension inside
	 * @param originalFileName
	 * @return
	 */
	public String assignFileName(String newName, String originalFileName) {
		String fileName = newName;

		if (StringUtils.isEmpty(fileName)) {
			fileName = originalFileName;
		}
		return fileName;
	}

	/**
	 * This method is to determine which path (saveToPath or
	 * DEFAULT_UPLOADED_FOLDER) will be used as the path to which the file will
	 * saved. It returns the full path : ..//...//[file_name].[extension]
	 *
	 * @param saveToPath
	 * @return
	 */
	public String assignFilePath(String saveToPath, String fileName) {
		String path = saveToPath;

		if (StringUtils.isEmpty(path)) {
			path = DEFAULT_UPLOADED_FOLDER;
		}
		path = path + fileName;
		return path;
	}

	/**
	 * This is a simple method to get DEFAULT_UPLOADED_FOLDER which is a private
	 * static String field. We will need this method to access to
	 * DEFAULT_UPLOADED_FOLDER from the other classes.
	 *
	 * @return
	 */
	public String getDefaultUploadedFolder() {
		return DEFAULT_UPLOADED_FOLDER;
	}

}
