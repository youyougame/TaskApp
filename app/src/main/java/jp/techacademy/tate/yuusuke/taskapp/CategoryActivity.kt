package jp.techacademy.tate.yuusuke.taskapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_main.*

class CategoryActivity : AppCompatActivity() {

    private var mTask: Task? = null

    private lateinit var mRealm: Realm

    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(t: Realm) {
            reloadCategoryList()
        }
    }

    private lateinit var mTaskAdapter: TaskAdapter

//    private lateinit var mCategoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        mTaskAdapter = TaskAdapter(this@CategoryActivity)

        category_done_button.setOnClickListener {
            val categoryText = category_edit_text.text
            if (categoryText.toString() != "" ) {
                addCategory()
                reloadCategoryList()
            }
        }

        categoryListView.setOnItemLongClickListener { parent, _, position, _ ->
            val task = parent.adapter.getItem(position) as Task

            val results = mRealm.where(Task::class.java).equalTo("category", task.category).findAll()

            mRealm.beginTransaction()
            results.deleteAllFromRealm()
            mRealm.commitTransaction()

            reloadCategoryList()
            true
        }

        reloadCategoryList()
    }

    private fun reloadCategoryList() {
        val categoryRealmResults = mRealm.where(Task::class.java).findAll().sort("category", Sort.DESCENDING)

//        mCategoryAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)
        mTaskAdapter.taskList = mRealm.copyFromRealm(categoryRealmResults)

//        categoryListView.adapter = mCategoryAdapter
        categoryListView.adapter = mTaskAdapter

//        mCategoryAdapter.notifyDataSetChanged()
        mTaskAdapter.notifyDataSetChanged()
    }

    private fun addCategory() {

        mRealm.beginTransaction()

        val category = category_edit_text.toString()

        mTask = Task()

        mTask!!.category = category

        mRealm.copyToRealmOrUpdate(mTask!!)
        mRealm.commitTransaction()

    }
}
