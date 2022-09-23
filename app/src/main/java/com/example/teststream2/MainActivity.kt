package com.example.teststream2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pedro.encoder.input.video.CameraOpenException
import com.pedro.rtplibrary.rtsp.RtspCamera1
import com.example.teststream2.utils.PathUtils
import com.pedro.rtsp.utils.ConnectCheckerRtsp
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * More documentation see:
 * [com.pedro.rtplibrary.base.Camera1Base]
 * [com.pedro.rtplibrary.rtsp.RtspCamera1]
 */
class MainActivity : AppCompatActivity(), ConnectCheckerRtsp, View.OnClickListener,
    SurfaceHolder.Callback {
    private var rtspCamera1: RtspCamera1? = null
    private var button: Button? = null
    private var bRecord: Button? = null
    private var etUrl: EditText? = null
    private var currentDateAndTime = ""
    private var folder: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permission1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                101
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        folder = PathUtils.getRecordPath()
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        button = findViewById(R.id.b_start_stop)
        button?.setOnClickListener(this)
        bRecord = findViewById(R.id.b_record)
        bRecord?.setOnClickListener(this)
        val switchCamera = findViewById<Button>(R.id.switch_camera)
        switchCamera.setOnClickListener(this)
        etUrl = findViewById(R.id.et_rtp_url)
        etUrl?.setHint(R.string.hint_rtsp)
        rtspCamera1 = RtspCamera1(surfaceView, this)
        rtspCamera1?.setReTries(10)
        surfaceView.holder.addCallback(this)
    }

    override fun onConnectionStartedRtsp(rtspUrl: String) {}
    override fun onConnectionSuccessRtsp() {
        runOnUiThread {
            Toast.makeText(
                this,
                "Connection success",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onConnectionFailedRtsp(reason: String) {
        runOnUiThread {
            if (rtspCamera1?.reTry(5000, reason, null) == true) {
                Toast.makeText(this, "Retry", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    "Connection failed. $reason",
                    Toast.LENGTH_SHORT
                )
                    .show()
                rtspCamera1?.stopStream()
                button?.setText(R.string.start_button)
            }
        }
    }

    override fun onNewBitrateRtsp(bitrate: Long) {}
    override fun onDisconnectRtsp() {
        runOnUiThread {
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthErrorRtsp() {
        runOnUiThread {
            Toast.makeText(this, "Auth error", Toast.LENGTH_SHORT).show()
            rtspCamera1?.stopStream()
            button?.setText(R.string.start_button)
        }
    }

    override fun onAuthSuccessRtsp() {
        runOnUiThread {
            Toast.makeText(this, "Auth success", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.b_start_stop -> if (!rtspCamera1?.isStreaming()!!) {
                if (rtspCamera1!!.isRecording()
                    || rtspCamera1!!.prepareAudio() && rtspCamera1!!.prepareVideo()
                ) {
                    button?.setText(R.string.stop_button)
                    rtspCamera1!!.startStream(etUrl!!.text.toString())
                } else {
                    Toast.makeText(
                        this, "Error preparing stream, This device cant do it",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                button?.setText(R.string.start_button)
                rtspCamera1!!.stopStream()
            }
            R.id.switch_camera -> try {
                rtspCamera1?.switchCamera()
            } catch (e: CameraOpenException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
            R.id.b_record -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!rtspCamera1?.isRecording()!!) {
                    try {
                        if (!folder!!.exists()) {
                            folder!!.mkdir()
                        }
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        currentDateAndTime = sdf.format(Date())
                        if (!rtspCamera1?.isStreaming()!!) {
                            if (rtspCamera1!!.prepareAudio() && rtspCamera1!!.prepareVideo()) {
                                rtspCamera1!!.startRecord(
                                    folder!!.absolutePath + "/" + currentDateAndTime + ".mp4"
                                )
                                bRecord?.setText(R.string.stop_record)
                                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    this, "Error preparing stream, This device cant do it",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            rtspCamera1!!.startRecord(
                                folder!!.absolutePath + "/" + currentDateAndTime + ".mp4"
                            )
                            bRecord?.setText(R.string.stop_record)
                            Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IOException) {
                        rtspCamera1?.stopRecord()
                        PathUtils.updateGallery(
                            this,
                            folder!!.absolutePath + "/" + currentDateAndTime + ".mp4"
                        )
                        bRecord?.setText(R.string.start_record)
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    rtspCamera1?.stopRecord()
                    PathUtils.updateGallery(
                        this,
                        folder!!.absolutePath + "/" + currentDateAndTime + ".mp4"
                    )
                    bRecord?.setText(R.string.start_record)
                    Toast.makeText(
                        this,
                        "file " + currentDateAndTime + ".mp4 saved in " + folder!!.absolutePath,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}
    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        rtspCamera1?.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtspCamera1?.isRecording() == true) {
            rtspCamera1?.stopRecord()
            PathUtils.updateGallery(this, folder!!.absolutePath + "/" + currentDateAndTime + ".mp4")
            bRecord?.setText(R.string.start_record)
            Toast.makeText(
                this,
                "file " + currentDateAndTime + ".mp4 saved in " + folder!!.absolutePath,
                Toast.LENGTH_SHORT
            ).show()
            currentDateAndTime = ""
        }
        if (rtspCamera1?.isStreaming() == true) {
            rtspCamera1?.stopStream()
            button!!.text = resources.getString(R.string.start_button)
        }
        rtspCamera1?.stopPreview()
    }
}