package jp.techacademy.tate.yuusuke.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class Category :RealmObject(), Serializable {
    var category: String = "" //カテゴリー

    @PrimaryKey
    var categoryId: Int = 0
}