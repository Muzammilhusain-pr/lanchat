package com.example.lanchat.media

import android.content.ContentResolver
import android.net.Uri
import java.io.File

/**
 * Handles sending IMAGE / VIDEO / DOCUMENT attachments over the local
 * network. Two directions:
 *
 *  - upload(): read the picked file's bytes via ContentResolver, POST them
 *    to the host's LocalServer at http://<hostIp>:<httpPort>/files, get
 *    back a URL, then send a WireMessage(type = IMAGE/VIDEO/DOCUMENT,
 *    fileUrl = that URL) over SignalingClient so peers know a new file exists.
 *
 *  - download(): given a fileUrl from an incoming WireMessage, GET it from
 *    the host and save it into this device's local cache/media dir so it
 *    shows up in the chat thread.
 *
 * Both are plain HTTP over the LAN -- no internet, no cloud storage.
 */
class FileTransferManager(
    private val contentResolver: ContentResolver,
    private val hostAddress: String,
    private val httpPort: Int,
    private val downloadDir: File
) {

    sealed class TransferResult {
        data class Success(val urlOrPath: String) : TransferResult()
        data class Failure(val error: Throwable) : TransferResult()
    }

    /** Upload a picked file (image/video/document Uri) to the host. Call off the main thread. */
    suspend fun upload(uri: Uri, fileName: String): TransferResult {
        // TODO:
        //  1. contentResolver.openInputStream(uri) to read bytes
        //  2. multipart/form-data POST to http://$hostAddress:$httpPort/files
        //  3. parse the JSON response for the served URL
        //  4. return TransferResult.Success(url)
        return TransferResult.Failure(NotImplementedError("upload() not wired yet"))
    }

    /** Download a file the host is serving at [fileUrl] and store it locally. Call off the main thread. */
    suspend fun download(fileUrl: String, fileName: String): TransferResult {
        // TODO: plain HTTP GET to fileUrl (already a full http://<hostIp>:<port>/files/... URL),
        // stream the response body into File(downloadDir, fileName), return its local path.
        return TransferResult.Failure(NotImplementedError("download() not wired yet"))
    }
}
