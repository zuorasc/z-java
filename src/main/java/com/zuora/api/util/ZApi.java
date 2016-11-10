package com.zuora.api.util;

import com.zuora.api.axis2.InvalidQueryLocatorFault;
import com.zuora.api.axis2.InvalidTypeFault;
import com.zuora.api.axis2.InvalidValueFault;
import com.zuora.api.axis2.LoginFault;
import com.zuora.api.axis2.MalformedQueryFault;
import com.zuora.api.axis2.UnexpectedErrorFault;
import com.zuora.api.axis2.*;
import com.zuora.api.axis2.ZuoraServiceStub.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ZApi {

    /**
     * The logger.
     */
    private static Logger logger = LoggerFactory.getLogger(ZApi.class);

    /**
     * The Constant PROPERTY_ENDPOINT.
     */
    private static final String PROPERTY_ENDPOINT = "endpoint";

    /**
     * The Constant PROPERTY_USERNAME.
     */
    private static final String PROPERTY_USERNAME = "username";

    /**
     * The Constant PROPERTY_PASSWORD.
     */
    private static final String PROPERTY_PASSWORD = "password";

    /**
     * Max number of objects per call (except QUERY)
     */
    public static final int MAX_OBJECTS = 50;

    /**
     * The stub.
     */
    private ZuoraServiceStub stub;

    /**
     * The header.
     */
    private SessionHeader header;

    /**
     * The endpoint used.
     */
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
     * Overloaded method to specify the endpoint instead of reading from the
     * config.properties file
     *
     * @param endpoint Specify the endpoint (Zuora) to connect to
     */
    public ZApi(String endpoint) {

        logger.info("Creating a new ZAPI object");

        try {
            setStub(new ZuoraServiceStub());

            // set new ENDPOINT
            setEndpoint(endpoint);

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

        String username = ZuoraUtility.getPropertyValue(PROPERTY_USERNAME);
        String password = ZuoraUtility.getPropertyValue(PROPERTY_PASSWORD);

        logger.debug("Username = " + username + " | Password = " + password);

        return zLogin(username, password);
    }

    /**
     * Overloaded zLogin call to pass username/password credentials instead of
     * reading from config.properties file
     *
     * @param username Zuora API user
     * @param password Zuora API password
     * @return LoginResult object
     */
    public LoginResult zLogin(String username, String password) {

        LoginResult result = null;

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
     * @param queryString The ZOQL query string
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
     * Create subscription(s) in Zuora using API call
     *
     * @param objects array of SubscriptionRequest to create
     * @return SubscribeResult or null if an error occured
     */
    public ZuoraServiceStub.SubscribeResult[] zSubscribe(ZuoraServiceStub.SubscribeRequest[] objects) throws UnexpectedErrorFault, RemoteException {

        ZuoraServiceStub.SubscribeResult[] subscribeResult = null;

        try {
            if (objects.length > MAX_OBJECTS) {

//                subscribeResult = new SubscribeResult[objects.length];

                ZuoraServiceStub.Subscribe subscribe = new ZuoraServiceStub.Subscribe();
                subscribe.setSubscribes(objects);
                ZuoraServiceStub.SubscribeResponse resp = stub.subscribe(subscribe, this.header);
                subscribeResult = resp.getResult();
            } else {

                ZuoraServiceStub.Subscribe create = new ZuoraServiceStub.Subscribe();
                create.setSubscribes(objects);

                ZuoraServiceStub.SubscribeResponse createResponse;
                createResponse = stub.subscribe(create, this.header);
                subscribeResult = createResponse.getResult();
            }
        } catch (UnexpectedErrorFault e) {
            logger.error("Unexpected error | " + e.getFaultMessage());

        } catch (RemoteException e) {
            logger.error("Remote Exception | " + e.getMessage());
        } catch (Exception e) {
            logger.error("Exception | " + e.getMessage());
        }

        if (subscribeResult != null) {
            logger.debug("Successfully received " + subscribeResult.length + " subscribe result(s).");

            for (ZuoraServiceStub.SubscribeResult result : subscribeResult) {
                if (!result.getSuccess()) {
                    logger.error("Create call failed with the following errors:");
                    for (com.zuora.api.axis2.ZuoraServiceStub.Error error : result.getErrors()) {
                        logger.error("field: " + error.getField() + " | message: " + error.getMessage() + " | code: "
                                + error.getCode());
                    }
                }
            }

        } else {
            logger.error("Null object received during zSubscribe() operation");
        }

        return subscribeResult;
    }

    /**
     * Requests aditional result from a previous query() call.
     *
     * @param queryLocator QueryLocator from the query call
     * @return The query result
     */
    public QueryResult zQueryMore(QueryLocator queryLocator) {

        QueryResult result = null;

        // Prepares the query
        QueryMore query = new QueryMore();
        query.setQueryLocator(queryLocator);

        try {
            QueryMoreResponse resp = stub.queryMore(query, null, header);
            result = resp.getResult();
            logger.info("Query returned " + result.getSize() + " values");

        } catch (RemoteException e) {
            logger.error("Remote Exception | " + e.getMessage());

        } catch (UnexpectedErrorFault e) {
            logger.error("Unexpected Error Fault | " + e.getMessage());

        } catch (InvalidQueryLocatorFault e) {
            logger.error("Invalid Query Locator | " + e.getMessage());

        }

        return result;
    }

    public List zAdvancedQuery(String queryString) {

        List objects = new ArrayList();

        QueryResult result = zQuery(queryString);

        if (result != null) {

            if (result.getSize() > 0) {

                Collections.addAll(objects, result.getRecords());

                while (!result.getDone()) {
                    result = zQueryMore(result.getQueryLocator());
                    Collections.addAll(objects, result.getRecords());

                }
            }
        }

        return objects;

    }

    /**
     * Create object(s) in Zuora using API call
     *
     * @param objects array of objects to create
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

        // If an error occurred, log it
        for (SaveResult result : saveResult) {
            if (!result.getSuccess()) {
                logger.error("Create call failed with the following errors:");
                for (com.zuora.api.axis2.ZuoraServiceStub.Error error : result.getErrors()) {
                    logger.error("field: " + error.getField() + " | message: " + error.getMessage() + " | code: "
                            + error.getCode());
                }
            }
        }

        return saveResult;
    }

    /**
     * Update object(s) in Zuora using API calls
     *
     * @param objects array of objects to update (must have their Zuora IDs set)
     * @return SaveResult or null if an error occured
     */
    public SaveResult[] zUpdate(ZObject[] objects) {

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
                    Update update = new Update();
                    update.setZObjects(splittedObjects[i]);

                    UpdateResponse updateResponse = stub.update(update, header);
                    SaveResult[] tmpSaveResult = updateResponse.getResult();

                    // Save the tmp result in the final table result returned
                    for (int j = 0; j < tmpSaveResult.length; j++) {
                        saveResult[(i * MAX_OBJECTS) + j] = tmpSaveResult[j];
                    }
                }

            } else {

                // Prepare the create object
                Update update = new Update();
                update.setZObjects(objects);

                UpdateResponse updateResponse = stub.update(update, header);
                saveResult = updateResponse.getResult();
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

        // If an error occurred, log it
        for (SaveResult result : saveResult) {
            if (!result.getSuccess()) {
                logger.error("Create call failed with the following errors:");
                for (com.zuora.api.axis2.ZuoraServiceStub.Error error : result.getErrors()) {
                    logger.error("field: " + error.getField() + " | message: " + error.getMessage() + " | code: "
                            + error.getCode());
                }
            }
        }

        return saveResult;
    }

    /**
     * Delete object(s) in Zuora using API calls
     *
     * @param ids  Zuora ID of object to delete
     * @param type can be Account, Subscription, etc.
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

    /**
     * Send amendments to zuora. Amendments change subscriptions.
     * The following fields are always required for this call:
     *   Amendment.Type
     *       Amendment.Name
     *       Amendment.SubscriptionId
     * @param amendments
     *          Array of amendments for send to zuora
     * @return
     *          Array of results of each amendment processed
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     */
    private ZuoraServiceStub.AmendResult[] zAmendUnit(ZuoraServiceStub.Amendment[] amendments) throws UnexpectedErrorFault, RemoteException {
        final ZuoraServiceStub.AmendRequest amend = new ZuoraServiceStub.AmendRequest();
        amend.setAmendments(amendments);

        final ZuoraServiceStub.Amend amends = new ZuoraServiceStub.Amend();
        amends.setRequests(new ZuoraServiceStub.AmendRequest[]{amend});


        final ZuoraServiceStub.AmendResponse resp = stub.amend(amends, this.header);
        return resp.getResults();
    }

    /**
     * Print in log errors details if exits
     *
     * @param errors
     *          Array of Zuora errors
     */
    private void printZuoraErrors(com.zuora.api.axis2.ZuoraServiceStub.Error[] errors) {
        if(errors.length > 0) {
            logger.error("Create call failed with the following errors:");
            for (com.zuora.api.axis2.ZuoraServiceStub.Error error : errors) {
                logger.error("field: {} | message: {} | code: {}", error.getField(), error.getMessage(), error.getCode());
            }
        }
    }

    /**
     * {@see ZApi.zAmendUnit}
     *
     * If amendments array exceeds the {code MAX_OBJECTS} limit,
     * then process amends as blocks of {code MAX_OBJECTS} size and make multiple calls to ZuoraServiceStub.amend.
     *
     * @param amendments
     *          Array of amendments for send to zuora
     * @return
     *          Array of results of each amendment processed
     * @throws UnexpectedErrorFault
     * @throws RemoteException
     */
    public ZuoraServiceStub.AmendResult[] zAmend(ZuoraServiceStub.Amendment[] amendments) throws UnexpectedErrorFault, RemoteException {

        ZuoraServiceStub.AmendResult[] amendResult;

        /*
            Check to process amend as bulk or not
         */
        if (amendments.length > MAX_OBJECTS) {
            final ZObject[][] bulkStructure = ZuoraUtility.splitObjects(amendments);
            final List<AmendResult> bulkResults = new ArrayList<AmendResult>(0);
            for (int i = 0; i < bulkStructure.length; i++) {
                bulkResults.addAll(Arrays.asList(zAmendUnit((Amendment[]) bulkStructure[i])));
            }
            amendResult = (AmendResult[]) bulkResults.toArray();

        } else {

            amendResult = zAmendUnit(amendments);
        }

        // check results
        if (amendResult != null) {
            logger.debug("Successfully received {} amend result(s).", amendResult.length);
            for (ZuoraServiceStub.AmendResult result : amendResult) {
                if (!result.getSuccess()) {
                    printZuoraErrors(result.getErrors());
                }
            }

        } else {
            logger.error("Null object received during zAmend() operation");
        }

        return amendResult;
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
