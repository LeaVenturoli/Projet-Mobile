package fr.lea.booky

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.widget.Toast.*
import androidx.appcompat.app.AppCompatActivity
import fr.lea.booky.databinding.ActivityBookDetailsBinding
import okhttp3.*
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64

class BookDetails : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding
    private lateinit var userId: String
    private val REQUEST_IMAGE_GALLERY = 8080
    private lateinit var buttonAjouterPhoto: Button
    private lateinit var ResultPhoto: ImageView

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchGenres()
        afficherEditions()

        val titleText = intent.getStringExtra("title")
        var authorText = intent.getStringExtra("author")
        userId = intent.getStringExtra("user_id") ?: ""

        authorText = authorText?.replace("[", "")?.replace("]", "")?.removeSurrounding("\"")

        binding.title.text = titleText
        binding.author.text = authorText

        val button = findViewById<Button>(R.id.backButton)
        button.setOnClickListener {
            val intent = Intent(this, Scan::class.java)
            startActivity(intent)
            finish()
        }

        val currentDate = SimpleDateFormat("dd/MM/yyyy").format(Date())
        binding.date.text = currentDate

        buttonAjouterPhoto = findViewById(R.id.chooseImageButton)
        ResultPhoto = findViewById(R.id.imageView)

        buttonAjouterPhoto.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_IMAGE_GALLERY)
        }

        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            val genre = binding.genreSpinner.selectedItem.toString()
            val edition = binding.editionSpinner.selectedItem.toString()
            val tome = binding.tome.text.toString()
            val addToWishlist = binding.wishlistCheckBox.isChecked

            if (genre?.isNotEmpty() == true && edition?.isNotEmpty() == true) {
                sendBookDataToServer(genre, edition, tome.toIntOrNull(), addToWishlist)
                val addIntent = Intent(this@BookDetails, Register::class.java)
                addIntent.putExtra("user_id", userId)
                startActivity(addIntent)
            } else {
                makeText(this, "Veuillez remplir tous les champs", LENGTH_SHORT).show()
            }
        }
    }

    private fun afficherEditions() {
        val editions = intent.getStringArrayListExtra("editions")
        val spinner : Spinner = findViewById(R.id.editionSpinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, editions!!)
        spinner.adapter = adapter
    }

    private fun fetchGenres() {
        val url = "https://booky-bibliotheque.fr/Api_V1/Genre/get.php"
        val request = Request.Builder()
            .url(url)
            .build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread{
                    makeText(this@BookDetails, "Impossible de récupérer les genres", LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { jsonString ->
                    val genresArray = JSONArray(jsonString)
                    val genres = Array(genresArray.length()) { i -> genresArray.getString(i) }
                    runOnUiThread {
                        val spinner : Spinner = findViewById(R.id.genreSpinner)
                        val adapter = ArrayAdapter(this@BookDetails, android.R.layout.simple_spinner_dropdown_item, genres)
                        spinner.adapter = adapter
                    }
                }
            }
        })
    }

    private fun sendBookDataToServer(genre: String, edition: String, tome: Int?, addToWishlist: Boolean)  {
        val url = "https://booky-bibliotheque.fr/Api_V1/livres/post.php"
        val drawable = ResultPhoto.drawable

        // Convert the drawable to a Bitmap
        val bitmap = (drawable as BitmapDrawable).bitmap

        // Convert the Bitmap to a byte array
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val imageByteArray: ByteArray = outputStream.toByteArray()

        // Convert the byte array to a base64-encoded string
        val imageBase64: String = Base64.encodeToString(imageByteArray, Base64.DEFAULT)

        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("ID_UTILISATEUR", userId)
            put("GENRE", genre)
            put("EDITIONS", edition)
            put("NOM_LIVRE", binding.title.text.toString())
            put("AUTEUR", binding.author.text.toString())
            put("DATE_AJOUT", binding.date.text.toString())
            tome?.let { put("TOME", it) }
            put("SOUHAIT", addToWishlist)
            put("IMAGES",imageBase64)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    makeText(
                        this@BookDetails,
                        "Erreur de connexion au serveur",
                        LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (!responseBody.isNullOrEmpty()) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val success = jsonResponse.getBoolean("success")
                        val message = if (success) {
                            jsonResponse.getString("message")
                        } else {
                            jsonResponse.getString("error")
                        }

                        Log.d("Response", "Response: $message")

                        runOnUiThread {
                            if (success) {
                                makeText(this@BookDetails, message, LENGTH_SHORT).show()
                            } else {
                                makeText(this@BookDetails, message, LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: JSONException) {
                        runOnUiThread {
                            makeText(
                                this@BookDetails,
                                "Erreur lors de l'analyse des données: ${e.message}",
                                LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        makeText(
                            this@BookDetails,
                            "La réponse du serveur était vide",
                            LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    companion object {
        private const val REQUEST_IMAGE_GALLERY = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            ResultPhoto.setImageBitmap(photo)
        }
    }
}