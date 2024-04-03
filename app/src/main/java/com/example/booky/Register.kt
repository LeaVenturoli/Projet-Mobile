package com.example.booky

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Register : AppCompatActivity() {
    private var buttonAjouter: Button? = null
    private var buttonBiblio: Button? = null
    private var buttonSouhait: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        buttonAjouter = findViewById(R.id.buttonAjouter)
        buttonAjouter?.setOnClickListener(View.OnClickListener {
            val mainActivity = Intent(this@Register, Scan::class.java)
            startActivity(mainActivity)
        })

        buttonBiblio = findViewById(R.id.buttonBiblio)
        buttonBiblio?.setOnClickListener(View.OnClickListener {
            val mainActivity = Intent(this@Register, Biblioperso::class.java)
            startActivity(mainActivity)
        })

        buttonSouhait = findViewById(R.id.buttonSouhait)
        buttonSouhait?.setOnClickListener(View.OnClickListener {
            val mainActivity = Intent(this@Register, Souhait::class.java)
            startActivity(mainActivity)
        })
    }
}