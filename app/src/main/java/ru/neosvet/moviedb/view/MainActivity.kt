package ru.neosvet.moviedb.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ru.neosvet.moviedb.R

class MainActivity : AppCompatActivity() {
    private lateinit var etSearch: TextInputEditText
    private lateinit var btnSearch: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews();
    }

    private fun initViews() {
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener {
            Toast.makeText(this, etSearch.text, Toast.LENGTH_SHORT).show()
        }
    }
}