package com.example.booky

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Register : AppCompatActivity() {
    private var buttonAjouter: Button? = null
    private var buttonBiblio: Button? = null
    private var buttonSouhait: Button? = null
    private var userId: String? = null // Déclarer la variable userId ici

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Récupérer le prénom et l'ID utilisateur transmis depuis MainActivity
        userId = intent.getStringExtra("user_id")
        val prenom = intent.getStringExtra("prenom")

        // Initialisation des boutons après la récupération de userId
        buttonAjouter = findViewById(R.id.buttonAjouter)
        buttonBiblio = findViewById(R.id.buttonBiblio)
        buttonSouhait = findViewById(R.id.buttonSouhait)

        // Configuration des listeners pour les boutons après la récupération de userId
        buttonAjouter?.setOnClickListener(View.OnClickListener {
            val mainActivity = Intent(this@Register, Scan::class.java)
            startActivity(mainActivity)
        })

        buttonBiblio?.setOnClickListener(View.OnClickListener {
            val biblioIntent = Intent(this@Register, Biblioperso::class.java)
            userId?.let { biblioIntent.putExtra("user_id", it) } // Utilisation de userId après sa récupération
            startActivity(biblioIntent)
        })

        buttonSouhait?.setOnClickListener(View.OnClickListener {
            val mainActivity = Intent(this@Register, Souhait::class.java)
            startActivity(mainActivity)
        })

        // Afficher le message de bienvenue avec le prénom de l'utilisateur
        if (userId != null && prenom != null) {
            val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
            welcomeTextView.text = "Bienvenue $prenom"

        }

        // Ajout du code de débogage
        Log.d("RegisterActivity", "Prénom récupéré : $prenom")
    }
}
