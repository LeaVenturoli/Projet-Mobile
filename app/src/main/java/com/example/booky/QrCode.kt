package com.example.booky

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import org.json.JSONException
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class QrCode : AppCompatActivity() {

    companion object {
        private const val TAG = "QrCodeActivity"
        const val QR_CODE_CLE = "qr_code_cle"
        const val REQUEST_CAMERA_PERMISSION = 1
    }

    private lateinit var cameraSurfaceView: SurfaceView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var userId: String
    private var isScanned: Boolean = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        userId = intent.getStringExtra("user_id") ?: ""

        cameraSurfaceView = findViewById(R.id.scan_surface)
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        initBarcodeDetector()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isCameraPermissionGranted(requestCode, grantResults)) {
            initBarcodeDetector() // Commencez à scanner les codes QR
        } else {
            Toast.makeText(this, "Autorisation de la caméra refusée.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun isCameraPermissionGranted(requestCode: Int, grantResults: IntArray) =
        requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

    private fun initBarcodeDetector() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        initCameraSource()
        initCameraSurfaceView()

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Log.d(TAG, "Camera has been released.")
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems

                if (barcodes.isNotEmpty() && !isScanned) {
                    barcodes.forEach { _, value ->
                        if (value.displayValue.isNotEmpty()) {
                            isScanned = true
                            onQrCodeScanned(value.displayValue)
                        }
                    }
                }
            }
        })
    }

    private fun initCameraSource() {
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun initCameraSurfaceView() {
        cameraSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@QrCode,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource.start(cameraSurfaceView.holder)
                    } else {
                        ActivityCompat.requestPermissions(
                            this@QrCode,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.release()
            }
        })
    }

    private fun onQrCodeScanned(qrCodeCle: String) {
        val isbn = qrCodeCle.trim()
        val url = "https://openlibrary.org/search.json?q=isbn:$isbn"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val docs = response.optJSONArray("docs")
                    if (docs != null && docs.length() > 0 ){
                        val bookInfo = docs.optJSONObject(0)
                        val title = bookInfo.optString("title", "Titre inconnu")
                        val author = bookInfo.optString("author_name", "Auteur inconnu")
                        val editionArray = bookInfo.optJSONArray("publisher")
                        val editions = ArrayList<String>()
                        if (editionArray != null) {
                            for (i in 0 until editionArray.length()) {
                                editions.add(editionArray.optString(i))
                            }
                        }
                        editions.sort()
                        if (editions.isEmpty()) editions.add("Editeur inconnu")

                        val coverId = bookInfo.optJSONArray("cover_i")?.optInt(0) ?: 0
                        val coverUrl = "https://covers.openlibrary.org/b/id/$coverId-L.jpg"

                        val intent = Intent(this@QrCode, BookDetails::class.java).apply {
                            putExtra("user_id", userId)
                            putExtra("title", title)
                            putExtra("author", author)
                            putStringArrayListExtra("editions", editions)
                            putExtra("coverUrl", coverUrl)
                        }
                        isScanned = true;
                        startActivity(intent)
                        finish()
                    } else {
                        isScanned = false;
                        Toast.makeText(
                            this@QrCode,
                            "Aucune information trouvée pour ce livre.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    isScanned = false;
                    Toast.makeText(
                        this@QrCode,
                        "Erreur lors de la recherche du livre.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(
                    this@QrCode,
                    "Erreur réseau lors de la recherche du livre.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        Volley.newRequestQueue(this@QrCode).add(request)
    }
}