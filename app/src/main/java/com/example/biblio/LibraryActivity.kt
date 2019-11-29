package com.example.bibliotekapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotekapp.MainActivity
import com.example.bibliotekapp.R
import kotlinx.android.synthetic.main.activity_library.*

/**
 * Biblioteka zapisanych książek
 */
class LibraryActivity : AppCompatActivity(), LibraryAdapter.myClickListener {

    /**
     * Włącza nową aktywność - BookActivity (zawierającą więcej informacji o książce) po wciśnięciu danej pozycji z listy
     * @param position Indeks wciśniętej pozycji na liście
     */
    override fun startActivity(position: Int) {
        val nowaAktywnosc = Intent(applicationContext, BookActivity::class.java)
       // Toast.makeText(this, position.toString(), Toast.LENGTH_SHORT).show()
        nowaAktywnosc.putExtra("itemPos", position)
        startActivity(nowaAktywnosc)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        val typeface = Typeface.createFromAsset(assets, "fonts/BebasNeue.ttf") //font tytulu w titlebarze
        toolbar2Title.typeface=typeface
        val db = DataBaseHelper(this)
        var bookList=db.readData()
        db.close()

        libraryRecyclerView.layoutManager = LinearLayoutManager(this)
        libraryRecyclerView.adapter=LibraryAdapter(bookList,this)

        if(libraryRecyclerView.adapter!!.itemCount==0) noBooksView.visibility= View.VISIBLE
        else noBooksView.visibility= View.GONE

    }

    /**
     * Aktualizacja zawartości biblioteki po wróceniu do aktywności przy pomocy przycisku wstecz
     */
    override fun onResume() {
        super.onResume()
        val db = DataBaseHelper(this)
        var bookList=db.readData()
        db.close()
        libraryRecyclerView.adapter=LibraryAdapter(bookList,this)

        if(libraryRecyclerView.adapter!!.itemCount==0) noBooksView.visibility= View.VISIBLE
        else noBooksView.visibility= View.GONE
    }


    @Suppress("UNUSED_PARAMETER")
            /**
             * Powrót do ekranu menu głownego po wciśnięciu przycisku z domkiem
             */
    fun goToMain(view: View) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("getFocus",true)
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}
