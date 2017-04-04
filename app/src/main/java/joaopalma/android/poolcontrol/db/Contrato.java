package joaopalma.android.poolcontrol.db;

import android.provider.BaseColumns;

/**
 * Created by joaopalma on 29/03/2017.
 */

public class Contrato {

    private static final String TEXT_TYPE = " TEXT ";
    private static final String INT_TYPE = " INTEGER ";
    private static final String REAL_TYPE = " REAL ";

    public Contrato() {}

    public static abstract class Agendamento implements BaseColumns{
        public static final String TABLE_NAME = "agendamento";
        public static final String COLUMN_IDEQUIPAMENTO = "id_equipamento";
        public static final String COLUMN_INICIO = "inicio";
        public static final String COLUMN_TEMPO = "tempo_duracao";

        public static final String[] PROJECTION = {Agendamento._ID, Agendamento.COLUMN_IDEQUIPAMENTO, Agendamento.COLUMN_INICIO, Agendamento.COLUMN_TEMPO};

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Agendamento.TABLE_NAME + "(" +
                        Agendamento._ID + INT_TYPE + " PRIMARY_KEY," +
                        Agendamento.COLUMN_IDEQUIPAMENTO + INT_TYPE + "," +
                        Agendamento.COLUMN_INICIO + TEXT_TYPE + "," +
                        Agendamento.COLUMN_TEMPO + INT_TYPE + ");";

        public static final String SQL_DROP_ENTRIES = "DROP TABLE " + Agendamento.TABLE_NAME + ";";
    }

    public static abstract class Historico implements BaseColumns{
        public static final String TABLE_NAME = "historico";
        public static final String COLUMN_IDEQUIPAMENTO = "id_equipamento";
        public static final String COLUMN_VALOR = "valor";

        public static final String[] PROJECTION = {Historico._ID, Historico.COLUMN_IDEQUIPAMENTO, Historico.COLUMN_VALOR};

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Historico.TABLE_NAME + "(" +
                        Historico._ID + INT_TYPE + " PRIMARY_KEY," +
                        Historico.COLUMN_IDEQUIPAMENTO + INT_TYPE + "," +
                        Historico.COLUMN_VALOR + INT_TYPE + ");";

        public static final String SQL_DROP_ENTRIES = "DROP TABLE " + Historico.TABLE_NAME + ";";
    }

    public static abstract class Sensor implements BaseColumns{
        public static final String TABLE_NAME = "sensor";
        public static final String COLUMN_IDEQUIPAMENTO = "id_equipamento";;
        public static final String COLUMN_VALOR = "valor";
        public static final String CALUMN_TIME = "time";

        public static final String[] PROJECTION = {Sensor._ID, Sensor.COLUMN_IDEQUIPAMENTO, Sensor.COLUMN_VALOR, Sensor.CALUMN_TIME};

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Sensor.TABLE_NAME + "(" +
                        Sensor._ID + INT_TYPE + " PRIMARY_KEY," +
                        Sensor.COLUMN_IDEQUIPAMENTO + INT_TYPE + "," +
                        Sensor.COLUMN_VALOR + REAL_TYPE + "," +
                        Sensor.CALUMN_TIME + TEXT_TYPE + ");";

        public static final String SQL_DROP_ENTRIES = "DROP TABLE " + Sensor.TABLE_NAME + ";";
    }
}
