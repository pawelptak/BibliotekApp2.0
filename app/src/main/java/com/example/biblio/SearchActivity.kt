package com.example.bibliotekapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_search.*
import okhttp3.*
import java.io.IOException
import java.io.Serializable
import java.util.concurrent.TimeUnit

/**
 * Lista książek pasujących do wyszukanej frazy
 */
class SearchActivity : AppCompatActivity(), MyAdapter.myClickListener  {
    /**
     * Zmienna przechowująca listę elementów otrzymaną od funkcji fetchJsonString(). Przekazywana jest do BookActivity w celu wyświetlenie informacji o odpowiedniej książce.
     */
    var il: ItemList ?=null

    /**
     * Włącza nową aktywność - BookActivity (zawierającą więcej informacji o książce) po wciśnięciu danej pozycji z listy
     * @param item Indeks wciśniętej pozycji na liście
     */
    override fun startActivity(position: Int) {
      //  Toast.makeText(this,"DD",Toast.LENGTH_SHORT).show()
        val nowaAktywnosc = Intent(applicationContext, BookActivity::class.java)
      //  Toast.makeText(this, position.toString(), Toast.LENGTH_SHORT).show()
        nowaAktywnosc.putExtra("itemPos", position)
        nowaAktywnosc.putExtra("itemList",il)
        startActivity(nowaAktywnosc)

    }

    /**
     * Aktualnie wybrany tryb sortowania. Domyślnie "releveace" - wg trafności. Przekazywany jako paramter do funkcji fetchJsonString()
     */
    var sorting = "relevance"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        searchRecyclerView.layoutManager = LinearLayoutManager(this)

        val bundle: Bundle? = intent.extras
        val message = bundle?.getString("searched_word").toString() //przechwytuje z ekranu glownego slowo wpisane w searchbar

       if(message!="null") {
           fetchJsonString(message, sorting)
           searchBar2.setText(message)
       }


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) //zeby nie wyskakiwala od razu klawiatura

        searchBar2.requestFocus()


        searchButton2.setOnClickListener {
            fetchJsonString(searchBar2.text.toString(), sorting)
            searchBar2.hideKeyboard()
            searchBarContainer2.isPressed = true
            searchBarContainer2.isPressed = false
        }

        searchBar2.setOnEditorActionListener { _, actionId, _ -> //zatwierdzanie 'enterem'
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                fetchJsonString(searchBar2.text.toString(), sorting)
                searchBar2.hideKeyboard()
            }
            false
        }

        searchBar2.setOnFocusChangeListener { _: View, _: Boolean ->
            searchBarContainer2.isPressed = true //animacja searchbara przy nacisnieciu
            searchBarContainer2.isPressed = false
        }

        searchBar2.setOnClickListener {
            searchBarContainer2.isPressed = true
            searchBarContainer2.isPressed = false
        }
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * Funkcja pobiera JSON z Google Books Api przy pomocy biblioteki OkHttp i zapisuje go do obiektu klasy ItemList.
     * @param phrase Wyszukiwana fraza
     * @param order Sortowanie. Dwie możliwości: "relevance" - najtrafniejsze, "newest" - najnowsze
     */
    fun fetchJsonString(phrase: String, order: String){

        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            searchRecyclerView.visibility=View.GONE
        }

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") //zeby nie bylo warna ze niepotrzebna zmienna. potrzebna.
        var jsonString = "cos poszlo nie tak"

        val url: String =
            Uri.parse("https://www.googleapis.com/books/v1/volumes?") //zapytanie do bazy
                .buildUpon()
                .appendQueryParameter("q", phrase)
                .appendQueryParameter("printType", "books")
                .appendQueryParameter("orderBy", order)
                 // .appendQueryParameter("maxResults", "1")
                //  .appendQueryParameter("projection", "lite")
                .appendQueryParameter("key", "AIzaSyCplTkBpwcR-n4DseXuS7806HZcWfz08ms")
                .build().toString()

        Log.e("url", url) //wypisuje przechwycony json w logach
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build()

        client.newCall(request).enqueue(object :
            Callback { //to jest osobny watek z tego co rozumiem, dlatego pobieranie z neta danych nie zatrzymuje apki
            override fun onFailure(call: Call, e: IOException) {
                Log.e("JSON_ERROR", "Nie udalo sie uzyskac odpowiedzi")
                searchRecyclerView.adapter = NotFoundAdapter(false)
                searchRecyclerView.visibility=View.VISIBLE
                progressBar.visibility=View.GONE
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                jsonString = body.toString()
                Log.d("pies",jsonString)
                val gson = GsonBuilder().create() //gson tworzy klasy ItemList, Item, itd. z jsona
                val itemList = gson.fromJson(jsonString, ItemList::class.java)
                il=itemList
                if(itemList.totalItems==0){
                    runOnUiThread {
                        searchRecyclerView.adapter = NotFoundAdapter(true)
                        searchRecyclerView.visibility=View.VISIBLE
                        progressBar.visibility=View.GONE
                    }
                }
                else {
                    runOnUiThread {
                           searchRecyclerView.adapter= MyAdapter(itemList, this@SearchActivity) //dzieki temu wyswietlaja sie poszczegolne elementy na ekranie
                        searchRecyclerView.visibility=View.VISIBLE
                        progressBar.visibility=View.GONE
                    }
                }

            }

        })
    }


    /**
     * Funkcja chowa systemową klawiaturę
     */
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    /**
     * Aktualnie wybrana w menu sortowania pozycja. 1 - wg trafności, 2 - od najnowszych
     */
    var itemSelection = 1 //aktualnie wybrany tryb sortowania (trafnosc)

    /**
     * Funkcja odpowiada za aktualny wygląd menu sortowania w zależności od wybranego trybu sortowania
     */
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.sort_menu, menu)
        val item1 = menu?.findItem(R.id.relevanceButton)
        val item2 = menu?.findItem(R.id.lastButton)
        when (itemSelection) {
            1 -> item1?.setChecked(true)
            2 -> item2?.setChecked(true)
        }
    }

    /**
     * Obsługa wyboru opcji w menu sortowania
     * @param item Funkcja przyjmuje jako parametr obiekt klasy MenuItem - element menu.
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.relevanceButton -> {
                Toast.makeText(this, "Sortowanie: trafność", Toast.LENGTH_SHORT).show()
                item.setChecked(true)
                itemSelection = 1
                sorting="relevance"
                fetchJsonString(searchBar2.text.toString(), "relevance")
                return true
            }
            R.id.lastButton -> {
                Toast.makeText(this, "Sortowanie: najnowsze", Toast.LENGTH_SHORT).show()
                item.setChecked(true)
                itemSelection = 2
                sorting="newest"
                fetchJsonString(searchBar2.text.toString(), "newest")
                return true
            }

        }
        return super.onContextItemSelected(item)
    }

    /**
     * Wyświetlanie menu sortowania. Funkcja uruchamiana jest po wciśnięciu odpowiedniego przycisku na ekranie.
     */
    fun showSortMenu(v: View){
        registerForContextMenu(v)
        openContextMenu(v)
    }
}


//klasy zgodne z jsonem z Google Api: ItemList - lista wszystkiego, Item - element listy, Book - ksiazka, ImageLink - tablica linkow do okladek
/**
 * Lista znalezionych elementów. Zawiera tablicę obiektow klasy Item.
 * @param totalItems Liczba elementów w liście
 * @param items Tablica obiektów klasy Item
 */
class ItemList(
    val totalItems: Int, val items: Array<Item>
): Serializable

/**
 * Element listy ItemList
 * @param id Numer ID elementu
 * @param volumeInfo Obiekt klasy Book - książka
 * @param note Notatka na temat książki. Dodawana przez użytkownika w bibliotece.
 */
class Item(
    val id: String,
    val volumeInfo: Book,
    val note: String
): Serializable

/**
 * Książka
 * @param title Tytuł
 * @param authors Autorzy
 * @param imageLinks Adresy url okładek. Obiekt klasy ImageLink
 * @param description Opis
 * @param averageRating Średnia ocena
 * @param ratingsCount Liczba ocen
 * @param publisher Wydawnictwo
 * @param pageCount Liczba stron
 */
class Book(
    val title: String,
    val authors: Array<String>,
    val imageLinks: ImageLink,
    val description: String,
    val averageRating: Double,
    val ratingsCount: Int,
    val publisher: String,
    val pageCount: Int
): Serializable

/**
 * Klasa zawierająca adresy URL okładek. Zawiera tylko jeden parametr, ponieważ większość książek posiada tylko miniaturkę okładki (smallThumbnail)
 * @param smallThumbnail Adres URL miniaturki okładki o niskiej rozdzielczości
 */
class ImageLink(
    val smallThumbnail: String
): Serializable
