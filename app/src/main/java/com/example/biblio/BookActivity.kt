package com.example.bibliotekapp

import android.content.ClipDescription
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.media.Rating
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.biblio.DataBaseHelper
import com.example.biblio.TableInfo
import com.example.bibliotekapp.MyAdapter.myClickListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book.*
import kotlinx.android.synthetic.main.activity_search.*

class BookActivity() : AppCompatActivity(),  myClickListener {
    override fun startActivity(position: Int) {

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }


        var pos: Int = intent.getIntExtra("itemPos", 0)

        bookRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) //to docelowo ma byc poziomo scrollowana lista innych ksiazek tego autora
        bookRecyclerView.adapter=MyAdapter(il!!, this)

        var url = "no url"
        //sprawdzam czy sa linki do okladek
        if(il?.items?.get(pos)?.volumeInfo?.imageLinks !=null)  url = il!!.items[pos].volumeInfo.imageLinks.smallThumbnail
        url=url.replace("http","https").replace("&edge=curl","")

        //ustawiam okladke
        Picasso.with(bookViewCover.context)
            .load(url)
            .placeholder(R.drawable.nocover)
            .into(bookViewCover)


        var fetchedTitle="[Brak tytułu]"
        var fetchedAuthor="[Brak autora]"
        var fetchedPublisher=""
        var fetchedDescription = "Brak opisu."
        var fetchedRating="-"
        val pages= il?.items?.get(pos)?.volumeInfo?.pageCount.toString()
        var ratingCount =il?.items?.get(pos)?.volumeInfo?.ratingsCount
        var fetchedRatingCountString="(ocen: "+ratingCount.toString()+")"
       // var fetchedPrice="N/A" //nie ma sensu ta cena bo zadna ksiazka nie ma jej wpisanej, ale nie mam czym zapelnic miejsca



        if (il?.items?.get(pos)?.volumeInfo?.title !=null) fetchedTitle = il!!.items[pos].volumeInfo.title


        if (il?.items?.get(pos)?.volumeInfo?.authors !=null){
            var i=0
            fetchedAuthor=""
            for (item in il!!.items[pos].volumeInfo.authors){
                fetchedAuthor+=item
                if(i!= il!!.items[pos].volumeInfo.authors.count()-1)fetchedAuthor+=", "
                ++i

            }
        }

        if (il?.items?.get(pos)?.volumeInfo?.publisher !=null) fetchedPublisher ="Wydawnictwo: "+il!!.items[pos].volumeInfo.publisher
        if (il?.items?.get(pos)?.volumeInfo?.description !=null) fetchedDescription = il!!.items[pos].volumeInfo.description
        if (il?.items?.get(pos)?.volumeInfo?.averageRating !=null) fetchedRating = il!!.items[pos].volumeInfo.averageRating.toString()
       // if (il?.items?.get(pos)?.volumeInfo?.retailPrice !=null) fetchedPrice = il!!.items[pos].volumeInfo.retailPrice.amount.toString()+" "+il!!.items[pos].volumeInfo.retailPrice.currencyCode

        bookViewTitle.text = fetchedTitle
        bookViewAuthor.text=fetchedAuthor
        bookViewPublisher.text=fetchedPublisher
        bookViewDescription.text= fetchedDescription
        rating.text=fetchedRating
        if(pages=="0")pageCount.text="N/A"
            else pageCount.text=pages
        numRatings.text=fetchedRatingCountString


        addButton.setOnClickListener{
            if (ratingCount != null) {
                saveToDataBase(fetchedTitle, il!!.items[pos].volumeInfo.authors, url, fetchedPublisher, fetchedDescription, il!!.items[pos].volumeInfo.averageRating, ratingCount, il!!.items?.get(pos)?.volumeInfo?.pageCount )
            }
        }



    }

    fun saveToDataBase(title:String, authors: Array<String>, url: String, publisher: String, description: String, rating: Double, numRating: Int, pages: Int){

        val dbHelper = DataBaseHelper(applicationContext)
        val db = dbHelper.writableDatabase

        val value = ContentValues()
        value.put("title", title)

        // value.put("authors", authors) //jak wlozyc tablice to bazy danych?

        //value.put("imageLinks", url)

        value.put("publisher", publisher)

        value.put("description", description)

        value.put("rating", rating)

        value.put("ratingcount",numRating)

        value.put("pagecount",pages)

        db.insertOrThrow(TableInfo.TABLE_NAME, null, value)
    }

}
