package com.example.opencvexample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.opencvexample.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Core.flip
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {
    private lateinit var binding: ActivityMainBinding
    var baseLoaderCallback: BaseLoaderCallback? = null
    private var mat: Mat? = null
    private var gray: Mat? = null
    private var canny: Mat? = null

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
        gray = Mat(width, height, CvType.CV_8UC1)
        canny = Mat(width, height, CvType.CV_8UC1)
    }

    override fun onCameraViewStopped() {
    }

    fun rot90(matImage: Mat, rotflag: Int): Mat? {
        //1=CW, 2=CCW, 3=180
        var rotated: Mat? = Mat()
        if (rotflag == 1) {
            rotated = matImage.t()
            flip(rotated, rotated, 1) //transpose+flip(1)=CW
        } else if (rotflag == 2) {
            rotated = matImage.t()
            flip(rotated, rotated, 0) //transpose+flip(0)=CCW
        } else if (rotflag == 3) {
            flip(matImage, rotated, -1) //flip(-1)=180
        } else if (rotflag != 0) { //if not 0,1,2,3:
        }
        return rotated
    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val img: Mat? = inputFrame?.rgba()

        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2BGRA)

        val imgResult = img!!.clone()

        Imgproc.Canny(img, imgResult, 80.0, 90.0)


       return imgResult
    }
}