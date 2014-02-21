package com.zuora.api.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.axis2.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.ZuoraServiceStub.ID;
import com.zuora.api.axis2.ZuoraServiceStub.Import;
import com.zuora.api.axis2.ZuoraServiceStub.SaveResult;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZImport {

	/** The constant USAGE_TYPE */
	public static final String USAGE_TYPE = "Usage";

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ZImport.class);

	/**
	 * Wrapper for the create usage import when passing the absolute file path
	 * 
	 * @param usageFilePath
	 *            Absolute path to your usage file
	 * @param usageFileName
	 *            File name (note: should be prefixed by `text/plain;name=<YOUR
	 *            FILE NAME>` but I added this as an extra check for
	 *            simplification)
	 * @return ID of the created usage import
	 */
	public static ID createImport(String usageFilePath, String usageFileName) {
		ID createdImportId = null;
		try {
			InputStream is = new FileInputStream(usageFilePath);
			createdImportId = createImport(is, usageFileName);
		} catch (FileNotFoundException ex) {
			logger.error("Could not load file from path: `" + usageFilePath + "`");
		}
		return createdImportId;
	}

	/**
	 * High-level wrapper to create an import object in Zuora
	 * 
	 * @param usageFileInputStream
	 *            InputStream for the usage file
	 * @param usageFileName
	 *            File name (note: should be prefixed by `text/plain;name=<YOUR
	 *            FILE NAME>` but I added this as an extra check for
	 *            simplification)
	 * @return ID of the created usage import
	 */
	public static ID createImport(InputStream usageFileInputStream, String usageFileName) {
		logger.debug("Entering import method for filename `" + usageFileName + "`");
		// Add the MIME content if not already present
		if (!usageFileName.contains("text/plain;name=")) {
			usageFileName = "text/plain;name=" + usageFileName;
			logger.debug("Added MIME content to filename: `" + usageFileName + "`");
		}
		// Create the import object and set the import type
		Import zimport = new Import();
		zimport.setImportType(USAGE_TYPE);
		// Prepare the data source (CSV file containing the usage)
		DataSource dataSource = null;
		try {
			dataSource = new ByteArrayDataSource(usageFileInputStream, usageFileName);
		} catch (IOException e) {
			logger.error("I/O Error loading usage file | " + e.getMessage());
		}
		DataHandler dataHandler = new DataHandler(dataSource);
		zimport.setFileContent(dataHandler);
		// Get the zuora API object and log into Zuora
		ZApi zapi = new ZApi();
		zapi.zLogin();
		// Set the proper configuration option to the ZApi
		zapi.getStub()._getServiceClient().getOptions()
				.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
		// Create the object in Zuora
		SaveResult result = zapi.zCreate(new ZObject[] { zimport })[0];
		if (result.getSuccess()) {
			logger.debug("Successfully created import with ID = " + result.getId());
		} else {
			logger.error("Import failed with the following error(s):");
			for (com.zuora.api.axis2.ZuoraServiceStub.Error error : result.getErrors()) {
				logger.error("field: " + error.getField() + " | message: " + error.getMessage() + " | code: "
						+ error.getCode());
			}
		}
		return result.getId();
	}

}
