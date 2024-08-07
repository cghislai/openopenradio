package com.charlyghislain.openopenradio.service.media

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.map
import androidx.media3.common.HeartRating
import androidx.media3.common.MediaItem
import androidx.media3.common.Rating
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.stream.Collectors


class CustomMediaSessionCallback(val service: MediaPlaybackService) : MediaLibrarySession.Callback {


    // Configure commands available to the controller in onConnect()
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
//        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
//            .build()
//
//        return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
//            .setAvailableSessionCommands(sessionCommands)
//            .build()
        return super.onConnect(session, controller)
    }

    override fun onSetRating(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        rating: Rating
    ): ListenableFuture<SessionResult> {
        val item = session.player.currentMediaItem;
        val heart: HeartRating = rating as HeartRating

        val future = service.setFavorite(item?.mediaId, heart.isHeart)
        Futures.addCallback(future, object : FutureCallback<SessionResult> {
            override fun onSuccess(result: SessionResult?) {
                if (item != null) {
                    Handler(Looper.getMainLooper()).post { // Use a Handler to post the update to the main thread
                        val updatedItem = item.buildUpon()
                            .setMediaMetadata(
                                item.mediaMetadata.buildUpon()
                                    .apply {
                                        setUserRating(HeartRating(heart.isHeart))
                                    }.build()
                            )
                            .build()

                        session.player.replaceMediaItem(0, updatedItem)
                    }
                }
            }

            override fun onFailure(t: Throwable) {
                Log.e("CustomMediaSessionCallback", "onFailure", t)
            }

        }, MoreExecutors.directExecutor()) // Execute the callback on the main thread

        return future
    }

    override fun onSetRating(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaId: String,
        rating: Rating
    ): ListenableFuture<SessionResult> {
        val heart: HeartRating = rating as HeartRating
        return service.setFavorite(mediaId, heart.isHeart)
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val item = service.treeService.getRootItem()
        return Futures.immediateFuture(
            LibraryResult.ofItem(item, params)
        );
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val item = service.treeService.getItemById(mediaId)
        return Futures.transform(
            item, { i -> LibraryResult.ofItem(i, null) },
            MoreExecutors.directExecutor()
        )
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val future = service.treeService.getChildren(parentId, params, page, pageSize)
        return Futures.transform(
            future, { list -> LibraryResult.ofItemList(list, params) },
            MoreExecutors.directExecutor()
        )
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        return resolveMediaItems(mediaItems)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onSetMediaItems(
        mediaSession: MediaSession,
        browser: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long,
    ): ListenableFuture<MediaItemsWithStartPosition> {
        val future = resolveMediaItems(mediaItems);
        return Futures.transform(
            future, { list -> MediaItemsWithStartPosition(list, startIndex, startPositionMs) },
            MoreExecutors.directExecutor()
        )
    }


    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaItemsWithStartPosition> {
        val currentItem = mediaSession.player.currentMediaItem
        val currentPosition = mediaSession.player.currentPosition
        return Futures.immediateFuture(
            MediaItemsWithStartPosition(
                listOfNotNull(currentItem),
                0,
                currentPosition
            )
        )
    }

    override fun onSearch(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<Void>> {
        return service.treeService.countSearchResults(query)
            .map { c ->
                session.notifySearchResultChanged(browser, query, c, params)
                return@map LibraryResult.ofVoid(params)
            }.asListenableFuture()
    }

    override fun onGetSearchResult(
        session: MediaLibraryService.MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return service.treeService.getSearchResults(query, page, pageSize)
            .map { r -> LibraryResult.ofItemList(r, params) }
            .asListenableFuture()
    }

    private fun resolveMediaItems(mediaItems: List<MediaItem>): ListenableFuture<List<MediaItem>> {
        val futureList: MutableList<ListenableFuture<List<MediaItem>>> = mutableListOf()
        mediaItems.forEach { mediaItem ->
            if (mediaItem.mediaId.isNotEmpty()) {
                val future = service.treeService.expandItem(mediaItem)
                futureList.add(
                    Futures.transform(
                        future, { i -> listOfNotNull(i) },
                        MoreExecutors.directExecutor()
                    )
                )
            } else if (mediaItem.requestMetadata.searchQuery != null) {
                futureList.add(
                    service.treeService.search(mediaItem.requestMetadata.searchQuery!!)
                )
            }
        }

        return Futures.transform(
            Futures.allAsList(futureList),
            { lists ->
                val items =
                    lists.stream()
                        .flatMap { list -> list.stream() }
                        .collect(Collectors.toList())
                return@transform items
            },
            MoreExecutors.directExecutor()
        )
    }
}
