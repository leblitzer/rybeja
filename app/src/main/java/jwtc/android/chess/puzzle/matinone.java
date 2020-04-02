package jwtc.android.chess.puzzle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.InputStream;

import jwtc.android.chess.HtmlActivity;
import jwtc.android.chess.MyBaseActivity;
import jwtc.android.chess.R;

// YBO 14/02/2020 : Création

public class matinone extends MyBaseActivity {
	
    /** instances for the view and game of chess **/
	private ChessViewMatInOne _chessView; //YBO 14/02/2020
    public static final String TAG = "practice";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.practice);

        this.makeActionOverflowMenuShown();

        _chessView = new ChessViewMatInOne(this); //YBO 14/02/2020
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.practice_topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_help:
                Intent i = new Intent();
                i.setClass(matinone.this, HtmlActivity.class);
                i.putExtra(HtmlActivity.HELP_MODE, "help_practice");
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	/**
	 * 
	 */
    @Override
    protected void onResume() {
        
		SharedPreferences prefs = this.getPrefs();

		 final Intent intent = getIntent();
	     Uri uri = intent.getData();
	     InputStream is = null;
	     if(uri != null){
			try {
				is = getContentResolver().openInputStream(uri);
			} catch(Exception ex){}
	     }
        _chessView.OnResume(prefs, is);
        _chessView.updateState();
		
        super.onResume();
    }


    @Override
    protected void onPause() {
        
        SharedPreferences.Editor editor = this.getPrefs().edit();
        _chessView.OnPause(editor);

        editor.commit();
        
        super.onPause();
    }
    @Override
    protected void onDestroy(){
    	_chessView.OnDestroy();
    	super.onDestroy();
    }


    public void flipBoard(){
    	_chessView.flipBoard();
    }
}