package fr.lea.booky

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ReportFragment.Companion.reportFragment

class Scan : AppCompatActivity() {

    private lateinit var qrCode: TextView
    private lateinit var userId: String


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val result = it.data?.getStringExtra(QrCode.QR_CODE_CLE)
                updateQrCodeResultTextView(result)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        
        userId = intent.getStringExtra("user_id") ?: ""

        qrCode = findViewById(R.id.qr_Code)
        initButtonClickListener()
    }

    private fun initButtonClickListener() {
        val startScanButton = findViewById<Button>(R.id.lancerScan)
        startScanButton.setOnClickListener {
            val intentQrCode = Intent(this, QrCode::class.java)
            intentQrCode.putExtra("user_id", userId)
            resultLauncher.launch(intentQrCode)
        }
    }

    private fun updateQrCodeResultTextView(result: String?) {
        runOnUiThread {
            qrCode.text = result ?: "Aucun code QR scanné"
        }
    }

    }
