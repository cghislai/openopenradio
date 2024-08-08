package com.charlyghislain.openopenradio.service

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Build
import com.charlyghislain.openopenradio.OpenOpenRadioMain
import com.charlyghislain.openopenradio.service.media.OpenOpenRadioMediaPlaybackService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OpenOpenRadioPlaybackServiceOpenOpenRadio : OpenOpenRadioMediaPlaybackService() {


    companion object {
        private val immutableFlag =
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    }

    override fun getSingleTopActivity(): PendingIntent? {
        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, OpenOpenRadioMain::class.java),
            immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun getBackStackedActivity(): PendingIntent? {
        return TaskStackBuilder.create(this).run {
            addNextIntent(Intent(this@OpenOpenRadioPlaybackServiceOpenOpenRadio, OpenOpenRadioMain::class.java))
            getPendingIntent(0, immutableFlag or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

}
