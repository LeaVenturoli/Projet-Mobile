package com.example.booky

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.booky.databinding.ActivityBookDetailsBinding
import android.widget.Button
import java.text.SimpleDateFormat
import java.util.Date
import android.widget.EditText
import com.example.booky.R
import com.example.booky.Scan

class BookDetails : AppCompatActivity() {
    private lateinit var binding: ActivityBookDetailsBinding

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titleText = intent.getStringExtra("title")
        var authorText = intent.getStringExtra("author")


        authorText = authorText?.replace("[", "")?.replace("]", "")?.removeSurrounding("\"")

        binding.title.text = titleText
        binding.author.text = authorText

        val button = findViewById<Button>(R.id.backButton)

        button.setOnClickListener{
            val intent = Intent(this, Scan::class.java)
            startActivity(intent) // Démarrer la nouvelle activité
            finish()
        }


        val currentDate = SimpleDateFormat("dd/MM/yyyy").format(Date())
        binding.date.text = currentDate


        val genreEditText = findViewById<EditText>(R.id.genre)
        val editionEditText = findViewById<EditText>(R.id.edition)
        val tomeEditText = findViewById<EditText>(R.id.tome)

    }
}