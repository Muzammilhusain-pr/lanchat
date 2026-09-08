package com.example.lanchat.network

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log

/**
 * Zero-config peer discovery over the local network using Android's NSD
 * (Network Service Discovery, a wrapper around mDNS/Bonjour).
 *
 * Flow:
 *  - The HOST device calls [advertise] once its hotspot + LocalServer are up.
 *    This broadcasts "LanChat is running on this device, at this port" to
 *    the local network -- no typing IP addresses.
 *  - CLIENT devices call [discover] to find the host automatically. When
 *    found, they get the host's local IP + port and open a WebSocket to it
 *    via SignalingClient.
 *
 * This only ever touches the local subnet -- nothing here needs, or uses,
 * a route to the internet.
 */
class NsdDiscoveryManager(private val context: Context) {

    companion object {
        const val SERVICE_TYPE = "_lanchat._tcp."
        const val SERVICE_NAME = "LanChatHost"
        private const val TAG = "NsdDiscoveryManager"
    }

    private val nsdManager: NsdManager by lazy {
        context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    /** Call on the HOST device once LocalServer is listening on [port]. */
    fun advertise(port: Int) {
        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.i(TAG, "Advertising LanChat host on port $port")
            }
            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Failed to advertise service: $errorCode")
            }
            override fun onServiceUnregistered(info: NsdServiceInfo) {}
            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {}
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    /**
     * Call on CLIENT devices. [onHostFound] fires with (host IP, port) as
     * soon as a LanChat host is found on the local network.
     */
    fun discover(onHostFound: (host: String, port: Int) -> Unit) {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.i(TAG, "Discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType == SERVICE_TYPE && service.serviceName.contains(SERVICE_NAME)) {
                    // TODO: NsdServiceInfo.host/port are populated after resolveService()
                    // on API < 34. Call nsdManager.resolveService(service, resolveListener)
                    // here and invoke onHostFound from the resolve callback.
                    nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                            Log.e(TAG, "Resolve failed: $errorCode")
                        }
                        override fun onServiceResolved(info: NsdServiceInfo) {
                            val hostAddress = info.host.hostAddress ?: return
                            onHostFound(hostAddress, info.port)
                        }
                    })
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.i(TAG, "Host lost: ${service.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {}
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
        }

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopAdvertising() {
        registrationListener?.let { runCatching { nsdManager.unregisterService(it) } }
        registrationListener = null
    }

    fun stopDiscovery() {
        discoveryListener?.let { runCatching { nsdManager.stopServiceDiscovery(it) } }
        discoveryListener = null
    }
}
