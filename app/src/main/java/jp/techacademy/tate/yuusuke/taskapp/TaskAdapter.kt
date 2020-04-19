package jp.techacademy.tate.yuusuke.taskapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.coroutineContext

class TaskAdapter(context: Context): BaseAdapter() {
    private val mLayoutInflater: LayoutInflater
    //アイテムを保持するリスト
    var taskList = mutableListOf<Task>()

    init {
        this.mLayoutInflater = LayoutInflater.from(context)
    }

    //アイテムの数を返す
    override fun getCount(): Int {
        return taskList.size
    }

    //アイテムを返す
    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    //アイテムのIDを返す
    override fun getItemId(position: Int): Long {
        return taskList[position].id.toLong()
    }

    //Viewを返す
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val  view: View = convertView ?: mLayoutInflater.inflate(android.R.layout.simple_list_item_2,null)

        val textView1 = view.findViewById<TextView>(android.R.id.text1)
        val textView2 = view.findViewById<TextView>(android.R.id.text2)

        //後でTaskクラスから情報を取得するように変更する
        textView1.text = taskList[position].title

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.JAPANESE)
        val date = taskList[position].date
        textView2.text = simpleDateFormat.format(date)

        return view
    }
}