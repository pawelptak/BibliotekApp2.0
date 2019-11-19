package com.example.biblio


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.widget.Toast
import com.example.bibliotekapp.Book
import com.example.bibliotekapp.ImageLink
import java.io.Serializable


object TableInfo: BaseColumns{
    const val TABLE_NAME="MyBooks"
    const val TABLE_COLUMN_TITLE="title"
    const val TABLE_COLUMN_AUTHORS="authors"
    const val TABLE_COLUMN_DESCRIPTION="description"
    const val TABLE_COLUMN_IMAGES="imagelinks"
    const val TABLE_COLUMN_RATING="rating"
    const val TABLE_COLUMN_RATINGCOUNT="ratingcount"
    const val TABLE_COLUMN_PUBLISHER="publisher"
    const val TABLE_COLUMN_PAGECOUNT="pagecount"

}

object  BasicCommand{
    const val SQL_CREATE_TABLE =
        "CREATE TABLE ${TableInfo.TABLE_NAME} (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY,"+
                "${TableInfo.TABLE_COLUMN_TITLE} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_AUTHORS} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_DESCRIPTION} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_IMAGES} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_RATING} REAL NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_RATINGCOUNT} INT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_PUBLISHER} TEXT NOT NULL,"+
                "${TableInfo.TABLE_COLUMN_PAGECOUNT} INT NOT NULL,"+
                "CONSTRAINT name_unique UNIQUE (${TableInfo.TABLE_COLUMN_TITLE}))" //dzieki temu nie mozna dodac dwoch ksiazek o takim samym tytule

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS ${TableInfo.TABLE_NAME}"

}


@Suppress("SENSELESS_COMPARISON")
class DataBaseHelper (var context: Context): SQLiteOpenHelper(context, TableInfo.TABLE_NAME, null, 1), Serializable {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(BasicCommand.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(BasicCommand.SQL_DELETE_TABLE)
        onCreate(db)
    }

    fun insertData(book: Book):Boolean{
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(TableInfo.TABLE_COLUMN_TITLE, book.title)


        if(book.authors!=null){
            val authors=book.authors.joinToString(", ")
            cv.put(TableInfo.TABLE_COLUMN_AUTHORS,authors)
        }
        else cv.put(TableInfo.TABLE_COLUMN_AUTHORS,"[Brak autora]")

        if(book.description!=null)cv.put(TableInfo.TABLE_COLUMN_DESCRIPTION,book.description)
        else cv.put(TableInfo.TABLE_COLUMN_DESCRIPTION,"Brak opisu.")

        if(book.imageLinks!=null){
            val links=book.imageLinks.smallThumbnail
            cv.put(TableInfo.TABLE_COLUMN_IMAGES,links)
        }else cv.put(TableInfo.TABLE_COLUMN_IMAGES,"")

        cv.put(TableInfo.TABLE_COLUMN_RATING,book.averageRating)

        cv.put(TableInfo.TABLE_COLUMN_RATINGCOUNT,book.ratingsCount)

        if(book.publisher!=null)cv.put(TableInfo.TABLE_COLUMN_PUBLISHER,book.publisher)
        else cv.put(TableInfo.TABLE_COLUMN_PUBLISHER,"")

        cv.put(TableInfo.TABLE_COLUMN_PAGECOUNT,book.pageCount)

        val result=db.insert(TableInfo.TABLE_NAME,null,cv)
        if(result==-1.toLong()){
            Toast.makeText(context,"Failed",Toast.LENGTH_LONG).show()
            return false
        }
        else{
            Toast.makeText(context,"Dodano do biblioteki", Toast.LENGTH_SHORT).show()
            return true
        }
    }

    fun readData() :MutableList<Book>{
        val list:MutableList<Book> = ArrayList()

        val db = this.readableDatabase
        val query="Select * from "+TableInfo.TABLE_NAME
        val result=db.rawQuery(query,null)
        if(result.moveToFirst()){
            do{
                val title=result.getString(result.getColumnIndex(TableInfo.TABLE_COLUMN_TITLE))
                val authorList= result.getString(result.getColumnIndex(TableInfo.TABLE_COLUMN_AUTHORS)).split(",").toTypedArray()
                val imageLink = result.getString(result.getColumnIndex(TableInfo.TABLE_COLUMN_IMAGES))
                val imageObject = ImageLink(imageLink)
                val description=result.getString(result.getColumnIndex(TableInfo.TABLE_COLUMN_DESCRIPTION))
                val avgRating = result.getFloat(result.getColumnIndex(TableInfo.TABLE_COLUMN_RATING))
                val ratingCount=result.getInt(result.getColumnIndex(TableInfo.TABLE_COLUMN_RATINGCOUNT))
                val publisher=result.getString(result.getColumnIndex(TableInfo.TABLE_COLUMN_PUBLISHER))
                val pageCount=result.getInt(result.getColumnIndex(TableInfo.TABLE_COLUMN_PAGECOUNT))

                val book=Book(title,authorList,imageObject,description,avgRating.toDouble(),ratingCount,publisher,pageCount)
                list.add(book)
            }while (result.moveToNext())
        }

        result.close()
        db.close()
        return list
    }

    fun deleteData(book: Book){
        var authors="[Brak autora]" //zbedna zmienna ale zostawilem
        if(book.authors!=null){
             authors=book.authors.joinToString(", ")
            authors=authors.replace("'","''")
        }
        val title=book.title.replace("'","''") //byl problem z query jesli byl apostrof w tytule
        val db = this.readableDatabase
        val query="Delete from "+TableInfo.TABLE_NAME+" where "+TableInfo.TABLE_COLUMN_TITLE+"='"+title+"'"
        db.execSQL(query)
        db.close()
        Toast.makeText(context,"Usunięto z biblioteki", Toast.LENGTH_SHORT).show()
    }

    fun isAdded(book: Book):Boolean{
        var authors="[Brak autora]" //zbedna zmienna ale zostawilem
        if(book.authors!=null){
            authors=book.authors.joinToString(", ")
            authors=authors.replace("'","''")
        }
        val title=book.title.replace("'","''") //byl problem z query jesli byl apostrof w tytule
        val db = this.readableDatabase
        val query="Select 1 from "+TableInfo.TABLE_NAME+" where "+TableInfo.TABLE_COLUMN_TITLE+"='"+title+"'"
        val result=db.rawQuery(query,null)
        return result.count>0
    }

}