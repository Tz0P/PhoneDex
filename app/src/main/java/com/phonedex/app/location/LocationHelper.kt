package com.phonedex.app.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
object LocationHelper {

    private fun granted(ctx: Context, p: String) =
        ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED

    fun hasPermission(ctx: Context): Boolean =
        granted(ctx, Manifest.permission.ACCESS_FINE_LOCATION) ||
            granted(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)

    /** Tries GPS, then network, then the last known fix. Returns null if nothing is available. */
    suspend fun currentLocation(ctx: Context): Location? {
        if (!hasPermission(ctx)) return null
        val lm = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fine = granted(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        val providers = buildList {
            if (fine && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        }
        for (p in providers) {
            val timeout = if (p == LocationManager.GPS_PROVIDER) 12_000L else 6_000L
            val fix = withTimeoutOrNull(timeout) { requestFix(ctx, lm, p) }
            if (fix != null) return fix
        }
        return (providers + LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    private suspend fun requestFix(ctx: Context, lm: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            try {
                LocationManagerCompat.getCurrentLocation(
                    lm, provider, signal, ContextCompat.getMainExecutor(ctx)
                ) { loc -> if (cont.isActive) cont.resume(loc) }
            } catch (e: SecurityException) {
                if (cont.isActive) cont.resume(null)
            }
        }

    /** Returns a readable place/address, or null when offline or nothing was found. */
    @Suppress("DEPRECATION")
    suspend fun reverseGeocode(ctx: Context, lat: Double, lon: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(ctx, Locale.getDefault())
            val address: Address? = try {
                withTimeoutOrNull(10_000L) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        suspendCancellableCoroutine<Address?> { c ->
                            geocoder.getFromLocation(lat, lon, 1, object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    if (c.isActive) c.resume(addresses.firstOrNull())
                                }
                                override fun onError(errorMessage: String?) {
                                    if (c.isActive) c.resume(null)
                                }
                            })
                        }
                    } else {
                        geocoder.getFromLocation(lat, lon, 1)?.firstOrNull()
                    }
                }
            } catch (e: Exception) {
                null
            }
            address?.toReadable()?.takeIf { it.isNotBlank() }
        }

    private fun Address.toReadable(): String {
        val place = featureName?.takeIf {
            it.isNotBlank() && it.none(Char::isDigit) && it != thoroughfare && it != locality
        }
        val street = thoroughfare?.let { t -> subThoroughfare?.let { "$t $it" } ?: t }
        val city = locality ?: subAdminArea ?: adminArea
        return listOfNotNull(place, street, city).distinct().joinToString(", ")
            .ifBlank { getAddressLine(0).orEmpty() }
    }
}
