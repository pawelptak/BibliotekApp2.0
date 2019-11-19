package com.example.bibliotekapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.biblio.DataBaseHelper
import com.example.biblio.LibraryActivity
import com.google.gson.GsonBuilder
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T




class MainActivity : AppCompatActivity()  {
    val db = DataBaseHelper(this)
    var il: ItemList ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent.hasExtra("getFocus")) { //sprawdzam czy wrocono tu z pustej biblioteki
            searchBar.requestFocus()
            searchBar.setText("")
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        scanButton.setOnClickListener {
            initScan()
        }



        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        logo.setImageResource(R.drawable.logo_napis)
        searchButton.setImageResource(R.drawable.search_image)

        searchButton.setOnClickListener { //nowa aktywnosc po klikneciu przycisku szukaj
           search()
            searchBarContainer.isPressed = true
            searchBarContainer.isPressed = false
        }

        searchBar.setOnEditorActionListener { _, actionId, _ -> //zatwierdzanie 'enterem'
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
               search()
            }
            false
        }

        searchBar.setOnFocusChangeListener { _, _ -> searchBar.setText("")//usuwa tekst z searchbara
            searchBarContainer.isPressed = true //animacja searchbara przy nacisnieciu
            searchBarContainer.isPressed = false
        }

        searchBar.setOnClickListener {
            searchBarContainer.isPressed = true
            searchBarContainer.isPressed = false
        }

        libraryButton.setOnClickListener{
            val nowaAktywnosc = Intent(applicationContext, LibraryActivity::class.java)
            //nowaAktywnosc.putExtra("dataBase",db)
            startActivity(nowaAktywnosc)
        }


        addScanButton.setOnClickListener {
            dialogBox.visibility=View.GONE
            if(db.isAdded(il!!.items[0].volumeInfo)){
                Toast.makeText(this,"Ta książka jest już w bibliotece", Toast.LENGTH_LONG).show()
            }else{
                db.insertData(il!!.items[0].volumeInfo)
            }
        }

        cancelScanButton.setOnClickListener {
            dialogBox.visibility=View.GONE
            Toast.makeText(this,"Anulowano", Toast.LENGTH_SHORT).show()

        }


       reset.setOnClickListener {//czysci baze danych. funkcja do testow
            this.deleteDatabase(db.databaseName)
        }


    }

    fun search(){

            val nowaAktywnosc = Intent(applicationContext, SearchActivity::class.java)
            nowaAktywnosc.putExtra("searched_word", searchBar.text.toString())
            startActivity(nowaAktywnosc)
    }

    fun initScan(){
        IntentIntegrator(this).initiateScan()
    }

    fun fetchISBN(isbn: String) {


        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") //zeby nie bylo warna ze niepotrzebna zmienna. potrzebna.
        var jsonString = "cos poszlo nie tak"

        val url: String =
            Uri.parse("https://www.googleapis.com/books/v1/volumes?") //zapytanie do bazy
                .buildUpon()
                .appendQueryParameter("q", "=isbn:"+isbn)
                .appendQueryParameter("printType", "books")
                // .appendQueryParameter("maxResults", "1")
                .appendQueryParameter("key", "AIzaSyCplTkBpwcR-n4DseXuS7806HZcWfz08ms")
                .build().toString()

        Log.e("url", url) //wypisuje przechwycony json w logach
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient()


        client.newCall(request).enqueue(object :
            Callback { //to jest osobny watek z tego co rozumiem, dlatego pobieranie z neta danych nie zatrzymuje apki
            override fun onFailure(call: Call, e: IOException) {
                Log.e("JSON_ERROR", "Nie udalo sie uzyskac odpowiedzi")
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                jsonString = body.toString()
                Log.d("pies",jsonString)
                val gson = GsonBuilder().create() //gson tworzy klasy ItemList, Item, itd. z jsona
                val itemList = gson.fromJson(jsonString, ItemList::class.java)
                il=itemList
                if(itemList.totalItems==0){
                    runOnUiThread {
                        scanProgressBar.visibility=View.GONE
                        Toast.makeText(this@MainActivity,"Nie znaleziono książki", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    runOnUiThread {
                        scanProgressBar.visibility=View.GONE
                        dialogBox.visibility=View.VISIBLE
                        scannedAuthor.text="[Brak autora]"
                            scannedTitle.text=itemList.items[0].volumeInfo.title
                        if(itemList.items[0].volumeInfo.authors!=null){
                            scannedAuthor.text=itemList.items[0].volumeInfo.authors[0]
                        }

                    }
                }


            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode,resultCode, data)
        if (result!=null){
            if(result.contents == null){
                   Toast.makeText(this,"Brak zeskanowanych danych",Toast.LENGTH_SHORT).show()
            }else{
                scanProgressBar.visibility=View.VISIBLE
                fetchISBN(result.contents.toString())
                Toast.makeText(this,"ISBN:"+result.contents.toString(),Toast.LENGTH_LONG).show()

            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    fun isKeyboardVisible(activity: MainActivity): Boolean{
        val imm = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        return imm.isAcceptingText
    }



}

