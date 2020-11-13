package biz.wolschon.wag.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import biz.wolschon.wag.shared.Greeting
import android.widget.TextView
import biz.wolschon.wag.R

fun greet(): String {
    return Greeting().greeting()
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv: TextView = findViewById(R.id.text_view)
        tv.text = greet()
    }
}
