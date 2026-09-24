package com.alquran.offline.azan

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Pengambilan lokasi 100% offline via GPS/penyedia lokasi sistem — TIDAK memakai
 * internet/API pihak ketiga. Dipakai hanya untuk menghitung waktu sholat secara lokal.
 */
object LocationHelper {

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(context: Context): Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (p in providers) {
            try {
                if (!lm.isProviderEnabled(p)) continue
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (best == null || loc.time > (best?.time ?: 0L)) best = loc
            } catch (ignored: Throwable) {
            }
        }
        return best
    }

    @SuppressLint("MissingPermission")
    suspend fun requestFreshLocation(context: Context, timeoutMs: Long = 15_000L): Location? =
        suspendCancellableCoroutine { cont ->
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (lm == null) {
                cont.resumeWith(Result.success(null))
                return@suspendCancellableCoroutine
            }
            val provider = when {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }
            if (provider == null) {
                cont.resumeWith(Result.success(getLastKnownLocation(context)))
                return@suspendCancellableCoroutine
            }

            var resolved = false
            val mainHandler = Handler(Looper.getMainLooper())
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (resolved) return
                    resolved = true
                    try {
                        lm.removeUpdates(this)
                    } catch (ignored: Throwable) {
                    }
                    cont.resumeWith(Result.success(location))
                }

                @Deprecated("Deprecated in API, still required to override")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                mainHandler.post {
                    try {
                        lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                    } catch (e: Throwable) {
                        if (!resolved) {
                            resolved = true
                            cont.resumeWith(Result.success(getLastKnownLocation(context)))
                        }
                    }
                }
            } catch (e: Throwable) {
                if (!resolved) {
                    resolved = true
                    cont.resumeWith(Result.success(getLastKnownLocation(context)))
                }
            }

            mainHandler.postDelayed({
                if (!resolved) {
                    resolved = true
                    try {
                        lm.removeUpdates(listener)
                    } catch (ignored: Throwable) {
                    }
                    cont.resumeWith(Result.success(getLastKnownLocation(context)))
                }
            }, timeoutMs)

            cont.invokeOnCancellation {
                try {
                    lm.removeUpdates(listener)
                } catch (ignored: Throwable) {
                }
            }
        }
}
