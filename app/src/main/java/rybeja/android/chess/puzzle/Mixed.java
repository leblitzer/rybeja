package rybeja.android.chess.puzzle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.InputStream;

import rybeja.android.chess.HtmlActivity;
import rybeja.android.chess.MyBaseActivity;
import rybeja.android.chess.R;
import rybeja.android.chess.parameters.parameters;

// YBO 14/02/2020 : Création

public class Mixed extends MyBaseActivity {
	
    /** instances for the view and game of chess **/
	private ChessViewMixed _chessView; // YBO 14/02/2020
    public static final String TAG = "practice";
    private int _bundleTypePosition; // YBO 20/04/2020

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.practice);

        this.makeActionOverflowMenuShown();

        Intent intent = getIntent();
        if (intent != null){
            if (intent.hasExtra(getString(R.string.bundle_type_position))){
                _bundleTypePosition = intent.getIntExtra(getString(R.string. bundle_type_position), 0);
            }
        }
        _chessView = new ChessViewMixed(this, _bundleTypePosition); //YBO 14/02/2020
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
        Intent i;
        switch (item.getItemId()) {
            // YBO 18/04/2020
            case R.id.action_params:
                i = new Intent();
                i.setClass(Mixed.this, parameters.class);
                i.putExtra(getString(R.string.bundle_type_position), _bundleTypePosition);
                startActivity(i);
                return true;
            // FIN YBO 18/04/2020
            case R.id.action_help:
                i = new Intent();
                i.setClass(Mixed.this, HtmlActivity.class);
                // YBO 17/04/2020 i.putExtra(HtmlActivity.HELP_MODE, "help_practice");
                i.putExtra(HtmlActivity.HELP_MODE, "help_mixed"); // YBO 17/04/2020
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