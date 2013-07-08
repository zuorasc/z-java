package com.zuora.api.util;

import java.rmi.RemoteException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zuora.api.axis2.InvalidQueryLocatorFault;
import com.zuora.api.axis2.InvalidTypeFault;
import com.zuora.api.axis2.InvalidValueFault;
import com.zuora.api.axis2.LoginFault;
import com.zuora.api.axis2.MalformedQueryFault;
import com.zuora.api.axis2.UnexpectedErrorFault;
import com.zuora.api.axis2.ZuoraServiceStub;
import com.zuora.api.axis2.ZuoraServiceStub.Create;
import com.zuora.api.axis2.ZuoraServiceStub.CreateResponse;
import com.zuora.api.axis2.ZuoraServiceStub.Delete;
import com.zuora.api.axis2.ZuoraServiceStub.DeleteResponse;
import com.zuora.api.axis2.ZuoraServiceStub.DeleteResult;
import com.zuora.api.axis2.ZuoraServiceStub.ID;
import com.zuora.api.axis2.ZuoraServiceStub.Login;
import com.zuora.api.axis2.ZuoraServiceStub.LoginResponse;
import com.zuora.api.axis2.ZuoraServiceStub.LoginResult;
import com.zuora.api.axis2.ZuoraServiceStub.Query;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResponse;
import com.zuora.api.axis2.ZuoraServiceStub.QueryResult;
import com.zuora.api.axis2.ZuoraServiceStub.SaveResult;
import com.zuora.api.axis2.ZuoraServiceStub.SessionHeader;
import com.zuora.api.axis2.ZuoraServiceStub.ZObject;

public class ZApi {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ZApi.class);

	/** The Constant PROPERTY_ENDPOINT. */
	private static final String PROPERTY_ENDPOINT = "endpoint";

	/** The Constant PROPERTY_USERNAME. */
	private static final String PROPERTY_USERNAME = "username";

	/** The Constant PROPERTY_PASSWORD. */
	private static final String PROPERTY_PASSWORD = "password";

	/** Max number of objects per call (except QUERY) */
	public static final int MAX_OBJECTS = 50;

	/** The stub. */
	private ZuoraServiceStub stub;

	/** The header. */
	private SessionHeader header;

	/** The endpoint used. */
	private String endpoint;

	/**
	 * Instantiates a new Zuora API Helper
	 */
	public ZApi() {

		logger.info("Creating a new ZAPI object");

		try {
			setStub(new ZuoraServiceStub());

			// set new ENDPOINT
			setEndpoint(ZuoraUtility.getPropertyValue(PROPERTY_ENDPOINT));

			if (endpoint != null && endpoint.trim().length() > 0) {
				ServiceClient client = stub._getServiceClient();
				client.getOptions().getTo().setAddress(endpoint);
			}

		} catch (AxisFault e) {
			logger.error(e.getMessage());
		}

	}

	/**
	 * zLogin.
	 * 
	 * @return LoginResult object
	 */
	public LoginResult zLogin() {

		LoginResult result = null;

		String username = ZuoraUtility.getPropertyValue(PROPERTY_USERNAME);
		String password = ZuoraUtility.getPropertyValue(PROPERTY_PASSWORD);

		logger.debug("Username = " + username + " | Password = " + password);

		// Prepare the login request
		Login login = new Login();

		login.setUsername(username);
		login.setPassword(password);

		// Get the response from Zuora
		LoginResponse resp;

		try {

			resp = stub.login(login);
			result = resp.getResult();

			// Create session for all subsequent calls
			setHeader(new SessionHeader());
			getHeader().setSession(result.getSession());

			logger.info("User `" + ZuoraUtility.getPropertyValue(PROPERTY_USERNAME) + "` successfully connected!");

		} catch (RemoteException e) {
			logger.error("Remote error while trying to login | " + e.getMessage());

		} catch (UnexpectedErrorFault e) {
			logger.error("Unexpected Error Fault | " + e.getMessage());

		} catch (LoginFault e) {
			logger.error("Login Fault | " + e.getMessage());
		}

		return result;
	}

	/**
	 * Do a query to Zuora and return the result (no more than 2,000 objects)
	 * 
	 * @param queryString
	 *            The ZOQL query string
	 * @return The query result
	 */
	public QueryResult zQuery(String queryString) {

		QueryResult result = null;

		// Prepare the query
		Query query = new Query();

		query.setQueryString(queryString);
		logger.debug("Query String = " + queryString);

		// We set `null` for the 2nd parameter to return max. 2000 objects
		try {
			QueryResponse resp = stub.query(query, null, header);
			result = resp.getResult();
			logger.info("Query returned " + result.getSize() + " values");

		} catch (RemoteException e) {
			logger.error("Remote Exception | " + e.getMessage());

		} catch (MalformedQueryFault e) {
			logger.error("Malformed Query | " + e.getMessage());

		} catch (UnexpectedErrorFault e) {
			logger.error("Unexpected Error Fault | " + e.getMessage());

		} catch (InvalidQueryLocatorFault e) {
			logger.error("Invalid Query Locator | " + e.getMessage());

		}

		return result;
	}

	/**
	 * Create object in Zuora using API call
	 * 
	 * @param objects
	 *            array of objects to create
	 * @return SaveResult or null if an error occured
	 */
	public SaveResult[] zCreate(ZObject[] objects) {

		SaveResult[] saveResult = null;

		try {
			// If there is more than MAX_OBJECTS to create we split the call and
			// then merge back the result
			if (objects.length > MAX_OBJECTS) {

				// Get the objects in split tables
				ZObject[][] splittedObjects = ZuoraUtility.splitObjects(objects);

				saveResult = new SaveResult[objects.length];

				// For each sub table, create() API call
				for (int i = 0; i < splittedObjects.length; i++) {

					// Prepare the create object
					Create create = new Create();
					create.setZObjects(splittedObjects[i]);

					CreateResponse createResponse = stub.create(create, null, header);
					SaveResult[] tmpSaveResult = createResponse.getResult();

					// Save the tmp result in the final table result returned
					for (int j = 0; j < tmpSaveResult.length; j++) {
						saveResult[(i * MAX_OBJECTS) + j] = tmpSaveResult[j];
					}
				}

			} else {

				// Prepare the create object
				Create create = new Create();
				create.setZObjects(objects);

				CreateResponse createResponse = stub.create(create, null, header);
				saveResult = createResponse.getResult();
			}

		} catch (UnexpectedErrorFault e) {
			logger.error("Unexpected error | " + e.getFaultMessage());

		} catch (InvalidTypeFault e) {
			logger.error("Invalid Type Fault | " + e.getFaultMessage());

		} catch (RemoteException e) {
			logger.error("Remote Exception | " + e.getMessage());
		}

		if (saveResult != null) {
			logger.debug("Successfully received " + saveResult.length + " save result(s).");
		} else {
			logger.error("Null object received during zCreate() operation");
		}
		
		return saveResult;
	}

	/**
	 * Delete objects in Zuora using API calls
	 * 
	 * @param ids
	 *            Zuora ID of object to delete
	 * @param type
	 *            can be Account, Subscription, etc.
	 * @return Delete Result if success, null if an error occurred
	 */
	public DeleteResult[] zDelete(String[] ids, String type) {

		DeleteResult[] deleteResult = null;
		
		try {

			// If there is more than MAX_OBJECTS to create we split the call and
			// then merge back the result
			if (ids.length > MAX_OBJECTS) {

				// Get the objects in split tables
				String[][] splittedIds = ZuoraUtility.splitIds(ids);

				deleteResult = new DeleteResult[ids.length];

				// For each sub table, create() API call
				for (int i = 0; i < splittedIds.length; i++) {
					
					// Convert the IDs to Zuora IDs
					ID[] zuoraIds = ZuoraUtility.stringToZuoraId(splittedIds[i]);
					
					// Prepare the delete object
					Delete delete = new Delete();
					delete.setType(type);
					delete.setIds(zuoraIds);
					
					DeleteResponse deleteResponse = stub.delete(delete, header);
					DeleteResult[] tmpDeleteResult = deleteResponse.getResult();

					// Save the tmp result in the final table result returned
					for (int j = 0; j < tmpDeleteResult.length; j++) {
						deleteResult[(i * MAX_OBJECTS) + j] = tmpDeleteResult[j];
					}
				}

			} else {
				// Convert the IDs to Zuora IDs
				ID[] zuoraIds = ZuoraUtility.stringToZuoraId(ids);
				
				// Prepare the delete object
				Delete delete = new Delete();
				delete.setType(type);
				delete.setIds(zuoraIds);
				
				DeleteResponse deleteResponse = stub.delete(delete, header);
				deleteResult = deleteResponse.getResult();
			}

		} catch (InvalidValueFault e) {
			logger.error("Invalid Value | " + e.getFaultMessage());

		} catch (UnexpectedErrorFault e) {
			logger.error("Unexpected error | " + e.getFaultMessage());

		} catch (InvalidTypeFault e) {
			logger.error("Invalid Type Fault | " + e.getFaultMessage());

		} catch (RemoteException e) {
			logger.error("Remote Exception | " + e.getMessage());
		}
		
		if (deleteResult != null)
			logger.info("Successfully deleted " + deleteResult.length + " zObject(s)");
		else
			logger.error("An error occurred during the zDelete() call");
		
		return deleteResult;
	}
	

	// --- Setter(s) & Getter(s) ---

	public ZuoraServiceStub getStub() {
		return stub;
	}

	private void setStub(ZuoraServiceStub stub) {
		this.stub = stub;
	}

	public SessionHeader getHeader() {
		return header;
	}

	private void setHeader(SessionHeader header) {
		this.header = header;
	}

	public String getEndpoint() {
		return endpoint;
	}

	private void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

}
