package com.example.banner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BannerAdapter(
    var list: ArrayList<Any>,
    private val layoutId: Int,
    private val onBindHolder: (holder: BannerHolder, value: Any) -> Unit
) :
    RecyclerView.Adapter<BannerAdapter.BannerHolder>() {
    class BannerHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerHolder {
        val holderView = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return BannerHolder(holderView)
    }

    override fun onBindViewHolder(holder: BannerHolder, position: Int) {
        val index = when (position) {
            0 -> list.size - 1
            list.size + 1 -> 0
            else -> position - 1
        }
        onBindHolder(holder, list[index])
    }

    override fun getItemCount() = if (list.size != 0) list.size + 2 else 0
}