package com.huawei.mlkit.sample.activity.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.mlsdk.fr.MLFormRecognitionTablesAttribute.TablesContent.TableAttribute.TableCellAttribute
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.adapter.ItemAdapter.ItemHolder
import java.util.*

class ItemAdapter : RecyclerView.Adapter<ItemHolder>() {
    var list: MutableList<TableCellAttribute?> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ItemHolder(v)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.tv.text = list[position]!!.startCol.toString() + ":" + list[position]!!
            .startRow + ":" + list[position]!!.endCol + ":" + list[position]!!
            .endRow + " " + list[position]!!.textInfo
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv: TextView = itemView.findViewById(R.id.tv)
    }

}