package com.example.biblio

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bibliotekapp.*
import kotlinx.android.synthetic.main.activity_library.*

class LibraryActivity : AppCompatActivity(), LibraryAdapter.myClickListener {

    override fun startActivity(position: Int) {
        val nowaAktywnosc = Intent(applicationContext, BookActivity::class.java)
       // Toast.makeText(this, position.toString(), Toast.LENGTH_SHORT).show()
        nowaAktywnosc.putExtra("itemPos", position)
        startActivity(nowaAktywnosc)
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        this.supportActionBar?.title = "Biblioteka"

        val db = DataBaseHelper(this)
        var bookList=db.readData()
        db.close()

        libraryRecyclerView.layoutManager = LinearLayoutManager(this)
        libraryRecyclerView.adapter=LibraryAdapter(bookList,this)

        if(libraryRecyclerView.adapter!!.itemCount==0) noBooksView.visibility= View.VISIBLE
        else noBooksView.visibility= View.GONE

    }

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
    fun goToMain(view: View) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("getFocus",true)
        startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}
