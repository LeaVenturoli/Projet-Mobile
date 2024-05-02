package fr.lea.booky

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

        userId = intent.getStringExtra("user_id") ?: ""
        val prenom = intent.getStringExtra("prenom") ?: ""


        welcomeTextView = findViewById(R.id.welcomeTextView)
        numberOfBooksTextView = findViewById(R.id.numberOfBooksTextView)
        buttonAjouter = findViewById(R.id.buttonAjouter)
        buttonBiblio = findViewById(R.id.buttonBiblio)
        buttonSouhait = findViewById(R.id.buttonSouhait)


        buttonAjouter.setOnClickListener {
            val mainActivity = Intent(this@Register, Scan::class.java)
            mainActivity.putExtra("user_id", userId)
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

        welcomeTextView.text = "Bienvenue $prenom"


        updateNumberOfBooks()
    }


    @SuppressLint("SetTextI18n")
    private fun updateNumberOfBooks() {

        getNumberOfBooks(userId) { numberOfBooks ->

            numberOfBooksTextView.text = "Vous avez $numberOfBooks livres"
        }
    }


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

                        callback(10)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()

                    callback(100)
                }
            },
            { error ->
                error.printStackTrace()

                callback(0)
            }
        )

        queue.add(jsonObjectRequest)
    }

}