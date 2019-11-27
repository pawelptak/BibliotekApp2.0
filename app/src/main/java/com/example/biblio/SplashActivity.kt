package com.example.biblio

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import com.example.bibliotekapp.MainActivity
import com.example.bibliotekapp.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        try { //wylacza titlebar
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()


    }
}
