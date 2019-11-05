package com.example.bibliotekapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_search.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        logo.setImageResource(R.drawable.logo_napis)
        searchButton.setImageResource(R.drawable.search_image)

        searchButton.setOnClickListener { //nowa aktywnosc po klikneciu przycisku szukaj
           search()
        }

        searchBar.setOnEditorActionListener { _, actionId, _ -> //zatwierdzanie 'enterem'
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
               search()
            }
            false
        }

        searchBar.setOnFocusChangeListener { _, _ -> searchBar.setText("") } //usuwa tekst z searchbara

    }

    fun search(){

            val nowaAktywnosc = Intent(applicationContext, SearchActivity::class.java)
            nowaAktywnosc.putExtra("searched_word", searchBar.text.toString())
            startActivity(nowaAktywnosc)
    }

}
