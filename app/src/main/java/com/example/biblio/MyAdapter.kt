package com.example.bibliotekapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_element.view.*


class MyAdapter(val itemList: ItemList, val listener: myClickListener): RecyclerView.Adapter<MyAdapter.MyViewHolder>(){
    @Suppress("SENSELESS_COMPARISON") //zeby nie bylo warningow do ifow
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.foo() //nie wiem jak ale dzieki temu mozna klikac na elementy

        val title= holder.view.title
        val author = holder.view.author
        val image = holder.view.cover

        var url = "no url"
        var fetchedTitle="[Brak tytułu]"
        var fetchedAuthor="[Brak autora]"

        //sprawdzam czy jest tytul
        if (itemList.items[position].volumeInfo.title!=null) fetchedTitle =itemList.items[position].volumeInfo.title
        //sprawdzam czy sa autorzy
        if (itemList.items[position].volumeInfo.authors!=null) fetchedAuthor =itemList.items[position].volumeInfo.authors[0]
        //sprawdzam czy sa linki do okladek
        if(itemList.items[position].volumeInfo.imageLinks!=null)  url = itemList.items[position].volumeInfo.imageLinks.smallThumbnail

        //zamiana na https, bo inaczej obrazki w apce nie dzalaja, usuniecie edgecurl - bo tak ladniej wyglada
        url=url.replace("http","https").replace("&edge=curl","")

        //ustawiam tekst dla elementow w list_element.xml
        title.setText(fetchedTitle)
        author.setText(fetchedAuthor)


        //ustawiam okladke
        Log.e("e",url)
        Picasso.with(image.context)
            .load(url)
            .placeholder(R.drawable.nocover)
            .into(image)

    }

    override fun getItemCount(): Int {
        return itemList.items.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val wiersz = layoutInflater.inflate(R.layout.list_element, parent, false) //dostarczam xml
        return MyViewHolder(wiersz, listener)
    }



    interface myClickListener
    {
        fun startActivity(position: Int)
    }

    inner class MyViewHolder(val view: View, clickListener:myClickListener) : RecyclerView.ViewHolder(view) {
        val item = view
        fun foo(){
            item.setOnClickListener{
                listener.startActivity(this.adapterPosition)
            }
        }
    }


}