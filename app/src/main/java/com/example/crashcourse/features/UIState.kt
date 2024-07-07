package com.example.crashcourse.features

data class UIState(
    val isUploading: Boolean = false,
    val images: List<ImageResults> = ArrayList()
)

data class ImageResults(val uid: String, val imageUrl: String,)