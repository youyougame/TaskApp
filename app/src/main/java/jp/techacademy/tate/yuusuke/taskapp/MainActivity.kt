package jp.techacademy.tate.yuusuke.taskapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.realm.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_input.*
import java.util.*
import kotlin.collections.ArrayList

const val EXTRA_TASK = "jp.techacademy.tate.yuusuke.taskapp.TASK"

class MainActivity : AppCompatActivity() {

    //Realmクラスを保持する
    private lateinit var mRealm: Realm

    //Realmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(t: Realm) {
            reloadListView()
        }
    }

    //TaskAdapterを保持するプロパティを定義する
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //FloatingActionButtonのクリックリスナー
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            startActivity(intent)
        }

        //Realmの設定
        //オブジェクトを取得
        mRealm = Realm.getDefaultInstance()
        //mRealmListenerを設定する
        mRealm.addChangeListener(mRealmListener)

        category_done_button.setOnClickListener {
            val categoryWord = categoryEditText.text
            if (categoryWord.toString() != "") {
                val categoryRealmResults = mRealm.where(Task::class.java).equalTo("category", categoryWord.toString()).findAll()
                if (categoryRealmResults.toString() != "") {
                    mTaskAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)
                    listView1.adapter = mTaskAdapter
                    mTaskAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(applicationContext, "該当するカテゴリーがありません", Toast.LENGTH_SHORT).show()
                }
            } else {
                reloadListView()
            }
        }

        //ListViewの設定
        mTaskAdapter = TaskAdapter(this@MainActivity)

        //ListViewをタップしたときの処理
        listView1.setOnItemClickListener { parent, _, position, _ ->
            //入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }

        //ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            //タスクを削除する
            val task = parent.adapter.getItem(position) as Task

            //ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")

            builder.setPositiveButton("OK") {_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                //アラームを解除する
                val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
                val resultPendingIntent = PendingIntent.getBroadcast(
                    this@MainActivity,
                    task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(resultPendingIntent)

                reloadListView()
            }

            builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }

    private fun reloadListView() {
        //Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        val taskRealmResults = mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)

        //上記の結果を、TaskListとしてセットする
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)

        //TaskのListView用のアダプタに渡す
        listView1.adapter = mTaskAdapter

        //表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged()
    }


    override fun onDestroy() {
        super.onDestroy()

        mRealm.close()
    }
}
