package jp.techacademy.tate.yuusuke.taskapp

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import kotlinx.android.synthetic.main.content_input.*
import io.realm.Realm
import java.util.*

class InputActivity : AppCompatActivity() {

    //タスクの日時を保持
    private var mYear = 0
    private var mMonth = 0
    private var mDay = 0
    private var mHour = 0
    private var mMinute = 0

    //タスククラスのオブジェクト
    private var mTask: Task? = null

    //日付の設定をするリスナー
    private val mOnDateClickListener = View.OnClickListener {
        val datePickerDialog = DatePickerDialog(this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                mYear = year
                mMonth = month
                mDay = dayOfMonth
                val dateString = mYear.toString() + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
                date_button.text = dateString
            }, mYear, mMonth, mDay)
        datePickerDialog.show()
    }

    //時間の設定をするリスナー
    private val mOnTimeClickListener = View.OnClickListener {
        val timePickerDialog = TimePickerDialog(this,
            TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                mHour = hour
                mMinute = minute
                val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)
                times_button.text = timeString
            }, mHour, mMinute, false)
        timePickerDialog.show()
    }

    //決定ボタンのリスナー
    private val mOnDoneClickListener = View.OnClickListener {
        addTask()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        //ActionBarを設定する
        val toolbar = findViewById<View>(R.id.toolbar) as android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            //ActionBarに戻るボタンを表示
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        //UI部品の設定
        date_button.setOnClickListener(mOnDateClickListener)
        times_button.setOnClickListener(mOnTimeClickListener)
        done_button.setOnClickListener(mOnDoneClickListener)

        //Realmからの受け取り
        //EXTRA_TASkからTaskのidを取得して、idからTaskのインスタンスを取得する
        val intent = intent
        //EXTRA_TASKからTaskのidを取り出す
        //EXTRA_TASKが設定されていない場合taskIdに-1が代入される
        val taskId = intent.getIntExtra(EXTRA_TASK, -1)
        val realm = Realm.getDefaultInstance()
        //TaskのidがTaskIdのものが検索され、findFirst()によって最初に見つかったインスタンスが返され、mTaskへ代入される
        //idが必ず0以上という仕様を利用し、taskIdに-1が入っていると、検索に引っかからず、mTaskにnullが代入される
        mTask = realm.where(Task::class.java).equalTo("id", taskId).findFirst()
        realm.close()

        if (mTask == null) {
            //新規作成の場合
            //カレンダーから、現在時刻をmYear、mMonth、mDay、mHour、mMinuteに設定する
            val calendar = Calendar.getInstance()
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)
        } else {
            //更新の場合
            //渡ってきたタスクの時間を設定する
            title_edit_text.setText(mTask!!.title)
            content_edit_text.setText(mTask!!.contents)

            val calendar = Calendar.getInstance()
            calendar.time = mTask!!.date
            mYear = calendar.get(Calendar.YEAR)
            mMonth = calendar.get(Calendar.MONTH)
            mDay = calendar.get(Calendar.DAY_OF_MONTH)
            mHour = calendar.get(Calendar.HOUR_OF_DAY)
            mMinute = calendar.get(Calendar.MINUTE)

            val dateString = mYear.toString() + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay)
            val timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute)

            date_button.text = dateString
            times_button.text = timeString
        }

    }

    private fun addTask() {
        //Realmオブジェクトを取得
        val realm = Realm.getDefaultInstance()

        //Realmと取引を開始する
        realm.beginTransaction()

        if (mTask == null) {
            //mTaskが新規作成の場合
            //Taskクラス生成（保存されているタスクの中の最大のidの値に1を足したものを設定）
            mTask = Task()

            val taskRealmResults = realm.where(Task::class.java).findAll()

            val identifier: Int =
                if (taskRealmResults.max("id") != null) {
                    taskRealmResults.max("id")!!.toInt() + 1
                } else {
                    0
                }
            mTask!!.id = identifier
        }

        val title = title_edit_text.text.toString()
        val content = content_edit_text.text.toString()

        //タイトル、内容、日時をmTaskに設定
        mTask!!.title = title //タイトル
        mTask!!.contents = content //内容

        val calendar = GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute)
        val date = calendar.time
        mTask!!.date = date //日時

        realm.copyToRealmOrUpdate(mTask!!) //Realmにコピーまたは更新
        realm.commitTransaction() //Realmとの取引を終了する

        realm.close()

        //タスクの日時にブロードキャストされるように、addTaskメソッドでデータベースにタスクを保存
        //TaskAlarmReceiverを起動するIntentを作成
        val resultIntent = Intent(applicationContext, TaskAlarmReceiver::class.java)
        //Extraにタスクを設定
        resultIntent.putExtra(EXTRA_TASK, mTask!!.id)
        //PendingIntentを作成
        val resultPendingIntent = PendingIntent.getBroadcast(
            this,
            mTask!!.id, //タスクのID
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT //PendingIntentがあれば、そのままでextraのデータだけ置き換えるという指定
        )

        //AlarmManagerを使うことで指定した時間に任意の処理をさせることができる
        //AlarmManagerはActivityのgetSystemServiceメソッドに引数ALARM_SERVICEを与えて取得する
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, resultPendingIntent)

    }
}
