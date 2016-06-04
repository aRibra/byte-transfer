package com.bytetransfer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryLog {

	// database name and version
	private static final String DATABASE_NAME = "byte_transfer_history_db";
	private static final int DATABASE_VERSION = 1;

	// RECEIVED_FILES_LOG_HISTORY_TABLE
	private static final String RECEIVED_FILES_LOG_HISTORY_TABLE = "received_files_log_history";

	// columns
	public static final String KEY_RF_ROWID = "rf_id";
	public static final String KEY_RF_FILE_NAME = "file_name";
	public static final String KEY_RF_SOURCE = "source";
	public static final String KEY_RF_FILE_TYPE = "file_type";
	public static final String KEY_RF_DATE_RECEIVED = "date_received";
	public static final String KEY_RF_COMPLETED = "completed";
	public static final String KEY_RF_SIZE = "size";

	// SENT_FILES_LOG_HISTORY_TABLE
	private static final String SENT_FILES_LOG_HISTORY_TABLE = "sent_files_log_history";

	// columns
	public static final String KEY_SF_ROWID = "sf_id";
	public static final String KEY_SF_FILE_NAME = "file_name";
	public static final String KEY_SF_DESTINATION = "destination";
	public static final String KEY_SF_FILE_TYPE = "file_type";
	public static final String KEY_SF_DATE_SENT = "date_sent";
	public static final String KEY_SF_COMPLETED = "completed";
	public static final String KEY_SF_SIZE = "size";

	// INSTANT_MESSAGING_LOG_HISTORY_TABLE
	private static final String INSTANT_MESSAGING_LOG_HISTORY_TABLE = "instant_messaging_log_history";

	// columns
	public static final String KEY_IM_ROWID = "im_id";
	public static final String KEY_IM_MESSAGE = "message";
	public static final String KEY_IM_SOURCE = "source";
	public static final String KEY_IM_DESTINATION = "destination";
	public static final String KEY_IM_TIME = "time";
	public static final String KEY_IM_CHARACTERS = "characters";

	// SENT_FILES_LOG_HISTORY_TABLE
	private static final String IM_PEERS_TABLE = "im_peers";

	// columns
	public static final String KEY_PEER_ROWID = "peer_id";
	public static final String KEY_PEER_NAME = "peer_name";

	private DbHelper ourHelper;
	private final Context ourContext;
	private SQLiteDatabase ourDatabase;

	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("CREATE TABLE " + RECEIVED_FILES_LOG_HISTORY_TABLE
					+ " (" + KEY_RF_ROWID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_RF_FILE_NAME
					+ " TEXT NOT NULL, " + KEY_RF_SOURCE + " TEXT NOT NULL, "
					+ KEY_RF_FILE_TYPE + " TEXT NOT NULL, "
					+ KEY_RF_DATE_RECEIVED + " TEXT NOT NULL, " + KEY_RF_SIZE
					+ " TEXT NOT NULL, " + KEY_RF_COMPLETED
					+ " TEXT NOT NULL);");

			db.execSQL("CREATE TABLE " + SENT_FILES_LOG_HISTORY_TABLE + " ("
					+ KEY_SF_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ KEY_SF_FILE_NAME + " TEXT NOT NULL, "
					+ KEY_SF_DESTINATION + " TEXT NOT NULL, "
					+ KEY_SF_FILE_TYPE + " TEXT NOT NULL, " + KEY_SF_DATE_SENT
					+ " TEXT NOT NULL, " + KEY_SF_SIZE + " TEXT NOT NULL, "
					+ KEY_SF_COMPLETED + " TEXT NOT NULL);");

			db.execSQL("CREATE TABLE " + INSTANT_MESSAGING_LOG_HISTORY_TABLE
					+ " (" + KEY_IM_ROWID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_IM_MESSAGE
					+ " TEXT NOT NULL, " + KEY_IM_SOURCE + " TEXT NOT NULL, "
					+ KEY_IM_DESTINATION + " TEXT NOT NULL, " + KEY_IM_TIME
					+ " TEXT NOT NULL, " + KEY_IM_CHARACTERS
					+ " TEXT NOT NULL);");

			db.execSQL("CREATE TABLE " + IM_PEERS_TABLE + " (" + KEY_PEER_ROWID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_PEER_NAME
					+ " TEXT NOT NULL);");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS "
					+ RECEIVED_FILES_LOG_HISTORY_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SENT_FILES_LOG_HISTORY_TABLE);
			db.execSQL("DROP TABLE IF EXISTS "
					+ INSTANT_MESSAGING_LOG_HISTORY_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + IM_PEERS_TABLE);
			onCreate(db);

		}
	}

	public HistoryLog(Context c) {
		ourContext = c;
	}

	public HistoryLog open() throws SQLException {
		ourHelper = new DbHelper(ourContext);
		ourDatabase = ourHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		ourHelper.close();
	}

	// RECEIVED_FILES_LOG_HISTORY_TABLE createEntry
	public long rfCreateEntry(String file_name, String source,
			String file_type, String date_received, String completed,
			String size) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put(KEY_RF_FILE_NAME, file_name);
		cv.put(KEY_RF_SOURCE, source);
		cv.put(KEY_RF_FILE_TYPE, file_type);
		cv.put(KEY_RF_DATE_RECEIVED, date_received);
		cv.put(KEY_RF_COMPLETED, completed);
		cv.put(KEY_RF_SIZE, size);
		return ourDatabase.insert(RECEIVED_FILES_LOG_HISTORY_TABLE, null, cv);
	}

	// SENT_FILES_LOG_HISTORY_TABLE createEntry
	public long sfCreateEntry(String file_name, String destination,
			String file_type, String date_sent, String completed, String size) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put(KEY_SF_FILE_NAME, file_name);
		cv.put(KEY_SF_DESTINATION, destination);
		cv.put(KEY_SF_FILE_TYPE, file_type);
		cv.put(KEY_SF_DATE_SENT, date_sent);
		cv.put(KEY_SF_COMPLETED, completed);
		cv.put(KEY_SF_SIZE, size);
		return ourDatabase.insert(SENT_FILES_LOG_HISTORY_TABLE, null, cv);
	}

	// INSTANT_MESSAGING_LOG_HISTORY_TABLE createEntry
	public long imCreateEntry(String message, String source,
			String destination, String time, String characters) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put(KEY_IM_MESSAGE, message);
		cv.put(KEY_IM_SOURCE, source);
		cv.put(KEY_IM_DESTINATION, destination);
		cv.put(KEY_IM_TIME, time);
		cv.put(KEY_IM_CHARACTERS, characters);
		return ourDatabase
				.insert(INSTANT_MESSAGING_LOG_HISTORY_TABLE, null, cv);
	}

	// INSTANT_MESSAGING_LOG_HISTORY_TABLE createEntry
	public long peerCreateEntry(String peer_name) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put(KEY_PEER_NAME, peer_name);
		return ourDatabase.insert(IM_PEERS_TABLE, null, cv);
	}

	// getData for RECEIVED_FILES
	public String getRfData() throws SQLException {
		// TODO Auto-generated method stub
		String[] columns = new String[] { KEY_RF_ROWID, KEY_RF_FILE_NAME,
				KEY_RF_SOURCE, KEY_RF_FILE_TYPE, KEY_RF_DATE_RECEIVED,
				KEY_RF_COMPLETED, KEY_RF_SIZE };
		Cursor c = ourDatabase.query(RECEIVED_FILES_LOG_HISTORY_TABLE, columns,
				null, null, null, null, null);
		String result = "";

		int indexRow = c.getColumnIndex(KEY_RF_ROWID);
		int indexFileName = c.getColumnIndex(KEY_RF_FILE_NAME);
		int indexSource = c.getColumnIndex(KEY_RF_SOURCE);
		int indexFileType = c.getColumnIndex(KEY_RF_FILE_TYPE);
		int indexDateRecevied = c.getColumnIndex(KEY_RF_DATE_RECEIVED);
		int indexCompleted = c.getColumnIndex(KEY_RF_COMPLETED);
		int indexSize = c.getColumnIndex(KEY_RF_SIZE);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result = result + c.getString(indexRow) + "|"
					+ c.getString(indexFileName) + "|"
					+ c.getString(indexSource) + "|"
					+ c.getString(indexFileType) + "|"
					+ c.getString(indexDateRecevied) + "|"
					+ c.getString(indexCompleted) + "|"
					+ c.getString(indexSize) + "|" + "\n";
		}

		return result;
	}

	// getData for SENT_FILES
	public String getSfData() throws SQLException {
		// TODO Auto-generated method stub
		String[] columns = new String[] { KEY_SF_ROWID, KEY_SF_FILE_NAME,
				KEY_SF_DESTINATION, KEY_SF_FILE_TYPE, KEY_SF_DATE_SENT,
				KEY_SF_COMPLETED, KEY_SF_SIZE };
		Cursor c = ourDatabase.query(SENT_FILES_LOG_HISTORY_TABLE, columns,
				null, null, null, null, null);
		String result = "";

		int indexRow = c.getColumnIndex(KEY_SF_ROWID);
		int indexFileName = c.getColumnIndex(KEY_SF_FILE_NAME);
		int indexDestination = c.getColumnIndex(KEY_SF_DESTINATION);
		int indexFileType = c.getColumnIndex(KEY_SF_FILE_TYPE);
		int indexDateSent = c.getColumnIndex(KEY_SF_DATE_SENT);
		int indexCompleted = c.getColumnIndex(KEY_SF_COMPLETED);
		int indexSize = c.getColumnIndex(KEY_SF_SIZE);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result = result + c.getString(indexRow) + "|"
					+ c.getString(indexFileName) + "|"
					+ c.getString(indexDestination) + "|"
					+ c.getString(indexFileType) + "|"
					+ c.getString(indexDateSent) + "|"
					+ c.getString(indexCompleted) + "|"
					+ c.getString(indexSize) + "|" + "\n";
		}

		return result;
	}

	// getData for INSTANT_MESSAGING
	public String getImData(String peer_name) throws SQLException {
		// TODO Auto-generated method stub
		String[] columns = new String[] { KEY_IM_ROWID, KEY_IM_MESSAGE,
				KEY_IM_SOURCE, KEY_IM_DESTINATION, KEY_IM_TIME,
				KEY_IM_CHARACTERS };
		Cursor c = ourDatabase.query(INSTANT_MESSAGING_LOG_HISTORY_TABLE,
				columns, KEY_IM_DESTINATION + " = '" + peer_name + "' OR "
						+ KEY_IM_SOURCE + " = '" + peer_name + "'", null, null,
				null, null);
		// KEY_IM_DESTINATION + " = " + peer_name
		String result = "";

		int indexRow = c.getColumnIndex(KEY_IM_ROWID);
		int indexMessage = c.getColumnIndex(KEY_IM_MESSAGE);
		int indexSource = c.getColumnIndex(KEY_IM_SOURCE);
		int indexDestination = c.getColumnIndex(KEY_IM_DESTINATION);
		int indexImTime = c.getColumnIndex(KEY_IM_TIME);
		int indexCharacters = c.getColumnIndex(KEY_IM_CHARACTERS);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result = result + c.getString(indexRow) + "|"
					+ c.getString(indexMessage) + "|"
					+ c.getString(indexSource) + "|"
					+ c.getString(indexDestination) + "|"
					+ c.getString(indexImTime) + "|"
					+ c.getString(indexCharacters) + "|" + "\n";
		}

		return result;
	}

	// getData for PEERS
	public String getPeersData() throws SQLException {
		// TODO Auto-generated method stub
		String[] columns = new String[] { KEY_PEER_ROWID, KEY_PEER_NAME };
		Cursor c = ourDatabase.query(IM_PEERS_TABLE, columns, null, null, null,
				null, null);
		String result = "";

		int indexRow = c.getColumnIndex(KEY_PEER_ROWID);
		int indexPeer = c.getColumnIndex(KEY_PEER_NAME);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result = result + c.getString(indexRow) + "|"
					+ c.getString(indexPeer) + "|" + "\n";
		}

		return result;
	}

	public void deleteEntryRf(long rf_row) throws SQLException {
		// TODO Auto-generated method stub
		ourDatabase.delete(RECEIVED_FILES_LOG_HISTORY_TABLE, KEY_RF_ROWID + "="
				+ rf_row, null);
	}

	public void deleteEntrySf(long sf_row) throws SQLException {
		// TODO Auto-generated method stub
		ourDatabase.delete(SENT_FILES_LOG_HISTORY_TABLE, KEY_SF_ROWID + "="
				+ sf_row, null);
	}

	public void deleteEntryIm(long im_row) throws SQLException {
		// TODO Auto-generated method stub
		ourDatabase.delete(INSTANT_MESSAGING_LOG_HISTORY_TABLE, KEY_IM_ROWID
				+ "=" + im_row, null);
	}

	public void deleteEntryPeers(long peer_row) throws SQLException {
		// TODO Auto-generated method stub
		ourDatabase.delete(IM_PEERS_TABLE, KEY_IM_ROWID + "=" + peer_row, null);
	}

	public void deleteAllEntryPeers() throws SQLException {
		// TODO Auto-generated method stub
		ourDatabase.delete(IM_PEERS_TABLE, null, null);
	}

}
