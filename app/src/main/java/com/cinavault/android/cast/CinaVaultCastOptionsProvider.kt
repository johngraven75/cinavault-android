package com.cinavault.android.cast

import android.content.Context
import com.cinavault.android.R
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider

class CinaVaultCastOptionsProvider : OptionsProvider {
    override fun getCastOptions(context: Context): CastOptions =
        CastOptions.Builder()
            .setReceiverApplicationId(context.getString(R.string.cast_receiver_app_id))
            .setEnableReconnectionService(true)
            .build()

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider> =
        emptyList()
}
