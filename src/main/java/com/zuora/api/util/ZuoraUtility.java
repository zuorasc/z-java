package com.zuora.api.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.ZuoraServiceStub.ID;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

public class ZuoraUtility {

	/** The logger (using logback, but can easily be configured for log4j). */
	private static final Logger logger = LoggerFactory.getLogger(ZuoraUtility.class);

	/** Filename for properties file. */
	private static final String FILE_PROPERTY_NAME = "application.yml";

	private static final String VERSION = "79.0";

	/** The properties, loaded from the file. */
	private static Properties properties = null;

	
	/**
	 * Load the properties.
	 */
	@SuppressWarnings("resource")
	public static void loadProperties() {
		Yaml yaml = new Yaml();
		ClassLoader loader = ZuoraUtility.class.getClassLoader();
		try {
			InputStream in = new FileInputStream(loader.getResource(FILE_PROPERTY_NAME).getFile());
			Map config = yaml.loadAs( in, LinkedHashMap.class);
			properties = new Properties();

			Map soap = ((Map)((Map)((Map)config.get("external")).get("zuora")).get("soap"));

			properties.setProperty("username", (String)soap.get("username"));
			properties.setProperty("password", (String)soap.get("password"));
			String endpoint = "https://" + soap.get("host") + "/apps/services/a/" + VERSION;
			properties.setProperty("endpoint", endpoint);
		} catch (IOException e) {
			logger.error("error obtain file: " + e.getMessage());
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

	/**
	 * Gets the current date.
	 *
	 * @return the current date
	 */
	public static Calendar getCurrentDate() {
		return Calendar.getInstance();
	}

}
