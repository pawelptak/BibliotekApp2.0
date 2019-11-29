package com.example.bibliotekapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.not_found.view.*

/**
 * Odpowiada za wyświetlanie odpowiedniej zawartości na ekranie informującym o nieznalezieniu szukanej książki
 * @param hasConnection Zmienna określająca czy urządzenie ma dostęp do Internetu
 */
class NotFoundAdapter(hasConnection:Boolean): RecyclerView.Adapter<MyViewHolder2>(){
    val connection=hasConnection
    /**
     * Funkcja ustawia odpowiedni tekst na ekranie, w zależności czy nie znaleziono frazy, ponieważ nie ma jej w bazie, czy nie ma połączenia z Internetem
     */
    override fun onBindViewHolder(holder: MyViewHolder2, position: Int) {
        val textView=holder.view.notFoundText
        val notfoundText="Sprawdź połączenie sieciowe"
        if(!connection) textView.text=notfoundText

    }

    /**
     * Zwraca wyświetlanych elementów (1)
     * @return liczba elementów (1)
     */
    override fun getItemCount(): Int {
        return 1
    }

    /**
     * Funkcja przekazuje adapterowi plik design (not_found.xml)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder2 {
        val layoutInflater = LayoutInflater.from(parent.context)
        val wiersz = layoutInflater.inflate(R.layout.not_found, parent, false) //dostarczam xml
        return MyViewHolder2(wiersz)
    }



}

class MyViewHolder2(val view: View): RecyclerView.ViewHolder(view)