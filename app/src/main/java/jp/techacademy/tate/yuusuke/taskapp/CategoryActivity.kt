package jp.techacademy.tate.yuusuke.taskapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_category.*
import kotlinx.android.synthetic.main.activity_main.*

class CategoryActivity : AppCompatActivity() {

    private var mCategory: Category? = null

    private lateinit var mRealm: Realm

    private val mRealmListener = object : RealmChangeListener<Realm> {
        override fun onChange(t: Realm) {
            reloadCategoryList()
        }
    }

//    private lateinit var mTaskAdapter: TaskAdapter

    private lateinit var mCategoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        mRealm = Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        mCategoryAdapter = CategoryAdapter(this@CategoryActivity)

        category_done_button.setOnClickListener {
            if (category_edit_text.text.toString() != "" ) {
                addCategory()
                reloadCategoryList()
            }
        }

        categoryListView.setOnItemLongClickListener { parent, _, position, _ ->
            val category = parent.adapter.getItem(position) as Category

            val results = mRealm.where(Category::class.java).equalTo("category", category.category).findAll()

            mRealm.beginTransaction()
            results.deleteAllFromRealm()
            mRealm.commitTransaction()

            reloadCategoryList()
            true
        }

        reloadCategoryList()
    }

    private fun reloadCategoryList() {
        val categoryRealmResults = mRealm.where(Category::class.java).findAll().sort("category")

        mCategoryAdapter.categoryList = mRealm.copyFromRealm(categoryRealmResults)

        categoryListView.adapter = mCategoryAdapter

        mCategoryAdapter.notifyDataSetChanged()
    }

    private fun addCategory() {
        mRealm.beginTransaction()

        mCategory = Category()

        val categoryRealmResults = mRealm.where(Category::class.java).findAll()


        val identifier: Int = if (categoryRealmResults.max("categoryId") != null) {
            categoryRealmResults.max("categoryId")!!.toInt() + 1
        } else {
            0
        }

        mCategory!!.categoryId = identifier



        val category = category_edit_text.text.toString()


        mCategory!!.category = category

        mRealm.copyToRealmOrUpdate(mCategory!!)
        mRealm.commitTransaction()

    }
}
