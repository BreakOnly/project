/**
 * SDKServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.jrmf.utils.sdkhttp;

public class SDKServiceLocator extends org.apache.axis.client.Service implements com.jrmf.utils.sdkhttp.SDKService {

    public SDKServiceLocator() {
    }


    // Use to get a proxy class for SDKService
//    private java.lang.String SDKService_address = "http://116.58.219.223:8081/sdk/SDKService";
   
//    	 private java.lang.String SDKService_address =PropertyResourceBundle.getBundle("config").getString("uri");
    //    	 private java.lang.String SDKService_address =ConfigUtil.getVoiceSmsUri();
    private java.lang.String SDKService_address = "http://hprpt2.eucp.b2m.cn:8080/sdk/SDKService?wsdl";

    public java.lang.String getSDKServiceAddress() {
        return SDKService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SDKServiceWSDDServiceName = "SDKService";

    public java.lang.String getSDKServiceWSDDServiceName() {
        return SDKServiceWSDDServiceName;
    }

    public void setSDKServiceWSDDServiceName(java.lang.String name) {
        SDKServiceWSDDServiceName = name;
    }

    @Override
    public com.jrmf.utils.sdkhttp.SDKClient getSDKService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SDKService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSDKService(endpoint);
    }

    @Override
    public com.jrmf.utils.sdkhttp.SDKClient getSDKService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.jrmf.utils.sdkhttp.SDKServiceBindingStub _stub = new com.jrmf.utils.sdkhttp.SDKServiceBindingStub(portAddress, this);
            _stub.setPortName(getSDKServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSDKServiceEndpointAddress(java.lang.String address) {
        SDKService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.jrmf.utils.sdkhttp.SDKClient.class.isAssignableFrom(serviceEndpointInterface)) {
                com.jrmf.utils.sdkhttp.SDKServiceBindingStub _stub = new com.jrmf.utils.sdkhttp.SDKServiceBindingStub(new java.net.URL(SDKService_address), this);
                _stub.setPortName(getSDKServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    @Override
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("SDKService".equals(inputPortName)) {
            return getSDKService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    @Override
    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://sdkhttp.eucp.b2m.cn/", "SDKService");
    }

    private java.util.HashSet ports = null;

    @Override
    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://sdkhttp.eucp.b2m.cn/", "SDKService"));
        }
        return ports.iterator();
    }

}
