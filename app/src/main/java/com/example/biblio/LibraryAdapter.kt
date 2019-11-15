package com.example.biblio

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotekapp.Book
import com.example.bibliotekapp.ItemList
import com.example.bibliotekapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_element.view.*


class LibraryAdapter(val list: MutableList<Book>, val listener: myClickListener): RecyclerView.Adapter<LibraryAdapter.MyViewHolder>(){
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.foo() //nie wiem jak ale dzieki temu mozna klikac na elementy

        val title= holder.view.title
        val author = holder.view.author
        val image = holder.view.cover
        val rating = holder.view.ratingBar
        val ratingText=holder.view.ratingBarText

        var url = "no url"
        var fetchedTitle="[Brak tytułu]"
        var fetchedAuthor="[Brak autora]"
        var fetchedRating=0.0

        fetchedTitle=list[position].title
        val authors=list[position].authors.joinToString(",")
        fetchedAuthor=authors

        val links=list[position].imageLinks.smallThumbnail
        url=links
        url = url.replace("http", "https").replace("&edge=curl", "")

        //ustawiam okladke
        Log.e("e",url)
        Picasso.with(image.context)
            .load(url)
            .placeholder(R.drawable.nocover)
            .into(image)

        fetchedRating=list[position].averageRating

        title.text = fetchedTitle
        author.text = fetchedAuthor
        rating.rating=fetchedRating.toFloat()
        ratingText.text=fetchedRating.toString()


    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val wiersz = layoutInflater.inflate(R.layout.list_element, parent, false) //dostarczam xml
        return MyViewHolder(wiersz)
    }



    interface myClickListener
    {
        fun startActivity(position: Int)
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val item = view
        fun foo(){
            item.setOnClickListener{
                listener.startActivity(this.adapterPosition)
            }
        }
    }


}