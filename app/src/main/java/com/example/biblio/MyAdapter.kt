package com.example.biblio

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotekapp.ItemList
import com.example.bibliotekapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_element.view.*


class MyAdapter(val itemList: ItemList, val listener: myClickListener): RecyclerView.Adapter<MyAdapter.MyViewHolder>(){
    @Suppress("SENSELESS_COMPARISON") //zeby nie bylo warningow do ifow
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.foo() //nie wiem jak ale dzieki temu mozna klikac na elementy

        val db = DataBaseHelper(holder.view.context)
        val isInLibrary=db.isAdded(itemList.items[position])
        db.close()

        val title= holder.view.title
        val author = holder.view.author
        val image = holder.view.cover
        val rating = holder.view.ratingBar
        val ratingText=holder.view.ratingBarText
        val addButton = holder.view.addButton
        if(isInLibrary) addButton.setImageResource(R.drawable.addedlibrary_icon)

        var url = "no url"
        var fetchedTitle="[Brak tytułu]"
        var fetchedAuthor="[Brak autora]"
        var fetchedRating=0.0

        //sprawdzam czy jest tytul
        if (itemList.items[position].volumeInfo.title!=null) fetchedTitle =itemList.items[position].volumeInfo.title
        //sprawdzam czy sa autorzy
        if (itemList.items[position].volumeInfo.authors!=null) fetchedAuthor =itemList.items[position].volumeInfo.authors[0]
        //sprawdzam czy sa linki do okladek
        if(itemList.items[position].volumeInfo.imageLinks!=null)  url = itemList.items[position].volumeInfo.imageLinks.smallThumbnail

        //zamiana na https, bo inaczej obrazki w apce nie dzalaja, usuniecie edgecurl - bo tak ladniej wyglada
        url=url.replace("http","https").replace("&edge=curl","")

        //sprawdzam czy jest ocena
        if(itemList.items[position].volumeInfo.averageRating!=null) fetchedRating=itemList.items[position].volumeInfo.averageRating

        //ustawiam tekst dla elementow w list_element.xml
        title.setText(fetchedTitle)
        author.setText(fetchedAuthor)
        rating.rating=fetchedRating.toFloat()
        ratingText.text=fetchedRating.toString()

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
        return MyViewHolder(wiersz)
    }



    interface myClickListener
    {
        fun startActivity(position: Int)
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val item = view
        val addButton=view.addButton
        fun foo(){
            item.setOnClickListener{
                listener.startActivity(this.adapterPosition)
            }
            addButton.setOnClickListener {
                val db = DataBaseHelper(view.context)
                addButton.setImageResource(R.drawable.addedlibrary_icon)
                if(!db.isAdded(itemList.items[this.adapterPosition])){
                    db.insertData(itemList.items[this.adapterPosition])
                    addButton.setImageResource(R.drawable.addedlibrary_icon)
                }else{
                    addButton.setImageResource(R.drawable.addlibrary_icon)
                    db.deleteData(itemList.items[this.adapterPosition])
                }
                db.close()
            }
        }
    }


}