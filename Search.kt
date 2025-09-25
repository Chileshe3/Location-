package com.example.musicplayer.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import com.example.musicplayer.model.Song
import com.example.musicplayer.viewmodel.MusicPlayerViewModel
import com.example.musicplayer.viewmodel.PlayListViewModel
import com.example.musicplayer.ui.components.InlineSearchBar
import com.example.musicplayer.ui.components.filterByQuery
import com.example.musicplayer.ui.components.MusicScreenOptionsMenu
import com.example.musicplayer.ui.components.LongPressHandler
import com.example.musicplayer.ui.components.SelectionToolbar
import com.example.musicplayer.ui.components.SongItem
import com.example.musicplayer.ui.components.SortByDialog
import com.example.musicplayer.ui.components.SortOption
import com.example.musicplayer.ui.components.sortBySortOption
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.launch
import com.example.musicplayer.utils.RenameSong
import kotlin.math.abs

// Custom smooth fling behavior for natural scrolling
@Composable
private fun rememberSmoothFlingBehavior(): FlingBehavior {
    val density = LocalDensity.current
    return remember(density) {
        object : FlingBehavior {
            private val decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay()

            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {

                val adjustedVelocity = initialVelocity * 0.8f // Slightly reduce initial velocity for smoother feel

                return if (abs(adjustedVelocity) > 1f) {
                    val animationState = AnimationState(
                        initialValue = 0f,
                        initialVelocity = adjustedVelocity,
                    )

                    var lastValue = 0f
                    animationState.animateDecay(decayAnimationSpec) {
                        val delta = value - lastValue
                        val consumed = scrollBy(delta)
                        lastValue = value

                        // Stop animation if we can't scroll anymore
                        if (abs(delta - consumed) > 0.5f) {
                            cancelAnimation()
                        }
                    }

                    animationState.velocity
                } else {
                    initialVelocity
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MusicScreen(
    viewModel: MusicPlayerViewModel,
    playlistViewModel: PlayListViewModel,
    onNavigateToFullPlayer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPlaylists: () -> Unit,
    deleteResultLauncher: ActivityResultLauncher<IntentSenderRequest>

) {
    val songs by viewModel.songs.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val favoriteSongIds by playlistViewModel.favoriteSongIds.collectAsState()

    var showOverflowMenu by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var currentSortOption by remember { mutableStateOf(SortOption.TITLE_ASC) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Enhanced smooth scroll state
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Custom smooth fling behavior for natural momentum scrolling
    val smoothFlingBehavior = rememberSmoothFlingBehavior()

    val showSnackbar: (String) -> Unit = remember {
        { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    // Initialize the long press state
    val longPressState = LongPressHandler.rememberLongPressState(
        viewModel = viewModel,
        playlistViewModel = playlistViewModel,
        coroutineScope = coroutineScope,
        deleteResultLauncher = deleteResultLauncher,
        showSnackbar = showSnackbar
    )

    // derivedStateOf to prevent unnecessary recompositions
    val filteredSongs by remember {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                songs
            } else {
                songs.filterByQuery(searchQuery)
            }
        }
    }
    
    val onSongRenamed = remember {
        {
            // Refresh the songs list from the viewModel
            viewModel.refreshSongs()
        }
    }

    // Use derivedStateOf for displayed songs with sorting applied
    val displayedSongs by remember {
        derivedStateOf {
            val baseList = if (isSearchActive && searchQuery.isNotEmpty()) {
                filteredSongs
            } else {
                songs
            }
            baseList.sortBySortOption(currentSortOption)
        }
    }

    Scaffold(
        topBar = {
            if (longPressState.value.isInSelectionMode) {
                SelectionToolbar(
                    longPressState = longPressState,
                    playlistViewModel = playlistViewModel,
                    coroutineScope = coroutineScope,
                    deleteResultLauncher = deleteResultLauncher,
                    showSnackbar = showSnackbar,
                    viewModel = viewModel,
                    allSongs = songs
                )
            } else {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Music Player",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "${displayedSongs.size} songs",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 32.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Box {
                                    IconButton(onClick = { showOverflowMenu = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    MusicScreenOptionsMenu(
                                        expanded = showOverflowMenu,
                                        onDismiss = { showOverflowMenu = false },
                                        onSettingsClick = onNavigateToSettings,
                                        onSortByClick = { showSortDialog = true },
                                        onViewModeClick = { /* TODO: Toggle between list/grid view */ },
                                    )
                                }
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Inline Search Bar (WhatsApp style)
            if (isSearchActive) {
                InlineSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { query -> searchQuery = query },
                    onBackClick = { 
                        isSearchActive = false
                        searchQuery = ""
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                // Filter Tabs (only show when not searching and not in selection mode)
                if (!longPressState.value.isInSelectionMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        TabText("All", selectedTab == 0) { selectedTab = 0 }
                        TabText("Playlists", false) { onNavigateToPlaylists() }
                        TabText("Folders", selectedTab == 2) { selectedTab = 2 }
                    }
                }
            }

            // Content based on search state or selected tab
            when {
                isSearchActive -> {
                    // Search Results (displayed in the same list style)
                    if (searchQuery.isEmpty()) {
                        // Show message when search is active but no query entered
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start typing to search songs...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (displayedSongs.isEmpty()) {
                        // No results found
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No results found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try searching for something else",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Display search results in the same list format
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = 4.dp,
                                bottom = 120.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                            flingBehavior = smoothFlingBehavior,
                            userScrollEnabled = true,
                            reverseLayout = false
                        ) {
                            items(
                                items = displayedSongs,
                                key = { song -> song.id },
                                contentType = { "song_item" }
                            ) { song ->
                                SongItem(
                                    song = song,
                                    currentSong = currentSong,
                                    isPlaying = isPlaying,
                                    longPressState = longPressState,
                                    viewModel = viewModel,
                                    playlistViewModel = playlistViewModel,
                                    favoriteSongIds = favoriteSongIds,
                                    deleteResultLauncher = deleteResultLauncher,
                                    showSnackbar = showSnackbar,
                                    onSongRenamed = onSongRenamed
                                )
                            }
                        }
                    }
                }
                selectedTab == 0 -> {
                    // All Songs Tab
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 4.dp,
                            bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        flingBehavior = smoothFlingBehavior,
                        userScrollEnabled = true,
                        reverseLayout = false
                    ) {
                        items(
                            items = displayedSongs,
                            key = { song -> song.id },
                            contentType = { "song_item" }
                        ) { song ->
                            SongItem(
                                song = song,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                longPressState = longPressState,
                                viewModel = viewModel,
                                playlistViewModel = playlistViewModel,
                                favoriteSongIds = favoriteSongIds,
                                deleteResultLauncher = deleteResultLauncher,
                                showSnackbar = showSnackbar,
                                onSongRenamed = onSongRenamed
                            )
                        }
                    }
                }
                selectedTab == 2 -> {
                    // Initialize folder view model
                    val folderViewModel = remember { FolderViewModel() }

                    FoldersScreen(
                        viewModel = viewModel,
                        playlistViewModel = playlistViewModel,
                        folderViewModel = folderViewModel,
                        deleteResultLauncher = deleteResultLauncher
                    )
                }
            }
        }

        // Sort Dialog
        SortByDialog(
            showDialog = showSortDialog,
            currentSortOption = currentSortOption,
            onDismiss = { showSortDialog = false },
            onSortSelected = { sortOption ->
                currentSortOption = sortOption
                showSortDialog = false
            }
        )
    }
}

@Composable
private fun TabText(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier.clickable { onClick() },
        style = MaterialTheme.typography.titleMedium,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
}
