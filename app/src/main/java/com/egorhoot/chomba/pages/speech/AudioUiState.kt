package com.egorhoot.chomba.pages.speech

data class AudioUiState (
    val audioPermissionGranted: Boolean = false,
    val audioPermissionDenied: Boolean = false,
)