package process.finalver.dica;	//宣告套件語句

import android.content.Context;	//引入相關類別
import android.database.sqlite.SQLiteDatabase;	//引入相關類別
import android.database.sqlite.SQLiteOpenHelper;	//引入相關類別
import android.database.sqlite.SQLiteDatabase.CursorFactory;	//引入相關類別

//繼承自SQLiteOpenHelper的子類別
public class MySQLiteHelper extends SQLiteOpenHelper{	
	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {			
		super(context, name, factory, version);		//呼叫父類別的建構式ん
	}
	@Override
	public void onCreate(SQLiteDatabase db) {		//覆寫onCreate方法
		db.execSQL("create table if not exists user_info("	//呼叫execSQL方法新建資料表
				 + "id integer primary key,"
				 + "name varchar,"
				 + "age integer,"
				 + "career varchar,"
				 + "major1 varchar,"
				 + "major2 varchar,"
				 + "major3 varchar)");
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}	
}