package com.example.booky

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var errorConnectAccountTextView: TextView? = null
    private var mailEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var connectBtn: Button? = null
    private var MAIL: String? = null
    private var MDP: String? = null
    private var databaseManager: DatabaseManager? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        errorConnectAccountTextView = findViewById(R.id.errorConnectAccountTextView)
        mailEditText = findViewById<EditText>(R.id.MailEditText)
        passwordEditText = findViewById<EditText>(R.id.PasswordEditText)
        connectBtn = findViewById<Button>(R.id.connectBtn)
        databaseManager = DatabaseManager(applicationContext)
        connectBtn?.setOnClickListener(View.OnClickListener {
            MAIL = mailEditText?.text.toString()
            MDP = passwordEditText?.text.toString()
            connectUser(MDP, MAIL)
        })
    }

    private fun onApiResponse(response: JSONObject) {
        var success: Boolean? = null
        var error: String? = ""
        try {
            success = response.getBoolean("success")
            if (success == true) {
                val interfaceActivity = Intent(
                    applicationContext,
                    Register::class.java
                )
                interfaceActivity.putExtra("mail", MAIL)
                startActivity(interfaceActivity)
                finish()
            } else {
                error = response.getString("error")
                errorConnectAccountTextView!!.visibility = View.VISIBLE
                errorConnectAccountTextView!!.text = error
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun connectUser(MAIL: String?, MDP: String?) {
        val url = "http://192.168.1.62:8080/User/login.php"
        val params = HashMap<String, String>()
        params["MAIL"] = MAIL ?: ""
        params["MDP"] = MDP ?: ""
        val parameters = JSONObject(params as Map<*, *>?)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, parameters,
            { response ->
                onApiResponse(response)
            },
            { error ->
                val errorMessage = error?.message ?: "Erreur inconnue lors de la connexion"
                Toast.makeText(
                    this@MainActivity,
                    "Erreur lors de la connexion: $errorMessage",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )


        databaseManager!!.queue.add(jsonObjectRequest)
    }
}



