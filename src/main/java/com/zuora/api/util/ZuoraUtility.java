package com.zuora.api.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.ZuoraServiceStub.ID;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZuoraUtility {

	/** The logger (using logback, but can easily be configured for log4j). */
	private static final Logger logger = LoggerFactory.getLogger(ZuoraUtility.class);

	/** Filename for properties file. */
	private static final String FILE_PROPERTY_NAME = "config.properties";

	/** The properties, loaded from the file. */
	private static Properties properties = null;

	
	/**
	 * Load the properties.
	 */
	@SuppressWarnings("resource")
	public static void loadProperties() {
		
		// Retrieve resource from working directory, if unfound fall back to packaged resource
		InputStream is;
		
		try {
			is = new FileInputStream("./" + FILE_PROPERTY_NAME);
		} catch (FileNotFoundException e1) {
			logger.debug("Could not read file from working directoy, switching to packaged properties file");
			is = ZuoraUtility.class.getResourceAsStream("/" + FILE_PROPERTY_NAME);
		}

		properties = new Properties();

		try {
			properties.load(is);
			logger.info("Properties successfully loaded from `" + FILE_PROPERTY_NAME + "`");

		} catch (IOException e) {
			logger.error("Error loading properties file | " + e.getMessage());

		} catch (NullPointerException e) {
			logger.error("Error loading properties file (null pointer exception while loading properties) | "
					+ e.getMessage());
		} finally {
			// Close the resource
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error("Could close the ressource | " + e.getMessage());
				}
			}
		}
		
	}

	
	/**
	 * Get the properties (singleton pattern).
	 */
	public static Properties getProperties() {

		if (properties == null) {
			loadProperties();
		}

		return properties;
	}

	
	/**
	 * Get a property value.
	 */
	public static String getPropertyValue(String propertyName) {

		return getProperties().getProperty(propertyName);
	}
	
	
	/**
	 * Split object in tab of 50
	 */
	public static ZObject[][] splitObjects(ZObject[] objects) {

		int n = objects.length / ZApi.MAX_OBJECTS + 1;

		ZObject[][] returnedObjects = new ZObject[n][ZApi.MAX_OBJECTS];

		for (int i = 0; i < objects.length; i++) {
			int j = i / ZApi.MAX_OBJECTS;
			returnedObjects[j][i % ZApi.MAX_OBJECTS] = objects[i];
		}

		return returnedObjects;
	}

	
	/**
	 * Split string in tab of 50
	 */
	public static String[][] splitIds(String[] ids) {

		int n = ids.length / ZApi.MAX_OBJECTS + 1;

		String[][] returnedIds = new String[n][ZApi.MAX_OBJECTS];

		for (int i = 0; i < ids.length; i++) {
			int j = i / ZApi.MAX_OBJECTS;
			returnedIds[j][i % ZApi.MAX_OBJECTS] = ids[i];
		}

		return returnedIds;
	}
	
	
	/**
	 * Convert a tab of string in a tab of Zuora IDS
	 */
	public static ID[] stringToZuoraId(String[] ids) {
		
		ID[] zuoraIds = new ID[ids.length];
		
		for (int i = 0; i < ids.length; i++) {
			ID zId = new ID();
			zId.setID(ids[i]);
			zuoraIds[i] = zId;
		}
		
		return zuoraIds;
	}
}
