package jwtc.android.chess.puzzle;

import android.content.UriMatcher;
import android.net.Uri;
import jwtc.chess.ChessPuzzleProvider;

public class MyPuzzleProvider extends ChessPuzzleProvider{

	static {

		AUTHORITY = "jwtc.android.chess.puzzle.MyPuzzleProvider";
		CONTENT_URI_PUZZLES = Uri.parse("content://"  + AUTHORITY + "/puzzles");
		CONTENT_URI_PRACTICES = Uri.parse("content://"  + AUTHORITY + "/_"); //practices
		CONTENT_URI_MATINONE = Uri.parse("content://"  + AUTHORITY + "/matinone"); // YBO 14/02/2020

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "puzzles", PUZZLES);
        sUriMatcher.addURI(AUTHORITY, "puzzles/#", PUZZLES_ID);
        
        sUriMatcher.addURI(AUTHORITY, "_", PRACTICES);
        sUriMatcher.addURI(AUTHORITY, "_/#", PRACTICES_ID);

        sUriMatcher.addURI(AUTHORITY, "matinone", MATINONE);
        sUriMatcher.addURI(AUTHORITY, "matinone/#",MATINONE_ID);
	}
}
