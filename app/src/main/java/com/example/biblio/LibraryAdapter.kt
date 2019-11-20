package com.example.biblio

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotekapp.Book
import com.example.bibliotekapp.Item
import com.example.bibliotekapp.ItemList
import com.example.bibliotekapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.library_element.view.*
import kotlinx.android.synthetic.main.list_element.view.*
import kotlinx.android.synthetic.main.list_element.view.author
import kotlinx.android.synthetic.main.list_element.view.cover
import kotlinx.android.synthetic.main.list_element.view.title


class LibraryAdapter(val list: MutableList<Item>, val listener: myClickListener): RecyclerView.Adapter<LibraryAdapter.MyViewHolder>(){
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.foo() //nie wiem jak ale dzieki temu mozna klikac na elementy

        val title= holder.view.title
        val author = holder.view.author
        val image = holder.view.cover
        val note = holder.view.note

        var url = "no url"
        var fetchedTitle="[Brak tytułu]"
        var fetchedAuthor="[Brak autora]"
        var fetchedRating=0.0

        fetchedTitle=list[position].volumeInfo.title
        val authors=list[position].volumeInfo.authors.joinToString(",")
        fetchedAuthor=authors

        val links=list[position].volumeInfo.imageLinks.smallThumbnail
        url=links
        url = url.replace("http", "https").replace("&edge=curl", "")

        //ustawiam okladke
        Log.e("e",url)
        Picasso.with(image.context)
            .load(url)
            .placeholder(R.drawable.nocover)
            .into(image)

        fetchedRating=list[position].volumeInfo.averageRating

        title.text = fetchedTitle
        author.text = fetchedAuthor
        if(list[position].note!="")note.setText(list[position].note)


    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val wiersz = layoutInflater.inflate(R.layout.library_element, parent, false) //dostarczam xml
        return MyViewHolder(wiersz)
    }



    interface myClickListener
    {
        fun startActivity(position: Int)
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val item = view
        val note = view.note
        fun foo(){
            item.setOnClickListener{
                listener.startActivity(this.adapterPosition)
            }
            note.setOnEditorActionListener { _, actionId, _ -> //dodawanie notatek
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    val db = DataBaseHelper(view.context)
                    db.addNote(list[this.adapterPosition],note.text.toString())
                    db.close()
                    note.clearFocus()
                }
                false
            }


        }
    }


}