package com.example.biblio

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.biblio.MyAdapter.myClickListener
import com.example.bibliotekapp.Book
import com.example.bibliotekapp.ItemList
import com.example.bibliotekapp.MainActivity
import com.example.bibliotekapp.R
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book.*
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit


@Suppress("SENSELESS_COMPARISON")
class BookActivity : AppCompatActivity(),  myClickListener {

    var itemList: ItemList? = null

    override fun startActivity(position: Int) { //nowa aktywnosc po wscisnieciu elemtu 'wiecej od autora'
        val nowaAktywnosc = Intent(applicationContext, BookActivity::class.java)
       // Toast.makeText(this, position.toString(), Toast.LENGTH_SHORT).show()
        nowaAktywnosc.putExtra("itemPos", position)
        nowaAktywnosc.putExtra("itemList",itemList)
        startActivity(nowaAktywnosc)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
      //  this.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val pos: Int = intent.getIntExtra("itemPos", 0)

        bookRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) //to docelowo ma byc poziomo scrollowana lista innych ksiazek tego autora


        var url = "no url"
        var fetchedTitle = "[Brak tytułu]"
        var fetchedAuthors = "[Brak autora]"
        var firstAuthor = "[Brak autora]"
        var fetchedPublisher = ""
        var fetchedDescription = "Brak opisu."
        var fetchedRating = "-"
        var pages = "-"
        var ratingCount = 0
        var fetchedRatingCountString = "-"
        // var fetchedPrice="N/A" //nie ma sensu ta cena bo zadna ksiazka nie ma jej wpisanej


        if (intent.hasExtra("itemList")) {
          itemList= intent.extras?.get("itemList") as ItemList
        }

        var thisBook:Book
        if(itemList!=null) { //jesli ksiazka jest z listy wyszukiwania
             thisBook= itemList!!.items[pos].volumeInfo
        }else{ //jesli ksiazka jest z biblioteki (bazy danych)
            val db = DataBaseHelper(this)
            val bookList=db.readData()
            thisBook=bookList[pos]
            db.close()
        }

        val db = DataBaseHelper(this)
        val isInLibrary=db.isAdded(thisBook)
        db.close()
        when(isInLibrary){ //ustawienie ikonki dodawania do biblioteki
            true->addButton.setImageResource(R.drawable.addedlibrary_icon)
            false->addButton.setImageResource(R.drawable.addlibrary_icon)
        }


        fetchedTitle=thisBook.title
        if (thisBook.authors != null) {
            fetchedAuthors = thisBook.authors.joinToString(", ")
            firstAuthor = thisBook.authors[0]
        }
        if(thisBook.description != null) fetchedDescription=thisBook.description
        if (thisBook.imageLinks != null) url=thisBook.imageLinks.smallThumbnail
        if (thisBook.averageRating != null) fetchedRating=thisBook.averageRating.toString()
        fetchedRatingCountString="(ocen: "+thisBook.ratingsCount.toString()+")"
        pages=thisBook.pageCount.toString()
        if(thisBook.publisher!= null) fetchedPublisher="Wydawnictwo: "+thisBook.publisher

        url = url.replace("http", "https").replace("&edge=curl", "")

        //ustawiam okladke
        Picasso.with(bookViewCover.context)
            .load(url)
            .placeholder(R.drawable.nocover)
            .into(bookViewCover)

        if(fetchedAuthors=="[Brak autora]") { //jesli nie ma autora to nie wyswietlaj wiecej ksiazek od autora
            moreFromContainer.visibility= View.GONE
            bookRecyclerView.visibility=View.GONE
        }else{
            fetchMoreFromAuthor(firstAuthor,fetchedTitle)
        }

        bookViewTitle.text = fetchedTitle
        bookViewAuthor.text=fetchedAuthors
        bookViewAuthor2.text=firstAuthor
        bookViewPublisher.text=fetchedPublisher
        bookViewDescription.text= fetchedDescription
        rating.text=fetchedRating
        if(pages=="0")pageCount.text="N/A"
        else pageCount.text=pages
        numRatings.text=fetchedRatingCountString

        bookViewDescription.post { //inaczej sie nie da wyciagnac liczby linii przed wyrenderowniem tekstu na ekran
            kotlin.run {
                val lines=bookViewDescription.lineCount
                if(lines>6){ //jesli opis jest dluzszy niz 6 linii to nie pokazuje calego i mozna rozwijac
                    bookViewDescription.setCollapseLines(6)
                    bookViewDescription.setExpandEnabled(true)
                    bookViewDescription.setExpand(false)
                }
            }
        }


        addButton.setOnClickListener{
                   saveToDataBase(thisBook)
        }

        homeButton.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        }


    }


    override fun onSupportNavigateUp(): Boolean { //wraca do glownego ekranu po wcisnieciu strzalki
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        return true
    }




    fun saveToDataBase(book: Book){
        val db = DataBaseHelper(this)
        if(!db.isAdded(book)){
            db.insertData(book)
            addButton.setImageResource(R.drawable.addedlibrary_icon)
        }else{
            addButton.setImageResource(R.drawable.addlibrary_icon)
            db.deleteData(book)
        }
        db.close()

    }


    private fun fetchMoreFromAuthor(author: String, title:String) {
        runOnUiThread {
            progressBar2.visibility = View.VISIBLE
            bookRecyclerView.visibility=View.GONE
            moreFromContainer.visibility= View.GONE
        }
        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER") //zeby nie bylo warna ze niepotrzebna zmienna. potrzebna.
        var jsonString = "cos poszlo nie tak"


        val url: String =
            Uri.parse("https://www.googleapis.com/books/v1/volumes?") //zapytanie do bazy
                .buildUpon()
                .appendQueryParameter("q", "author:"+author) //+"-intitle:"+title -> niezbyt to chcialo dzialac
        //-"+title+"+intitle:"
              //  .appendQueryParameter("printType", "books")
                .appendQueryParameter("key", "AIzaSyCplTkBpwcR-n4DseXuS7806HZcWfz08ms")
                .build().toString()

        Log.e("url", url) //wypisuje przechwycony json w logach
        val request = Request.Builder().url(url).build()

        val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build()


        client.newCall(request).enqueue(object :
            Callback { //to jest osobny watek z tego co rozumiem, dlatego pobieranie z neta danych nie zatrzymuje apki
            override fun onFailure(call: Call, e: IOException) {
                Log.e("JSON_ERROR", "Nie udalo sie uzyskac odpowiedzi")
                moreFromContainer.visibility= View.GONE
                bookRecyclerView.visibility=View.GONE
                progressBar2.visibility=View.GONE

            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                jsonString = body.toString()
                Log.d("pies",jsonString)
                val gson = GsonBuilder().create() //gson tworzy klasy ItemList, Item, itd. z jsona
                val list = gson.fromJson(jsonString, ItemList::class.java)
                itemList=list //zmiena globalna = gowno
                if(list.totalItems==0){ //to sie chyba nigdy nie stanie
                    runOnUiThread {
                        moreFromContainer.visibility= View.GONE
                        bookRecyclerView.visibility=View.GONE
                        progressBar2.visibility=View.GONE
                    }
                }
                else {
                    runOnUiThread {
                        bookRecyclerView.adapter= MyAdapter(list, this@BookActivity) //dzieki temu wyswietlaja sie poszczegolne elementy na ekranie
                        progressBar2.visibility=View.GONE
                        bookRecyclerView.visibility=View.VISIBLE
                        moreFromContainer.visibility= View.VISIBLE
                    }
                }


            }
        })
    }


}





