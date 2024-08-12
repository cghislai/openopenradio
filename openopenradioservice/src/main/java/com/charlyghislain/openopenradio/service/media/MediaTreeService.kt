package com.charlyghislain.openopenradio.service.media

import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.media3.common.HeartRating
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Rating
import androidx.media3.common.util.UnstableApi
import com.charlyghislain.openopenradio.service.radio.model.RatedStation
import com.charlyghislain.openopenradio.service.radio.repository.CountryRepository
import com.charlyghislain.openopenradio.service.radio.repository.GenreRepository
import com.charlyghislain.openopenradio.service.radio.repository.LanguageRepository
import com.charlyghislain.openopenradio.service.radio.repository.StationFavoritesRepository
import com.charlyghislain.openopenradio.service.radio.repository.StationRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class MediaTreeService(
    val countryRepository: CountryRepository,
    val genreRepository: GenreRepository,
    val languageRepository: LanguageRepository,
    val stationRepository: StationRepository,
) {

    companion object {
        private const val ROOT_ID = "[root]"
        private const val GENRE_ID = "[genre]"
        private const val COUNTRY_ID = "[country]"
        private const val LANGUAGE_ID = "[language]"
        private const val FAVORITES_ID = "[favorites]"
        private const val ALL_ID = "[all]"

        private const val GENRE_PREFIX = "[genre]:"
        private const val COUNTRY_PREFIX = "[country]:"
        private const val LANGUAGE_PREFIX = "[language]:"

        private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    }

    init {
        createBaseTree()
    }

    fun expandItem(item: MediaItem): ListenableFuture<MediaItem> {
        val fullItemFuture = getItemById(item.mediaId)

        return Futures.transform(fullItemFuture, { fullItem ->
            @OptIn(UnstableApi::class) // MediaMetadata.populate
            val metadata =
                fullItem.mediaMetadata.buildUpon().populate(fullItem.mediaMetadata).build()
            fullItem
                .buildUpon()
                .setMediaMetadata(metadata)
                .setUri(fullItem.localConfiguration?.uri)
                .build()
        }, MoreExecutors.directExecutor())
    }

    fun search(searchQuery: String): ListenableFuture<List<MediaItem>> {
        return stationRepository.searchStations(searchQuery)
            .map { list -> list.map { i -> buildStationItem(i) } }
            .asListenableFuture()
    }

    fun countSearchResults(searchQuery: String): LiveData<Int> {
        return stationRepository.countStationsSearch(searchQuery)
    }

    fun getSearchResults(query: String, page: Int, pageSize: Int): LiveData<List<MediaItem>> {
        val pageOffset = page * pageSize
        return stationRepository.getStationsSearch(query, pageOffset, pageSize)
            .map { list ->
                return@map list.map { this@MediaTreeService.buildStationItem(it) }
            }
    }


    fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }


    fun getChildren(
        parentId: String,
        page: Int,
        pageSize: Int
    ): ListenableFuture<List<MediaItem>> {
        val pageOffset = page * pageSize
        val parentNode = treeNodes[parentId]
        if (parentNode != null && parentNode.getChildren().isNotEmpty()) {
            return Futures.immediateFuture(
                getListPage(
                    parentNode.getChildren(),
                    pageOffset,
                    pageSize
                )
            )
        } else if (parentNode == null) {
            return Futures.immediateFuture(listOf())
        }

        if (parentId == GENRE_ID) {
            return genreRepository.genres
                .map { list -> getListPage(list, pageOffset, pageSize) }
                .map { list -> list.map { g -> buildGenreItem(g) } }
                .asListenableFuture()
        } else if (parentId == COUNTRY_ID) {
            return countryRepository.countrys
                .map { list -> getListPage(list, pageOffset, pageSize) }
                .map { list -> list.map { g -> buildCountryItem(g) } }
                .asListenableFuture()
        } else if (parentId == LANGUAGE_ID) {
            return languageRepository.languages
                .map { list -> getListPage(list, pageOffset, pageSize) }
                .map { list -> list.map { g -> buildLanguageItem(g) } }
                .asListenableFuture()
        } else if (parentId == FAVORITES_ID) {
            return stationRepository.allStationsFavorites
                .map { list -> list.map { g -> buildStationItem(g) } }
                .asListenableFuture()
        } else if (parentId == ALL_ID) {
            return stationRepository.getStationsPage(pageOffset, pageSize)
                .map { list -> list.map { g -> buildStationItem(g) } }
                .asListenableFuture()
        } else if (parentId.startsWith(GENRE_PREFIX)) {
            val genre = parentId.substring(GENRE_PREFIX.length)
            return stationRepository.getStationsByGenre(genre)
                .map { list -> getListPage(list, pageOffset, pageSize) }
                .map { list -> list.map { g -> buildStationItem(g) } }
                .asListenableFuture()
        } else if (parentId.startsWith(COUNTRY_PREFIX)) {
            val country = parentId.substring(COUNTRY_PREFIX.length)
            return stationRepository.getStationsByContry(country)
                .map { list -> getListPage(list, pageOffset, pageSize) }
                .map { list -> list.map { g -> buildStationItem(g) } }
                .asListenableFuture()
        } else if (parentId.startsWith(LANGUAGE_PREFIX)) {
            val language = parentId.substring(LANGUAGE_PREFIX.length)
            return stationRepository.getStationsByLanguage(language)
                .map { list -> getListPage(list, pageOffset, pageSize) }
                .map { list -> list.map { g -> buildStationItem(g) } }
                .asListenableFuture()
        } else {
            return Futures.immediateFuture(listOf())
        }
    }

    fun getItemById(id: String): ListenableFuture<MediaItem> {
        treeNodes[id]?.let { return Futures.immediateFuture(it.item) }
        if (id == ROOT_ID) {
            return Futures.immediateFuture(getRootItem())
        } else if (id == GENRE_ID) {
            return Futures.immediateFuture(treeNodes[GENRE_ID]!!.item)
        } else if (id == COUNTRY_ID) {
            return Futures.immediateFuture(treeNodes[COUNTRY_ID]!!.item)
        } else if (id == LANGUAGE_ID) {
            return Futures.immediateFuture(treeNodes[LANGUAGE_ID]!!.item)
        } else if (id == FAVORITES_ID) {
            return Futures.immediateFuture(treeNodes[FAVORITES_ID]!!.item)
        } else if (id == ALL_ID) {
            return Futures.immediateFuture(treeNodes[ALL_ID]!!.item)

        } else if (id.startsWith(GENRE_PREFIX)) {
            val genre = id.substring(GENRE_PREFIX.length)
            return Futures.immediateFuture(buildGenreItem(genre))
        } else if (id.startsWith(COUNTRY_PREFIX)) {
            val country = id.substring(COUNTRY_PREFIX.length)
            return Futures.immediateFuture(buildCountryItem(country))
        } else if (id.startsWith(LANGUAGE_PREFIX)) {
            val language = id.substring(LANGUAGE_PREFIX.length)
            return Futures.immediateFuture(buildLanguageItem(language))
        } else {
            return buildStationItem(id);
        }
    }

    private fun <T> getListPage(
        list: List<T>,
        pageOffset: Int,
        pageSize: Int
    ): List<T> {
        if (list.size > pageOffset) {
            val length = (list.size - pageOffset).coerceAtMost(pageSize)
            return list.subList(pageOffset, pageOffset + length)
        } else {
            return listOf()
        }
    }

    private fun buildStationItem(id: String): ListenableFuture<MediaItem> {
        val stationId = StationId.parseString(id)
        val future = stationRepository.findStationById(stationId.source, stationId.sourceId)
            .asListenableFuture()
        return Futures.transform(
            future, { station -> buildStationItem(station) },
            MoreExecutors.directExecutor()
        )
    }

    private fun buildStationItem(
        ratedStation: RatedStation,
    ): MediaItem {
        val station = ratedStation.station;
        val fav = ratedStation.favorite;
        val stationId = StationId(station.source, station.sourceId)
        val rating: Rating = HeartRating(fav)

        return buildMediaItem(
            station.name,
            stationId.toString(),
            isPlayable = true,
            isBrowsable = false,
            mediaType = MediaMetadata.MEDIA_TYPE_MUSIC,
            genre = station.genres.firstOrNull(),
            sourceUri = Uri.parse(station.streamUrl),
            imageUri = Uri.parse(station.logoUri),
            descrition = station.description,
            userRating = rating
        )
    }

    private fun buildGenreItem(genre: String): MediaItem {
        val mediaId = GENRE_PREFIX + genre
        val node = MediaItemNode(
            buildMediaItem(
                genre,
                mediaId,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[mediaId] = node
        return node.item
    }

    private fun buildCountryItem(country: String): MediaItem {
        val mediaId = COUNTRY_PREFIX + country
        val node = MediaItemNode(
            buildMediaItem(
                country,
                mediaId,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[mediaId] = node
        return node.item
    }

    private fun buildLanguageItem(language: String): MediaItem {
        val mediaId = LANGUAGE_PREFIX + language
        val node = MediaItemNode(
            buildMediaItem(
                language,
                mediaId,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[mediaId] = node
        return node.item
    }


    private fun createBaseTree() {
        treeNodes[ROOT_ID] = MediaItemNode(
            buildMediaItem(
                "Root",
                ROOT_ID,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[FAVORITES_ID] = MediaItemNode(
            buildMediaItem(
                "Favorites",
                FAVORITES_ID,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[GENRE_ID] = MediaItemNode(
            buildMediaItem(
                "Genres",
                GENRE_ID,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[COUNTRY_ID] = MediaItemNode(
            buildMediaItem(
                "Countries",
                COUNTRY_ID,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[LANGUAGE_ID] = MediaItemNode(
            buildMediaItem(
                "Languages",
                LANGUAGE_ID,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )

        treeNodes[ALL_ID] = MediaItemNode(
            buildMediaItem(
                "All stations",
                ALL_ID,
                isPlayable = false,
                isBrowsable = true,
                mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
            )
        )
        treeNodes[ROOT_ID]!!.addChild(FAVORITES_ID)
        treeNodes[ROOT_ID]!!.addChild(GENRE_ID)
        treeNodes[ROOT_ID]!!.addChild(COUNTRY_ID)
        treeNodes[ROOT_ID]!!.addChild(LANGUAGE_ID)
        treeNodes[ROOT_ID]!!.addChild(ALL_ID)
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        isBrowsable: Boolean,
        mediaType: @MediaMetadata.MediaType Int,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null,
        descrition: String? = null,
        userRating: Rating? = null,
    ): MediaItem {
        val metadata =
            MediaMetadata.Builder()
                .setTitle(title)
                .setGenre(genre)
                .setIsBrowsable(isBrowsable)
                .setIsPlayable(isPlayable)
                .setArtworkUri(imageUri)
                .setMediaType(mediaType)
                .setDescription(descrition)
                .setUserRating(userRating ?: HeartRating(false))
                .build()

        val item = MediaItem.Builder()
            .setMediaId(mediaId)
            .setMediaMetadata(metadata)
            .setUri(sourceUri)
            .build()
        return item;
    }


    private class MediaItemNode(var item: MediaItem) {
        private val children: MutableList<MediaItem> = ArrayList()

        fun addChild(childID: String) {
            this.children.add(treeNodes[childID]!!.item)
        }

        fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }

        private fun normalizeSearchText(text: CharSequence?): String {
            if (text.isNullOrEmpty() || text.trim().length == 1) {
                return ""
            }
            return "$text".trim().lowercase()
        }
    }

}