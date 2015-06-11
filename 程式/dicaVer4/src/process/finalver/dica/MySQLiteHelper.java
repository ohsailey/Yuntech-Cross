package process.finalver.dica;	//�ŧi�M��y�y

import android.content.Context;	//�ޤJ�������O
import android.database.sqlite.SQLiteDatabase;	//�ޤJ�������O
import android.database.sqlite.SQLiteOpenHelper;	//�ޤJ�������O
import android.database.sqlite.SQLiteDatabase.CursorFactory;	//�ޤJ�������O

//�~�Ӧ�SQLiteOpenHelper���l���O
public class MySQLiteHelper extends SQLiteOpenHelper{	
	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {			
		super(context, name, factory, version);		//�I�s�����O���غc����
	}
	@Override
	public void onCreate(SQLiteDatabase db) {		//�мgonCreate��k
		db.execSQL("create table if not exists user_info("	//�I�sexecSQL��k�s�ظ�ƪ�
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