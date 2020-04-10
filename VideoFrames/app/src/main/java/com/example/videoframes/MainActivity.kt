package com.example.videoframes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private var abc: VideoView? = null
    private var selectedImageUri: Uri? = null
    private var mediaMetadataRetriever: MediaMetadataRetriever? = null
    private lateinit var recyclerView: RecyclerView
    private var seek1: SeekBar? = null
    private var downLoadButton: Button?=null
    private var seek2: SeekBar? = null
    private var allAdapter: RecyclerAdapter? = null
    var bitmapper1 = ArrayList<Bitmap>()

    private var a: Int = 0
    private var b: Int = 100
    private var starter: Int = 0
    private var ender: Int = 100
    val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        seek1 = findViewById(R.id.firstProgress)
        seek2 = findViewById(R.id.secondProgress)
        abc = findViewById(R.id.surfaceView)
        downLoadButton= findViewById(R.id.button)
        downLoadButton?.setOnClickListener {
            sendWorkMAnager(bitmapper1)
        }
        recyclerView = findViewById(R.id.recicler)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        seek1?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                a = seekBar?.progress!!
                changeInFrames()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        seek2?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                b = seekBar?.progress!!
                changeInFrames()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        permisssion
    }

    fun changeInFrames() {
        if (a > b) {
            starter = b
            ender = a
        } else if (b > a) {
            starter = a
            ender = b
        } else {
            starter = 0
            ender = 100
        }

        startRetriverWork()
    }

    /**
     * To get run time permission
     */
    private val permisssion: Unit
        private get() {
            if ((ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1)
                }
            } else {
                videoGallery
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            videoGallery
        }
    }

    /**
     * To open video gallery
     */
    private val videoGallery: Unit
        private get() {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Video"), 2)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            selectedImageUri = data!!.data
            abc!!.setVideoURI(selectedImageUri)
            abc!!.start()
            abc!!.setOnPreparedListener { mp -> mp.isLooping = true }
            startRetriverWork()
        }
    }

    /**
     *To retrive bitmap from uri
     */
    private fun startRetriverWork() {
        val tRetriever = MediaMetadataRetriever()


        tRetriever.setDataSource(baseContext, selectedImageUri)
        mediaMetadataRetriever = tRetriever
        uiScope.launch {
            processBitmap(mediaMetadataRetriever!!)
        }




    }


    /**
     * Coroutine to remove jankiness
     */
    private suspend fun processBitmap(mediaMetadataRetriever: MediaMetadataRetriever) {
        withContext(Dispatchers.Default) {
            val bitmapper = ArrayList<Bitmap>()
            val DURATION = mediaMetadataRetriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION)

            var maxDur = (1000 * DURATION.toDouble()).toLong()
            maxDur = ((ender * maxDur) / 100)

            var i: Long = (starter * maxDur) / 100

            while (i < maxDur) {
                bitmapper.add(getResizedBitmap(mediaMetadataRetriever.getFrameAtTime(i), 60))
                i = i + maxDur / 10
            }

            withContext(Dispatchers.Main) {
              bitmapper1=bitmapper
                allAdapter = RecyclerAdapter(bitmapper, applicationContext)
                recyclerView.adapter = allAdapter
            }
        }

    }

    private fun sendWorkMAnager(bitmapper: ArrayList<Bitmap>) {
        var abc = arrayOfNulls<String>(bitmapper.size)
        val data = Data.Builder()
        var i = 0
        while (i < bitmapper.size) {
//            createDirectoryAndSaveFile(bitmapper.get(i),Integer.toString(i))
            abc[i] = BitMapToString(bitmapper.get(i))
            data.putString("array", abc[i])
            val workRequest = OneTimeWorkRequestBuilder<MyWorker>().setInputData(data.build())
                    .build()
            WorkManager.getInstance(this@MainActivity).enqueue(workRequest)
            i++
        }


    }

    private fun createDirectoryAndSaveFile(imageToSave: Bitmap, fileName: String) {
        val direct = File(Environment.getExternalStorageDirectory().absolutePath.toString() + "/frames")
        if (!direct.exists()) {
            val wallpaperDirectory = File("/sdcard/frames/")
            wallpaperDirectory.mkdirs()
        }
        val file = File( "frames")
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     *To decrease the size of bitmap
     */
    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun BitMapToString(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }
}

