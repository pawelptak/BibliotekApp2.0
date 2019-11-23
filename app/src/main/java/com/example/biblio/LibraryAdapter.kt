package com.example.biblio

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bibliotekapp.Item
import com.example.bibliotekapp.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.library_element.view.*
import kotlinx.android.synthetic.main.list_element.view.author
import kotlinx.android.synthetic.main.list_element.view.cover
import kotlinx.android.synthetic.main.list_element.view.title


class LibraryAdapter(val list: MutableList<Item>, val listener: myClickListener): RecyclerView.Adapter<LibraryAdapter.MyViewHolder>(){
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.initiateListeners() //nie wiem jak ale dzieki temu mozna klikac na elementy

        val title= holder.view.title
        val author = holder.view.author
        val image = holder.view.cover
        val note = holder.view.note
        note.setDoneButton()


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

       // fetchedRating=list[position].volumeInfo.averageRating

        title.text = fetchedTitle
        author.text = fetchedAuthor
        if(list[position].note!="") {
            note.setText(list[position].note)
            note.visibility=View.VISIBLE
            holder.view.addNoteButton.visibility=View.GONE

        }

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

    fun EditText.setDoneButton() { //pozwala na zatwierdzanie enterem tekstu o wielu liniach
        imeOptions = EditorInfo.IME_ACTION_DONE
        setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
    }

    inner class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val item = view
        val note = view.note
        val clearNoteBtn = view.clearNoteButton
        val addNoteBtn = view.addNoteButton
        val confNoteBtn = view.confirmNoteButton

        fun initiateListeners(){
            item.setOnClickListener{
                listener.startActivity(this.adapterPosition)
            }

            item.setOnCreateContextMenuListener { contextMenu, view, contextMenuInfo -> //usuwanie po przytrzymaniu
                contextMenu.add(0, view.getId(), 0, "Usuń z biblioteki").setOnMenuItemClickListener {
                    val db = DataBaseHelper(view.context)
                    db.deleteData(list[this.adapterPosition])
                    list.removeAt(this.adapterPosition)
                    notifyItemRemoved(this.adapterPosition)
                    db.close()
                    notifyDataSetChanged()
                    true
                }
            }



            note.setOnEditorActionListener { _, actionId, _ -> //dodawanie notatek
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    val db = DataBaseHelper(view.context)
                    db.addNote(list[this.adapterPosition],note.text.toString())
                    db.close()
                    note.clearFocus()
                   // confNoteBtn.visibility=View.GONE
                }
                false
            }

            confNoteBtn.setOnClickListener {
                val db = DataBaseHelper(view.context)
                db.addNote(list[this.adapterPosition],note.text.toString())
                db.close()
                note.onEditorAction(EditorInfo.IME_ACTION_DONE)
                note.clearFocus()
            }

            note.setOnFocusChangeListener { _, _ ->
                 if(note.hasFocus()){
                     clearNoteBtn.visibility=View.VISIBLE //pokazuje przycisk usuwania notatki
                     confNoteBtn.visibility=View.VISIBLE  //pokazuje przycisk zatwierdzenia notatki
                 }
                 else {
                     clearNoteBtn.visibility=View.GONE
                     confNoteBtn.visibility=View.GONE
                     if(note.text.toString()==""){
                         note.visibility=View.GONE
                         addNoteBtn.visibility=View.VISIBLE
                     }

                 }

            }


            clearNoteBtn.setOnClickListener {//usuwanie notatki
                note.setText("")
                val db = DataBaseHelper(view.context)
                db.addNote(list[this.adapterPosition],note.text.toString())
                db.close()
                note.onEditorAction(EditorInfo.IME_ACTION_DONE)
                note.visibility=View.GONE
                addNoteBtn.visibility=View.VISIBLE

            }

            addNoteBtn.setOnClickListener {
                addNoteBtn.visibility=View.GONE
                note.visibility=View.VISIBLE
                note.requestFocus()
                val imm =
                    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(
                    note,
                    InputMethodManager.SHOW_IMPLICIT
                )

            }


        }
    }


}