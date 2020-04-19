package jp.techacademy.tate.yuusuke.taskapp

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.security.Key
import java.util.*

open class Task : RealmObject(), Serializable {
    var title: String = "" //タイトル
    var contents: String = "" //内容
    var date: Date = Date() //日時

    //idをプライマリキーとして設定
    @PrimaryKey
    var id: Int = 0
}