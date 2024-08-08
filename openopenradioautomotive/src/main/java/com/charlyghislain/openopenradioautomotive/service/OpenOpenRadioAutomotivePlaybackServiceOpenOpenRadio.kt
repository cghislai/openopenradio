package com.charlyghislain.openopenradioautomotive.service

import android.media.MediaSession2.ControllerInfo
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaConstants
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.charlyghislain.openopenradio.service.media.MediaSessionCallback
import com.charlyghislain.openopenradio.service.media.OpenOpenRadioMediaPlaybackService
import com.google.common.util.concurrent.ListenableFuture

class OpenOpenRadioAutomotivePlaybackServiceOpenOpenRadio : OpenOpenRadioMediaPlaybackService() {


    override fun createLibrarySessionCallback(): MediaLibrarySession.Callback {
        return object :
            MediaSessionCallback(this@OpenOpenRadioAutomotivePlaybackServiceOpenOpenRadio) {

            @OptIn(UnstableApi::class)
            override fun onGetLibraryRoot(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                params: MediaLibraryService.LibraryParams?
            ): ListenableFuture<LibraryResult<MediaItem>> {
                var responseParams = params
                if (session.isAutomotiveController(browser)) {
                    // See https://developer.android.com/training/cars/media#apply_content_style
                    val rootHintParams = params ?: LibraryParams.Builder().build()
                    rootHintParams.extras.putInt(
                        MediaConstants.EXTRAS_KEY_CONTENT_STYLE_BROWSABLE,
                        MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
                    )
                    rootHintParams.extras.putInt(
                        MediaConstants.EXTRAS_KEY_CONTENT_STYLE_PLAYABLE,
                        MediaConstants.EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                    )
                    // Tweaked params are propagated to Automotive browsers as root hints.
                    responseParams = rootHintParams
                }
                // Use super to return the common library root with the tweaked params sent to the browser.
                return super.onGetLibraryRoot(session, browser, responseParams)
            }
        }
    }

}