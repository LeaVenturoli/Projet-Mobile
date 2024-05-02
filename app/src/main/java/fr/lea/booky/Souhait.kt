package fr.lea.booky

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class Souhait : AppCompatActivity() {
    private lateinit var listViewLivres: ListView
    private lateinit var livresList: ArrayList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_souhait)

        userId = intent.getStringExtra("user_id").toString()

        if (userId.isNullOrEmpty()) {
            // Gérer le cas où userId est nul ou vide
            // Vous pouvez afficher un message d'erreur ou prendre d'autres mesures nécessaires ici
            Toast.makeText(this@Souhait, "ID utilisateur non valide", Toast.LENGTH_SHORT).show()
            return
        }

        livresList = ArrayList() // Initialisation de livresList
        listViewLivres = findViewById(R.id.listViewLivres)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, livresList)
        listViewLivres.adapter = adapter


        // Récupération des livres souhaités de l'utilisateur depuis l'API
        getLivresFromApi(userId)
    }

    private fun getLivresFromApi(userId: String) {
        Log.d("SouhaitActivity", "Envoi de la demande de récupération des livres depuis l'API pour l'utilisateur $userId")

        val queue = Volley.newRequestQueue(this)
        val url = "https://booky-bibliotheque.fr/Api_V1/livres/getSouhait.php"
        val params = HashMap<String, String>()
        params["user_id"] = userId;

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (!success) {
                        Log.d("SouhaitActivity", "La demande de récupération des livres a échoué pour l'utilisateur $userId")
                        Toast.makeText(this@Souhait, "Erreur lors de la récupération des livres", Toast.LENGTH_SHORT).show()
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
                    Log.d("SouhaitActivity", "Livres récupérés avec succès depuis l'API pour l'utilisateur $userId")
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("SouhaitActivity", "Erreur lors de l'analyse de la réponse JSON")
                }
            },
            {
                Log.e("SouhaitActivity", "Erreur lors de la requête HTTP pour récupérer les livres depuis l'API")
                Toast.makeText(this@Souhait, "Erreur lors de l'affichage", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }
    }

