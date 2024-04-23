package fr.lea.booky

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var mailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var connectBtn: Button
    private var MAIL: String? = null
    private var MDP: String? = null
    private var databaseManager: DatabaseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mailEditText = findViewById(R.id.MailEditText)
        passwordEditText = findViewById(R.id.PasswordEditText)
        connectBtn = findViewById(R.id.connectBtn)
        databaseManager = DatabaseManager(applicationContext)

        connectBtn.setOnClickListener {
            MAIL = mailEditText.text.toString()
            MDP = passwordEditText.text.toString()
            connectUser(MAIL!!, MDP!!)
        }
    }

    private fun onApiResponse(response: JSONObject) {
        try {
            val success = response.getBoolean("success")
            if (success) {
                val userId = response.getString("ID_UTILISATEUR") // Récupérez l'`ID_UTILISATEUR` depuis la réponse JSON
                val prenom = response.getString("prenom")

                val intent = Intent(this@MainActivity, Register::class.java)
                intent.putExtra("user_id", userId) // Ajoutez l'`ID_UTILISATEUR` à l'intent
                intent.putExtra("MAIL", MAIL)
                intent.putExtra("prenom", prenom)
                startActivity(intent)
                finish()
            } else {
                val error = response.getString("error")
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_LONG).show()
        }
    }



    private fun connectUser(mail: String, mdp: String) {
        val url = "https://booky-bibliotheque.fr/Api_V1/User/login.php"
        val params = HashMap<String, String>()
        params["MAIL"] = mail
        params["MDP"] = mdp
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, (params as Map<*, *>?)?.let { JSONObject(it) },
            { response ->
                onApiResponse(response)
            },
            { error ->
                val errorMessage = error?.message ?: "Erreur inconnue lors de la connexion"
                Toast.makeText(this@MainActivity, "Erreur lors de la connexion: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
        databaseManager!!.queue.add(jsonObjectRequest)
    }
}
