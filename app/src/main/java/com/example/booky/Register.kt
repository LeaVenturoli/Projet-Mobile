package com.example.booky

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Register : AppCompatActivity() {
    private lateinit var buttonAjouter: Button
    private lateinit var buttonBiblio: Button
    private lateinit var buttonSouhait: Button
    private lateinit var userId: String
    private lateinit var welcomeTextView: TextView
    private lateinit var numberOfBooksTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Récupérer l'ID utilisateur et le prénom transmis depuis MainActivity
        userId = intent.getStringExtra("user_id") ?: ""
        val prenom = intent.getStringExtra("prenom") ?: ""

        // Références aux vues
        welcomeTextView = findViewById(R.id.welcomeTextView)
        numberOfBooksTextView = findViewById(R.id.numberOfBooksTextView)
        buttonAjouter = findViewById(R.id.buttonAjouter)
        buttonBiblio = findViewById(R.id.buttonBiblio)
        buttonSouhait = findViewById(R.id.buttonSouhait)

        // Configuration des listeners pour les boutons
        buttonAjouter.setOnClickListener {
            val mainActivity = Intent(this@Register, Scan::class.java)
            startActivity(mainActivity)
        }

        buttonBiblio.setOnClickListener {
            val biblioIntent = Intent(this@Register, Biblioperso::class.java)
            biblioIntent.putExtra("user_id", userId)
            startActivity(biblioIntent)
        }

        buttonSouhait.setOnClickListener {
            val souhaitIntent = Intent(this@Register, Souhait::class.java)
            souhaitIntent.putExtra("user_id", userId)
            startActivity(souhaitIntent)
        }

        // Afficher le message de bienvenue avec le prénom de l'utilisateur
        welcomeTextView.text = "Bienvenue $prenom"

        // Mettre à jour le nombre de livres
        updateNumberOfBooks()
    }

    // Méthode pour mettre à jour le nombre de livres dans l'interface utilisateur
    @SuppressLint("SetTextI18n")
    private fun updateNumberOfBooks() {
        // Appel de l'API pour récupérer le nombre de livres
        getNumberOfBooks(userId) { numberOfBooks ->
            // Mettre à jour le TextView avec le nombre de livres
            numberOfBooksTextView.text = "Vous avez $numberOfBooks livres"
        }
    }

    // Méthode pour récupérer le nombre de livres depuis l'API
    private fun getNumberOfBooks(userId: String, callback: (Int) -> Unit) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://booky-bibliotheque.fr/Api_V1/livres/getNbr.php"

        val params = HashMap<String, String>()
        params["user_id"] = userId

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val numberOfBooks = response.getInt("nombre_livre")
                        callback(numberOfBooks)
                    } else {
                        // Si la requête n'est pas un succès, renvoyer 0 livres
                        callback(10)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // En cas d'erreur lors de l'analyse de la réponse JSON, renvoyer 0 livres
                    callback(100)
                }
            },
            { error ->
                error.printStackTrace()
                // En cas d'erreur réseau ou d'erreur de l'API, renvoyer 0 livres
                callback(0)
            }
        )

        queue.add(jsonObjectRequest)
    }

}