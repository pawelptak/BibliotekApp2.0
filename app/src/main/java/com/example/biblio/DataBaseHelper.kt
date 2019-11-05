package com.example.biblio


import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

object TableInfo: BaseColumns{
    const val TABLE_NAME="MyBooks"
    const val TABLE_COLUMN_TITLE="title"
    const val TABLE_COLUMN_AUTHORS="authors"
    const val TABLE_COLUMN_DESCRIPTUON="description"
    const val TABLE_COLUMN_IMAGES="imagelinks"
    const val TABLE_COLUMN_RATING="rating"
    const val TABLE_COLUMN_RATINGCOUNT="ratingcount"
    const val TABLE_COLUMN_PUBLISHER="publisher"
    const val TABLE_COLUMN_PRICE="price"
    const val TABLE_COLUMN_PAGECOUNT="pagecount"

}

object  BasicCommand{
    const val SQL_CREATE_TABLE =
        "CREATE TABLE ${TableInfo.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY,"+
                "${TableInfo.TABLE_COLUMN_TITLE} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_AUTHORS} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_DESCRIPTUON} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_IMAGES} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_RATING} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_RATINGCOUNT} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_PUBLISHER} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_PRICE} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_PAGECOUNT} TEXT NOT NULL )"

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${TableInfo.TABLE_NAME}"

}


class DataBaseHelper (context: Context): SQLiteOpenHelper(context, TableInfo.TABLE_NAME, null, 1){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(BasicCommand.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(BasicCommand.SQL_DELETE_TABLE)
        onCreate(db)
    }

}