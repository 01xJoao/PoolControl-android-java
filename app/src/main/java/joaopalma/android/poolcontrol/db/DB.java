package joaopalma.android.poolcontrol.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by joaopalma on 30/03/2017.
 */

public class DB extends SQLiteOpenHelper {

    public static final int DATABASE_VERSON = 6;
    public static final String DATABASE_NAME = "poolcontrol.db";

    public DB(Context context) {super(context, DATABASE_NAME, null, DATABASE_VERSON);}

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contrato.Agendamento.SQL_CREATE_ENTRIES);
        db.execSQL(Contrato.Historico.SQL_CREATE_ENTRIES);
        db.execSQL(Contrato.Sensor.SQL_CREATE_ENTRIES);

        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (1, '1', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (2, '7', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (3, '8', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (4, '9', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (5, '10', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (6, '11', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (7, '12', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (8, '13', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (9, '14', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (10, '15', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (11, '16', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (12, '17', '0'); ");
        db.execSQL("insert into " + Contrato.Historico.TABLE_NAME + " values (13, '18', '0'); ");

        db.execSQL("insert into " + Contrato.Sensor.TABLE_NAME + " values (1, '7', '30', '0'); ");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(Contrato.Agendamento.SQL_DROP_ENTRIES);
        db.execSQL(Contrato.Historico.SQL_DROP_ENTRIES);
        db.execSQL(Contrato.Sensor.SQL_DROP_ENTRIES);
        onCreate(db);
    }

    public void onDonwgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
