package com.android.ronoam.taps.Network;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Utils.MyLog;

import android.os.Handler;
import android.os.Message;

public class NsdHelper {
    //private Context mContext;
    private Handler mHandler;
    private NsdManager mNsdManager;
    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdManager.RegistrationListener mRegistrationListener;
    private static final String SERVICE_TYPE = "_http._tcp.";
    private static final String TAG = "NsdHelper";
    private String mServiceName, mServiceWithoutName;
    private NsdServiceInfo mService;

    public NsdHelper(Context context, String serviceName, String deviceName, Handler handler) {
        mHandler = handler;
        mServiceWithoutName = serviceName;
        mServiceName = serviceName.concat(deviceName);
        //mContext = context;
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mHandler.obtainMessage(FinalVariables.NETWORK_UNINITIALIZED).sendToTarget();
    }
    public void initializeNsd() {
        initializeResolveListener();
        //mNsdManager.init(mContext.getMainLooper(), this);
    }
    private void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                new MyLog(TAG, "Service discovery started");
                mHandler.obtainMessage(FinalVariables.NETWORK_DISCOVERY_STARTED).sendToTarget();
            }
            @Override
            public void onServiceFound(NsdServiceInfo service) {
                new MyLog(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    new MyLog(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    new MyLog(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceWithoutName)){
                    mHandler.obtainMessage(FinalVariables.NETWORK_DISCOVERY_SERVICE_FOUND).sendToTarget();
                    /*Message message = mHandler.obtainMessage(FinalVariables.NETWORK_DISCOVERY_SERVICE_FOUND);
                    message.obj = service;
                    message.sendToTarget();
                    */
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }
            @Override
            public void onServiceLost(NsdServiceInfo service) {
                new MyLog(TAG, "service lost" + service);
                Message message = mHandler.obtainMessage(FinalVariables.NETWORK_DISCOVERY_SERVICE_LOST);
                message.obj = service;
                message.sendToTarget();
                if (mService == service) {
                    mService = null;
                }
            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                new MyLog(TAG, "Discovery stopped: " + serviceType);
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                new MyLog(TAG, "Discovery failed: Error code:" + errorCode);
                mHandler.obtainMessage(FinalVariables.NETWORK_DISCOVERY_START_FAILED).sendToTarget();
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                new MyLog(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }
    private void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                new MyLog(TAG, "Resolve failed" + errorCode);
                mHandler.obtainMessage(FinalVariables.NETWORK_RESOLVED_FAILED).sendToTarget();
                discoverServices();
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                new MyLog(TAG, "Resolve Succeeded. " + serviceInfo);
                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    new MyLog(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
                Message message = mHandler.obtainMessage(FinalVariables.NETWORK_RESOLVED_SERVICE);
                message.obj = serviceInfo;
                message.sendToTarget();
            }
        };
    }
    private void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                mServiceName = NsdServiceInfo.getServiceName();
                new MyLog(TAG, "Service registered: " + mServiceName);
                Message message = mHandler.obtainMessage(FinalVariables.NETWORK_REGISTERED_SUCCEEDED);
                message.obj = NsdServiceInfo;
                message.sendToTarget();
            }
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                new MyLog(TAG, "Service registration failed: " + arg1);
                mHandler.obtainMessage(FinalVariables.NETWORK_REGISTERED_FAILED).sendToTarget();
            }
            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                new MyLog(TAG, "Service unregistered: " + arg0.getServiceName());
            }
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                new MyLog(TAG, "Service unregistration failed: " + errorCode);
            }
        };
    }
    public void registerService(int port) {
        tearDown();  // Cancel any previous registration request
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(mServiceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }
    public void discoverServices() {
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }
    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }catch (Exception e){
                new MyLog(TAG, e.getMessage());
            } /*finally {
            }*/
            mDiscoveryListener = null;
        }
    }
    public NsdServiceInfo getChosenServiceInfo() {
        NsdServiceInfo temp = mService;
        mService = null;
        return temp;
    }
    public void tearDown() {
        if (mRegistrationListener != null) {
            try {
                mNsdManager.unregisterService(mRegistrationListener);
            } catch (Exception e){
                new MyLog(TAG, e.getMessage());
            }/* finally {
            }*/
            mRegistrationListener = null;
        }
    }
}
