package com.example.banner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nList = ArrayList<Any>()
        nList.add(R.drawable.first)
        nList.add(R.drawable.second)
        nList.add(R.drawable.third)
        nList.add(R.drawable.forth)

        val banner = findViewById<Banner>(R.id.banner)
        lifecycle.addObserver(banner)
        banner.setImageList(nList, R.layout.banner_item) { holder, value ->
            holder.itemView.findViewById<ImageView>(R.id.image).setImageResource(value as Int)
        }
        banner.start()
    }
}