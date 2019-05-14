/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.global.log.handler;

import org.apache.http.HttpHeaders;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.Map;

/**
 * Provides util methods for the LogsHandler
 */
class LogUtils {

    protected static String getAuthorizationHeader(Map headers) {
        return (String) headers.get(HttpHeaders.AUTHORIZATION);
    }

    protected static String getOrganizationIdHeader(Map headers) {
        return (String) headers.get("organization-id");
    }

    protected static String getSourceIdHeader(Map headers) {
        return (String) headers.get("source-id");
    }

    protected static String getApplicationIdHeader(Map headers) {
        return (String) headers.get("application-id");
    }

    protected static String getUuidHeader(Map headers) {
        return (String) headers.get("uuid");
    }

    protected static String getAPIName(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("SYNAPSE_REST_API");
    }

    protected static String getAPICtx(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("REST_API_CONTEXT");
    }

    protected static String getRestMethod(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        return (String) axis2MsgContext.getProperty("HTTP_METHOD");
    }

    protected static String getRestHttpResponseStatusCode(org.apache.synapse.MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MsgContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        return String.valueOf(axis2MsgContext.getProperty("HTTP_SC"));
    }

    protected static String getTo(org.apache.synapse.MessageContext messageContext) {
        return (String) messageContext.getProperty("DYNAMIC_URL_VALUE");
    }

    protected static Map getTransportHeaders(org.apache.synapse.MessageContext messageContext) {
        return (Map) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

    protected static String getConsumerKey(org.apache.synapse.MessageContext messageContext){
        return (String) messageContext.getProperty("api.ut.consumerKey");
    }

    public static String getHeaders (org.apache.synapse.MessageContext messageContext) {
        Map headers = getTransportHeaders(messageContext);
        StringBuilder headersStr = new StringBuilder();
        for (Object key : headers.keySet()) {
            if (!"Authorization".equals(key)) {
                String header = key + ": " + headers.get(key);
                headersStr.append(header);
                headersStr.append(", ");
            }
        }
        return headersStr.toString();
    }
}