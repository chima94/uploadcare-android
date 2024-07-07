package com.example.crashcourse.features

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.uploadcare.android.library.api.UploadcareClient
import com.uploadcare.android.library.api.UploadcareFile
import com.uploadcare.android.library.callbacks.UploadFileCallback
import com.uploadcare.android.library.callbacks.UploadFilesCallback
import com.uploadcare.android.library.callbacks.UploadcareAllFilesCallback
import com.uploadcare.android.library.exceptions.UploadcareApiException
import com.uploadcare.android.library.upload.FileUploader
import com.uploadcare.android.library.upload.MultipleFilesUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UIViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(UIState());
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    private val client = UploadcareClient("f7fad90997166cd1dc45","fb145e2ad5c6758728db");

    fun onEvent(event: UIEvent){
        when(event){
            is UIEvent.SingleImageChanged ->{
                uploadSingleImage(event.context, event.uri)
            }
            is UIEvent.MultipleImageChanged->{
                uploadMultipleImages(event.uris, event.context)
            }
            is UIEvent.GetImages->{
                getImages()
            }
            is UIEvent.DeleteImage->{
                deleteImage(event.uid, event.context)
            }
        }
    }

    private fun uploadSingleImage(context: Context, uri: Uri) {
        _uiState.value = UIState(isUploading = true)

            val uploader = FileUploader(client, uri, context).store(true);

            uploader.uploadAsync(object : UploadFileCallback {
                override fun onFailure(e: UploadcareApiException) {
                    Log.i("ERROR", e.message.toString())
                }

                override fun onProgressUpdate(
                    bytesWritten: Long,
                    contentLength: Long,
                    progress: Double
                ) {
                    // Upload progress info.
                }

                override fun onSuccess(result: UploadcareFile) {
                    _uiState.value = UIState(isUploading = false)
                }
            })

    }


    private fun uploadMultipleImages(uris: List<Uri>, context: Context){
        _uiState.value = UIState(isUploading = true)

            val uploader = MultipleFilesUploader(client, uris, context).store(true)

            uploader.uploadAsync(object : UploadFilesCallback {
                override fun onFailure(e: UploadcareApiException) {
                    Log.i("ERROR", e.message.toString())
                }

                override fun onProgressUpdate(
                    bytesWritten: Long,
                    contentLength: Long,
                    progress: Double
                ) {
                    // Upload progress info.
                }

                override fun onSuccess(result: List<UploadcareFile>) {
                    _uiState.value = UIState(isUploading = false)
                }
            })

    }

    private fun getImages(){
        _uiState.value = UIState(isUploading = true)
        val images = ArrayList<ImageResults>()
        client.getFiles().asListAsync(object : UploadcareAllFilesCallback{
            override fun onFailure(e: UploadcareApiException) {
                TODO("Not yet implemented")
            }

            override fun onSuccess(result: List<UploadcareFile>) {
                result.forEach {
                    images.add(
                        ImageResults(
                            uid = it.uuid,
                            imageUrl = it.originalFileUrl.toString()
                        )
                    )
                }
                _uiState.value = UIState(images = images, isUploading = false)
            }

        })
    }

    private fun deleteImage(uid: String, context: Context){
        client.deleteFileAsync(context, uid)
        val images = _uiState.value.images.filterNot { it.uid == uid }
        _uiState.value = UIState(images = images)
    }
}





