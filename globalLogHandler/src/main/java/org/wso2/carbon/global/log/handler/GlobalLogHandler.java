package org.wso2.carbon.global.log.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.Map;

/**
 * This class will log the following information from the request.
 * 1. API context path.
 * 2. Request received time.
 * 3. Response received time.
 * 4. Api manager time.
 * 5. Backend time.
 * 6. Incoming headers.
 * */
public class GlobalLogHandler extends AbstractSynapseHandler {
    Log log = LogFactory.getLog(GlobalLogHandler.class);
    public static final String BACKEND_REQUEST_START_TIME = "api.ut.backendRequestTime";
    public static final String BACKEND_REQUEST_END_TIME = "api.ut.backendRequestEndTime";
    public static final String REQUEST_EXECUTION_START_TIME ="request.execution.start.time";
    public static final String BACKEND_LATENCY = "backend_latency";

    private static final String BACK_END_LATENCY = "BACK_END_LATENCY";
    private static final String BACKEND_URL = "BACKEND_URL";
    private static final String REQUEST_HEADERS = "REQUEST_HEADERS";
    private static final String API_METHOD = "API_METHOD";
    private static final String REQUEST_EVENT_PUBLICATION_ERROR = "Cannot publish request event. ";
    private static final String RESPONSE_EVENT_PUBLICATION_ERROR = "Cannot publish response event. ";
    private String isLogHeadersEnabled = System.getProperty("logHeaders");

    @Override
    public boolean handleRequestInFlow(MessageContext messageContext) {

        // If the request path is /token or /revoke, set the execution start time.
        String transportInURL = (String)((Axis2MessageContext)messageContext).getAxis2MessageContext().getProperty("TransportInURL");
        if ("/token".equals(transportInURL) || "/revoke".equals(transportInURL)) {
            messageContext.setProperty(REQUEST_EXECUTION_START_TIME, Long.toString(System.currentTimeMillis()));
        }

        String apiMethod = LogUtils.getRestMethod(messageContext);
        messageContext.setProperty(API_METHOD, apiMethod);
        return true;
    }

    @Override
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        try {
            String apiCTX = LogUtils.getAPICtx(messageContext);
            // If the api context is /token, set the backend request start time.
            if ("/token".equals(apiCTX) || "/revoke".equals(apiCTX)) {
                messageContext.setProperty(BACKEND_REQUEST_START_TIME, Long.toString(System.currentTimeMillis()));
            }
            String apiTo = LogUtils.getTo(messageContext);
            String reqHeaders = LogUtils.getHeaders(messageContext);
            String apiConsumerKey = LogUtils.getConsumerKey(messageContext);
            messageContext.setProperty(BACKEND_URL, apiTo);
            if (isLogHeadersEnabled != null) {
                messageContext.setProperty(REQUEST_HEADERS, reqHeaders);
            }
            return true;
        } catch (Exception e) {
            log.error(REQUEST_EVENT_PUBLICATION_ERROR + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean handleResponseInFlow(MessageContext messageContext) {
        try {
            long backendlLatency = getBackendLatency(messageContext);
            messageContext.setProperty(BACK_END_LATENCY, Long.toString(backendlLatency));
            return true;
        } catch (Exception e) {
            log.error(RESPONSE_EVENT_PUBLICATION_ERROR + e.getMessage(), e);
        }
        return true;
    }

    @Override
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        try {
            String apiMethod = (String) messageContext.getProperty(API_METHOD);
            String apiName = LogUtils.getAPIName(messageContext);
            String apiCTX = LogUtils.getAPICtx(messageContext);
            String apiTo = (String) messageContext.getProperty(BACKEND_URL);
            String apiResponseSC = LogUtils.getRestHttpResponseStatusCode(messageContext);

            long beTotalLatency = 0;
            String backendLatency = (String) messageContext.getProperty(BACK_END_LATENCY);
            if (backendLatency != null) {
                beTotalLatency = Long.parseLong(backendLatency);
            }

            long responseTime = getResponseTime(messageContext);

            if (isLogHeadersEnabled != null) {
                String reqHeaders = (String) messageContext.getProperty(REQUEST_HEADERS);
                log.info("|" + apiName + "|" + apiMethod + "|" + apiCTX + "|"
                        + apiTo + "|" + reqHeaders + "|"  + apiResponseSC + "|" + beTotalLatency + "|" + responseTime);
            } else {
                log.info("|" + apiName + "|" + apiMethod + "|" + apiCTX + "|"
                        + apiTo + "|" + apiResponseSC + "|" + beTotalLatency + "|" + responseTime);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return true;
    }

    private long getResponseTime(org.apache.synapse.MessageContext messageContext) {
        long responseTime = 0;
        try {
            long rtStartTime = 0;
            if (messageContext.getProperty(REQUEST_EXECUTION_START_TIME) != null) {
                Object objRtStartTime = messageContext.getProperty(REQUEST_EXECUTION_START_TIME);
                rtStartTime = (objRtStartTime == null ? 0 : Long.parseLong((String) objRtStartTime));
            }
            responseTime = System.currentTimeMillis() - rtStartTime;
        } catch (Exception e) {
            log.error("Error getResponseTime -  " + e.getMessage(), e);
        }
        return responseTime;
    }

    private long getBackendLatency(org.apache.synapse.MessageContext messageContext) {
        long beTotalLatency = 0;
        long beStartTime = 0;
        long beEndTime = 0;
        long executionStartTime = 0;
        try {
            if (messageContext.getProperty(BACKEND_REQUEST_END_TIME) == null) {
                if (messageContext.getProperty(BACKEND_REQUEST_START_TIME) != null) {
                    executionStartTime = Long.parseLong(
                            (String) messageContext.getProperty(BACKEND_REQUEST_START_TIME));
                }
                messageContext.setProperty(BACKEND_LATENCY,
                        System.currentTimeMillis() - executionStartTime);
                messageContext.setProperty(BACKEND_REQUEST_END_TIME, System.currentTimeMillis());
            }
            if (messageContext.getProperty(BACKEND_REQUEST_START_TIME) != null) {
                beStartTime = Long.parseLong(
                        (String) messageContext.getProperty(BACKEND_REQUEST_START_TIME));
            }
            if (messageContext.getProperty(BACKEND_REQUEST_END_TIME) != null) {
                beEndTime = (Long) messageContext.getProperty(BACKEND_REQUEST_END_TIME);
            }

            beTotalLatency = beEndTime - beStartTime;

        } catch (Exception e) {
            log.error("Error getBackendLatency -  " + e.getMessage(), e);
        }
        return beTotalLatency;
    }
}
