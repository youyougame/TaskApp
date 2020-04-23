package jp.techacademy.tate.yuusuke.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlin.contracts.contract

class CategoryAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater
    var categoryList = mutableListOf<Category>()

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return categoryList.size
    }

    override fun getItem(position: Int): Any {
        return categoryList[position]
    }

    override fun getItemId(position: Int): Long {
        return categoryList[position].categoryId.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null)

        val textView = view.findViewById<TextView>(android.R.id.text1)

        textView.text = categoryList[position].category


        return view
    }
}