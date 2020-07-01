package rybeja.android.chess.puzzle;

import android.content.UriMatcher;
import android.net.Uri;
import rybeja.chess.ChessPuzzleProvider;

// YBO 09/05/2020 : jwtc -> rybeja
public class MyPuzzleProvider extends ChessPuzzleProvider{

	static {

		// YBO 09/05/2020 AUTHORITY = "jwtc.android.chess.puzzle.MyPuzzleProvider";
		AUTHORITY = "rybeja.android.chess.puzzle.MyPuzzleProvider"; // YBO 09/05/2020
		CONTENT_URI_PUZZLES = Uri.parse("content://"  + AUTHORITY + "/puzzles");
		CONTENT_URI_PRACTICES = Uri.parse("content://"  + AUTHORITY + "/practices"); //practices
		CONTENT_URI_MATINONE = Uri.parse("content://"  + AUTHORITY + "/Mixed"); // YBO 14/02/2020

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "puzzles", PUZZLES);
        sUriMatcher.addURI(AUTHORITY, "puzzles/#", PUZZLES_ID);

        // YBO 03/04/2020
        sUriMatcher.addURI(AUTHORITY, "practices", PRACTICES);
        sUriMatcher.addURI(AUTHORITY, "practices/#", PRACTICES_ID);
		 

		/*sUriMatcher.addURI(AUTHORITY, "practices", MATINONE);
		sUriMatcher.addURI(AUTHORITY, "practices/#", MATINONE_ID);*/

        sUriMatcher.addURI(AUTHORITY, "Mixed", MATINONE);
        sUriMatcher.addURI(AUTHORITY, "Mixed/#",MATINONE_ID);
	}
}