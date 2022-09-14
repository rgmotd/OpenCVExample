package com.example.opencvexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.opencvexample.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat


class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding
    var baseLoaderCallback: BaseLoaderCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OpenCVLoader.initDebug()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        baseLoaderCallback = object : BaseLoaderCallback(this) {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    SUCCESS -> {
                        binding.camera.enableView()
                    }
                    else -> super.onManagerConnected(status)
                }
            }
        }

        binding.camera.visibility = View.VISIBLE
        binding.camera.setCvCameraViewListener(this)
        binding.camera.setCameraPermissionGranted()
    }

    override fun onResume() {
        super.onResume()
        baseLoaderCallback?.onManagerConnected(BaseLoaderCallback.SUCCESS)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val img = inputFrame?.rgba()
        return img!!
    }
}