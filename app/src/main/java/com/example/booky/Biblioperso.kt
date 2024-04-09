package com.example.booky

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
class Biblioperso : AppCompatActivity() {
    private lateinit var listViewLivres: ListView
    private lateinit var livresList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioperso)

        userId = intent.getStringExtra("user_id").toString()

        if (userId == null) {

        }
        livresList = ArrayList() // Initialisation de livresList
        listViewLivres = findViewById(R.id.listViewLivres)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, livresList)
        listViewLivres.adapter = adapter

        // Correction de l'initialisation de userIdTextView
        val userIdTextView = findViewById<TextView>(R.id.userIdTextView)
        userIdTextView.text = "ID de l'utilisateur : $userId"

        getLivresFromApi(userId)

    }

    private fun getLivresFromApi(userId:String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://booky-bibliotheque.fr/Api_V1/livres/get.php"
        val params = HashMap<String, String>()
        params["user_id"] = userId;
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, (params as Map<*, *>?)?.let { JSONObject(it) },
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (!success) {
                        // Si la requête n'est pas un succès, affichez un message approprié
                        Toast.makeText(this@Biblioperso, "Erreur lors de la récupération des livres", Toast.LENGTH_SHORT).show()
                    }
                    val livresArray = response.getJSONArray("livres")
                    for (i in 0 until livresArray.length()) {
                        val livre = livresArray.getJSONObject(i)
                        val nomLivre = livre.getString("NOM_LIVRE")
                        val auteur = livre.getString("AUTEUR")
                        val tome = if (!livre.isNull("TOME")) {
                            livre.getInt("TOME")
                        } else {
                            "Tome inconnu"
                        }
                        livresList.add("$nomLivre - $auteur - ${if (tome is Int) "Tome $tome" else tome}")


                    }
                    adapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            {
                Toast.makeText(this@Biblioperso, "Erreur lors de l'affichage", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }
}

