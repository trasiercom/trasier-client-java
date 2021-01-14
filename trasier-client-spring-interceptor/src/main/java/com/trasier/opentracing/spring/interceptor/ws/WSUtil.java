package com.trasier.opentracing.spring.interceptor.ws;

import com.trasier.client.api.TrasierConstants;
import org.springframework.util.StringUtils;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;

import javax.xml.transform.dom.DOMSource;

public final class WSUtil {

    public static String extractOperationName(MessageContext messageContext, Object endpoint) {
        if (endpoint instanceof MethodEndpoint) {
            MethodEndpoint methodEndpoint = (MethodEndpoint) endpoint;
            String methodName = methodEndpoint.getMethod().getName();
            if (methodName != null) {
                return methodName;
            }
        }

        if (messageContext.getRequest() instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();

            String soapAction = soapMessage.getSoapAction();
            SoapBody body = soapMessage.getSoapBody();
            if (body.getPayloadSource() instanceof DOMSource) {
                Node node = ((DOMSource) body.getPayloadSource()).getNode();
                return node.getLocalName();
            } else if (!StringUtils.isEmpty(soapAction)) {
                soapAction = soapAction.replaceAll("\"", "");
                String[] soapActionArray = soapAction.split("/");
                return soapActionArray[soapActionArray.length - 1];
            }
        }

        return TrasierConstants.UNKNOWN_WS_CALL;
    }

}
