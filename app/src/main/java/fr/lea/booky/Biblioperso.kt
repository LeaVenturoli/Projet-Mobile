package fr.lea.booky

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import com.android.volley.toolbox.JsonObjectRequest
import com.squareup.picasso.Picasso
import org.json.JSONObject

data class Livre(
    val nom: String,
    val auteur: String,
    val tome: Any, // Peut être Int ou String
    val imageUrl: String
)

class LivresAdapter(private val context: Context, private val livresList: List<Livre>) : BaseAdapter() {

    override fun getCount(): Int {
        return livresList.size
    }

    override fun getItem(position: Int): Livre {
        return livresList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ResourceType")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.drawable.item_livre, parent, false)

        val livre = getItem(position)

        // Récupère les vues à partir du layout de l'élément du livre
        val nomTextView = view.findViewById<TextView>(R.id.nomTextView)
        val auteurTextView = view.findViewById<TextView>(R.id.auteurTextView)
        val tomeTextView = view.findViewById<TextView>(R.id.tomeTextView)
        val imageView = view.findViewById<ImageView>(R.id.imageView)


        nomTextView.text = livre.nom
        auteurTextView.text = livre.auteur
        tomeTextView.text = livre.tome.toString()

        Picasso.get().load(livre.imageUrl).resize(200, 250).centerCrop().into(imageView)


        return view
    }
}
class Biblioperso : AppCompatActivity() {
    private lateinit var listViewLivres: ListView
    private lateinit var livresList: ArrayList<Livre>
    private lateinit var adapter: LivresAdapter
    private lateinit var userId: String
    private lateinit var rechercheEditText: EditText

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioperso)

        rechercheEditText = findViewById(R.id.recherche)


        userId = intent.getStringExtra("user_id").toString()

        if (userId == null) {
            // Gère le cas où userId est null si nécessaire
        }

        livresList = ArrayList() // Initialisation de livresList
        listViewLivres = findViewById(R.id.listViewLivres)

        // Utilise la propriété de classe adapter au lieu de redéclarer une variable locale
        adapter = LivresAdapter(this, livresList)
        listViewLivres.adapter = adapter


        rechercheEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this implementation
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this implementation
                val query = s.toString()
                getLivresFromApi(userId, query)
            }

            override fun afterTextChanged(s: Editable?) {
                // pass
            }
        })
        try {
            getLivresFromApi(userId,null);
        } catch (e: JSONException) {
            e.printStackTrace()
            Log.e("Biblioperso", "Erreur JSON: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Biblioperso", "Erreur: ${e.message}")
        }


    }

    private fun getLivresFromApi(userId:String, query:String?) {
        livresList.clear()
        val queue = Volley.newRequestQueue(this)
        val url = "https://booky-bibliotheque.fr/Api_V1/livres/get.php"
        val params = HashMap<String, String>()
        params["user_id"] = userId;
        params["query"] = query ?: "" // Use query if not null, otherwise use ""
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
                        val imageUrl = "https://booky-bibliotheque.fr/images-livres/" + livre.getString("IMAGE")
                        livresList.add(Livre(nomLivre, auteur, tome, imageUrl)) // Ajoute le livre à la liste
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
