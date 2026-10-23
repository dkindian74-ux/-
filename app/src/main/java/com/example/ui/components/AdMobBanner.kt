package com.example.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Official AdMob IDs for Meri Majdoori:
 * App ID: ca-app-pub-4130353983461080~3590431574 (configured in AndroidManifest.xml)
 * Banner Ad Unit ID: ca-app-pub-4130353983461080/5833451535
 */
const val ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-4130353983461080/5833451535"
const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

private const val TAG = "AdMobBanner"

/**
 * Displays an Anchored Adaptive Google AdMob Banner Ad.
 * Handles lifecycle cleanup, adaptive screen dimensions, and graceful loading failures.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = ADMOB_BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current

    if (isInspectionMode) {
        // Compose Preview placeholder
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE2E8F0))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Google AdMob Banner (Preview)",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var adLoadFailed by remember { mutableStateOf(false) }

    val adView = remember {
        try {
            AdView(context).apply {
                this.adUnitId = adUnitId
                val adSize = getAdaptiveAdSize(context)
                setAdSize(adSize)
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        isAdLoaded = true
                        adLoadFailed = false
                        Log.d(TAG, "AdMob banner successfully loaded.")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        isAdLoaded = false
                        adLoadFailed = true
                        Log.w(TAG, "AdMob banner failed to load: ${error.message} (Code: ${error.code})")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdView: ${e.message}", e)
            null
        }
    }

    DisposableEffect(adView) {
        onDispose {
            try {
                adView?.destroy()
            } catch (e: Exception) {
                Log.w(TAG, "Error destroying AdView: ${e.message}")
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admob_banner"),
        contentAlignment = Alignment.Center
    ) {
        if (adView != null) {
            AndroidView(
                factory = { adView },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Calculates the anchored adaptive banner ad size based on device screen width in dp.
 */
private fun getAdaptiveAdSize(context: Context): AdSize {
    return try {
        val displayMetrics = context.resources.displayMetrics
        val density = displayMetrics.density
        val adWidthPixels = displayMetrics.widthPixels.toFloat()
        val adWidthDp = (adWidthPixels / density).toInt()
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    } catch (e: Exception) {
        Log.w(TAG, "Unable to compute adaptive ad size, falling back to standard BANNER: ${e.message}")
        AdSize.BANNER
    }
}
