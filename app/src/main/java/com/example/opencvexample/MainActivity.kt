package com.example.opencvexample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.opencvexample.databinding.ActivityMainBinding
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.core.Core.flip
import org.opencv.imgproc.Imgproc
import java.util.*


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

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val mRgba = inputFrame?.rgba()!!

        val contours = findContours(mRgba)
        Imgproc.drawContours(mRgba, findContours(mRgba), 0, Scalar(0.0, 255.0, 0.0), 5)
        val quad = getQuadrilateral(contours)
        quad?.points?.forEach {
            Log.d("DWAKWADKDAWKWADKWAKD", "${it.x} ${it.y}")
        }
        val points = quad?.points?.map { MatOfPoint(it) }
        Imgproc.drawContours(mRgba, points, 0, Scalar(255.0, 0.0, 0.0), 15)

        return mRgba
    }

    private fun findContours(src: Mat): ArrayList<MatOfPoint> {
        val ratio = src.size().height / 500
        val height = java.lang.Double.valueOf(src.size().height / ratio).toInt()
        val width = java.lang.Double.valueOf(src.size().width / ratio).toInt()
        val size = Size(width.toDouble(), height.toDouble())
        val resizedImage = Mat(size, CvType.CV_8UC4)
        val grayImage = Mat(size, CvType.CV_8UC4)
        val cannedImage = Mat(size, CvType.CV_8UC1)
        Imgproc.resize(src, resizedImage, size)
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4)
        Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
        Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(
            cannedImage,
            contours,
            hierarchy,
            Imgproc.RETR_LIST,
            Imgproc.CHAIN_APPROX_SIMPLE
        )
        hierarchy.release()
        contours.sortWith { lhs, rhs ->
            java.lang.Double.valueOf(Imgproc.contourArea(rhs))
                .compareTo(Imgproc.contourArea(lhs))
        }
        resizedImage.release()
        grayImage.release()
        cannedImage.release()
        return contours
    }

    private fun getQuadrilateral(contours: ArrayList<MatOfPoint>): Quadrilateral? {
        for (c in contours) {
            val c2f = MatOfPoint2f(*c.toArray())
            val peri = Imgproc.arcLength(c2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)
            val points = approx.toArray()

            // select biggest 4 angles polygon
            if (points.size == 4) {
                val foundPoints: Array<Point> = sortPoints(points)
                return Quadrilateral(c, foundPoints)
            }
        }
        return null
    }

    class Quadrilateral(var contour: MatOfPoint, var points: Array<Point>)

    private fun sortPoints(src: Array<Point>): Array<Point> {
        val srcPoints: ArrayList<Point> = ArrayList(src.toList())
        val result = arrayOf<Point>(Point(), Point(), Point(), Point())
        val sumComparator: Comparator<Point> =
            Comparator<Point> { lhs, rhs -> java.lang.Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x) }
        val diffComparator: Comparator<Point> =
            Comparator<Point> { lhs, rhs -> java.lang.Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x) }

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator)

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator)

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator)

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator)
        return result
    }
}