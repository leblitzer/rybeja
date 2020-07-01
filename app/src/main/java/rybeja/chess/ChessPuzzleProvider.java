package rybeja.chess;

import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

// YBO 08/05/2020 : jwtc -> rybeja
// YBO 29/06/2020 : Ajout BRD

public class ChessPuzzleProvider extends ContentProvider {

    // YBO 08/05/2020  public static String AUTHORITY = "jwtc.android.chess.puzzle.ChessPuzzleProvider";
	public static String AUTHORITY = "rybeja.android.chess.puzzle.ChessPuzzleProvider"; // YBO 08/05/2020
    public static Uri CONTENT_URI_PUZZLES = Uri.parse("content://"  + AUTHORITY + "/puzzles");
    public static Uri CONTENT_URI_PRACTICES = Uri.parse("content://"  + AUTHORITY + "/practices");
    public static Uri CONTENT_URI_MATINONE = Uri.parse("content://"  + AUTHORITY + "/Mixed"); // YBO 14/02/2020
    public static Uri CONTENT_URI_PLGFIN = Uri.parse("content://"  + AUTHORITY + "/plgfin"); // YBO 29/06/2020
    public static Uri CONTENT_URI_BRDMATIN3 = Uri.parse("content://"  + AUTHORITY + "/brdmatin3"); // YBO 29/06/2020

    private static final String TAG = "ChessPuzzleProvider";

    private static final String DATABASE_NAME = "chess_puzzles.db";
    private static final int DATABASE_VERSION = 8; // 7.6 lessons 
    private static final String GAMES_TABLE_NAME = "games";

    private static HashMap<String, String> sGamesProjectionMap;

    protected static final int PUZZLES = 1;
    protected static final int PUZZLES_ID = 2;
	protected static final int PRACTICES = 3;// YBO 03/04/2020 
	protected static final int PRACTICES_ID = 4;// YBO 03/04/2020
    protected static final int MATINONE = 5; // YBO 14/02/2020
    protected static final int MATINONE_ID = 6; // YBO 14/02/2020
    protected static final int PLGFIN = 7; // YBO 29/06/2020
    protected static final int PLGFIN_ID = 8; // YBO 29/06/2020
    protected static final int BRDMATIN3 = 9; // YBO 29/06/2020
    protected static final int BRDMATIN3_ID = 10; // YBO 29/06/2020

    protected static final int TYPE_PUZZLE = 101;
    protected static final int TYPE_PRACTICE = 102; // YBO 03/04/2020
    protected static final int TYPE_MATEINONE = 103; // YBO 14/02/2020
    protected static final int TYPE_PLGFIN = 104; // YBO 29/06/2020
    protected static final int TYPE_BRDIN3 = 105; // YBO 29/06/2020

    protected static UriMatcher sUriMatcher;
    
    public static final String COL_ID = "_ID";
    public static final String COL_PGN = "PGN";
    public static final String COL_TYPE = "PUZZLE_TYPE";
    
    public static final String[] COLUMNS = {
    		COL_ID,
    		COL_TYPE,
    		COL_PGN
    };
    
    public static final String DEFAULT_SORT_ORDER = "_ID ASC";
    // YBO 08/05/2020  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.jwtc.chesspuzzle";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.rybeja.chesspuzzle"; // YBO 08/05/2020
    // YBO 08/05/2020 public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.jwtc.chesspuzzle";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.rybeja.chesspuzzle";

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + GAMES_TABLE_NAME + " ("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_TYPE + " INTEGER,"
                    + COL_PGN + " TEXT"
                    + ");");
            
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + GAMES_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int iUri = sUriMatcher.match(uri);
        switch (iUri) {
        case PUZZLES:
            qb.setTables(GAMES_TABLE_NAME);
            qb.setProjectionMap(sGamesProjectionMap);
            qb.appendWhere(COL_TYPE + "=" + TYPE_PUZZLE);
            break;

        case PUZZLES_ID: case PRACTICES_ID: case MATINONE_ID: case PLGFIN_ID: case BRDMATIN3_ID: // YBO 29/06/2020
            qb.setTables(GAMES_TABLE_NAME);
            qb.setProjectionMap(sGamesProjectionMap);
            qb.appendWhere(COL_ID + "=" + uri.getPathSegments().get(1));
            break;
            // YBO 03/04/2020
            
        case PRACTICES:
            qb.setTables(GAMES_TABLE_NAME);
            qb.setProjectionMap(sGamesProjectionMap);
            qb.appendWhere(COL_TYPE + "=" + TYPE_PRACTICE);
            break;
            /* YBO 14/02/2020 */
            case MATINONE:
                qb.setTables(GAMES_TABLE_NAME);
                qb.setProjectionMap(sGamesProjectionMap);
                qb.appendWhere(COL_TYPE + "=" + TYPE_MATEINONE);
                break;
        /* FIN YBO 14/02/2020 */
        /*  YBO 29/06/2020 */
            case PLGFIN:
                qb.setTables(GAMES_TABLE_NAME);
                qb.setProjectionMap(sGamesProjectionMap);
                qb.appendWhere(COL_TYPE + "=" + TYPE_PLGFIN);
                break;
            case BRDMATIN3:
                qb.setTables(GAMES_TABLE_NAME);
                qb.setProjectionMap(sGamesProjectionMap);
                qb.appendWhere(COL_TYPE + "=" + TYPE_BRDIN3);
                break;
        /* FIN YBO 29/06/2020 */
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case PUZZLES: case MATINONE: case PRACTICES: case PLGFIN: case BRDMATIN3:
            return CONTENT_TYPE;
        case PUZZLES_ID: case PRACTICES_ID: case MATINONE_ID: case PLGFIN_ID: case BRDMATIN3_ID:
            return CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    public Uri insert(Uri uri, ContentValues initialValues) {
        // YBO 29/06/2020 : Ajout de else if
        // Validate the requested uri
    	
    	int iType = sUriMatcher.match(uri);
        // YBO 03/04/2020 PRATICES en commentaire
        if (iType != PUZZLES &&  iType != PRACTICES && iType != MATINONE && iType !=  PLGFIN && iType != BRDMATIN3) { // YBO 14/02/2020
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        if(iType == PUZZLES)
        	values.put(COL_TYPE, TYPE_PUZZLE);
            // YBO 03/04/2020  
        else if(iType == PRACTICES)
        	values.put(COL_TYPE, TYPE_PRACTICE);
        // YB0 14/02/2020
        else if(iType == MATINONE)
            values.put(COL_TYPE, TYPE_MATEINONE);
        // FIN YB0 14/02/2020
        else if(iType == PLGFIN) // YBO 29/06/2020
            values.put(COL_TYPE,TYPE_PLGFIN);
        else if(iType == BRDMATIN3) // YBO 29/06/2020
            values.put(COL_TYPE,TYPE_BRDIN3);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(GAMES_TABLE_NAME, null, values);
        if (rowId > 0) {
            Uri myUri = null;
            if(iType == PUZZLES)
            	myUri = ContentUris.withAppendedId(CONTENT_URI_PUZZLES, rowId);
            // YBO 14/02/2020
            else if(iType == MATINONE)
                myUri = ContentUris.withAppendedId(CONTENT_URI_MATINONE, rowId);
            // FIN YBO 14/02/2020
            // YBO 29/06/2020
            else if(iType == PLGFIN)
                myUri = ContentUris.withAppendedId(CONTENT_URI_PLGFIN, rowId);
            else if(iType == BRDMATIN3)
                myUri = ContentUris.withAppendedId(CONTENT_URI_BRDMATIN3, rowId);
            // FIN YBO 29/06/2020
            else if(iType == PRACTICES)
            	myUri = ContentUris.withAppendedId(CONTENT_URI_PRACTICES, rowId);
            else
                System.out.println("insert() : Cas impossible");
            
            getContext().getContentResolver().notifyChange(myUri, null);
            return myUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case PUZZLES:
            count = db.delete(GAMES_TABLE_NAME, COL_TYPE + "=" + TYPE_PUZZLE
            		+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        case PUZZLES_ID: case PRACTICES_ID: case MATINONE_ID: case PLGFIN_ID: case BRDMATIN3_ID: // YBO 29/06/2020
            String id = uri.getPathSegments().get(1);
            count = db.delete(GAMES_TABLE_NAME, COL_ID + "=" + id
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
// YBO 03/04/2020
        case PRACTICES:
            count = db.delete(GAMES_TABLE_NAME, COL_TYPE + "=" + TYPE_PRACTICE
            		+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;
// YBO 14/02/2020
            case MATINONE:
                count = db.delete(GAMES_TABLE_NAME, COL_TYPE + "=" + TYPE_MATEINONE
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
// FIN YBO 14/02/2020
// YBO 29/06/2020
            case PLGFIN:
                count = db.delete(GAMES_TABLE_NAME, COL_TYPE + "=" + TYPE_PLGFIN
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
            case BRDMATIN3:
                count = db.delete(GAMES_TABLE_NAME, COL_TYPE + "=" + TYPE_BRDIN3
                        + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
                break;
// FIN YBO 29/06/2020
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        // YBO 03/04/2020 PUZZLES_ID
        // YBO 14/02/2020 MATINONE_ID:
        // YBO 29/06/2020 : PLGFIN et BRDIN3

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case PUZZLES_ID: case PRACTICES_ID: case MATINONE_ID: case PLGFIN_ID: case BRDMATIN3_ID:
            String gameId = uri.getPathSegments().get(1);
            count = db.update(GAMES_TABLE_NAME, values, COL_ID + "=" + gameId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sGamesProjectionMap = new HashMap<String, String>();
        sGamesProjectionMap.put(COL_ID, COL_ID);
        sGamesProjectionMap.put(COL_TYPE, COL_TYPE);
        sGamesProjectionMap.put(COL_PGN, COL_PGN);
        
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "puzzles", PUZZLES);
        sUriMatcher.addURI(AUTHORITY, "puzzles/#", PUZZLES_ID);
        // YBO 03/04/2020
        
        sUriMatcher.addURI(AUTHORITY, "practices", PRACTICES);
        sUriMatcher.addURI(AUTHORITY, "practices/#", PRACTICES_ID);
         
        //sUriMatcher.addURI(AUTHORITY, "practices", MATINONE);
        //sUriMatcher.addURI(AUTHORITY, "practices/#", MATINONE_ID);
        sUriMatcher.addURI(AUTHORITY, "Mixed", MATINONE);
        sUriMatcher.addURI(AUTHORITY, "Mixed/#", MATINONE_ID);

        sUriMatcher.addURI(AUTHORITY, "plgfin", PLGFIN);
        sUriMatcher.addURI(AUTHORITY, "plgfin/#", PLGFIN_ID);

        sUriMatcher.addURI(AUTHORITY, "brdmatin3", BRDMATIN3);
        sUriMatcher.addURI(AUTHORITY, "brdmatin3/#", BRDMATIN3_ID);

    }
}
