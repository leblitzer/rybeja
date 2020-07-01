package rybeja.android.chess;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;

import rybeja.android.chess.ChessPreferences;
import rybeja.android.chess.HtmlActivity;
import rybeja.android.chess.ics.ICSClient;
import rybeja.android.chess.main;
import rybeja.android.chess.parameters.parameters;
import rybeja.android.chess.puzzle.Mixed;
import rybeja.android.chess.puzzle.puzzle;
import rybeja.android.chess.tools.pgntool;

/**
 * YBO : 07/02/2020 : Ajout de parameters
 * YBO : 14/02/2020 : Ajout de mat en 2 : start_menu pour le menu
 * YBO : 29/06/2020 : Ajout des BRD
 */

public class start extends Activity {

	public static final String TAG = "start";
	private static String _ssActivity = "";
	private ListView _list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences getData = getSharedPreferences("ChessPlayer", Context.MODE_PRIVATE);
		String myLanguage  	= getData.getString("localelanguage", "");

		Locale current = getResources().getConfiguration().locale;
		String language = current.getLanguage();
		if(myLanguage.equals("")){    // localelanguage not used yet? then use device default locale
			myLanguage = language;
		}

		Locale locale = new Locale(myLanguage);    // myLanguage is current language
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());

		setContentView(R.layout.start);

		if (getIntent().getBooleanExtra("RESTART", false)) {
			finish();
			Intent intent = new Intent(this, start.class);
			startActivity(intent);
		}

		String[] title = getResources().getStringArray(R.array.start_menu);

		_list = (ListView)findViewById(R.id.ListStart);
		_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				_ssActivity = parent.getItemAtPosition(position).toString();
				try {
					Intent i = new Intent();
					Log.i("start", _ssActivity);
					if (_ssActivity.equals(getString(R.string.start_play))) {
						i.setClass(start.this, main.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					} else if (_ssActivity.equals(getString(R.string.start_practice))) {
						// YBO 03/04/2020 i.setClass(start.this, practice.class);
						i.setClass(start.this, Mixed.class);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_practice);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// YBO 14/02/2020
					} else if (_ssActivity.equals(getString(R.string.start_mat_1))) {
						i.setClass(start.this, Mixed.class);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_mat_1);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// YBO 13/04/2020 mat2 -> Puzzle PLG - > PLG mat en 2
					} else if (_ssActivity.equals(getString(R.string.start_puzzles_plg_mat_in_2))) {
						i.setClass(start.this, Mixed.class);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_puzzles_plg_mat_in_2);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// YBO 29/06/2020 Puzzle PLG FIN
					} else if (_ssActivity.equals(getString(R.string.start_puzzles_plg_fin))) {
						i.setClass(start.this, Mixed.class);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_puzzles_plg_fin);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// YBO 13/04/2020 Puzzle ctsw
					} else if (_ssActivity.equals(getString(R.string.start_puzzles_ctsw))) {
						i.setClass(start.this, Mixed.class);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_puzzles_ctsw);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// YBO 29/06/2020 Puzzle brd
					} else if (_ssActivity.equals(getString(R.string.start_puzzles_brd_mat_in_3))) {
						i.setClass(start.this, Mixed.class);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_puzzles_brd_mat_in_3);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// FIN YBO 14/02/2020
					} else if (_ssActivity.equals(getString(R.string.start_puzzles))) {
						i.setClass(start.this, puzzle.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
					} else if (_ssActivity.equals(getString(R.string.start_about))) {
						i.setClass(start.this, HtmlActivity.class);
						i.putExtra(HtmlActivity.HELP_MODE, "about");
						startActivity(i);
					} else if (_ssActivity.equals(getString(R.string.start_ics))) {
						i.setClass(start.this, ICSClient.class);
						startActivity(i);
					} else if (_ssActivity.equals(getString(R.string.start_pgn))) {
						i.setClass(start.this, pgntool.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(i);
						// YBO 07/02/2020
					} else if (_ssActivity.equals(getString(R.string.start_parameters))) {
						i.setClass(start.this, parameters.class);
						i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						i.putExtra(getString(R.string.bundle_type_position), R.string.start_mat_1);
						startActivity(i);
						// FIN YBO 07/02/2020
					} else if (_ssActivity.equals(getString(R.string.start_globalpreferences))) {
						i.setClass(start.this, ChessPreferences.class);
						startActivityForResult(i, 0);
					} else if (_ssActivity.equals(getString(R.string.menu_help))) {
						i.setClass(start.this, HtmlActivity.class);
						i.putExtra(HtmlActivity.HELP_MODE, "help");
						startActivity(i);
					}
				} catch (Exception ex) {
					Toast t = Toast.makeText(start.this, R.string.toast_could_not_start_activity, Toast.LENGTH_LONG);
					t.setGravity(Gravity.BOTTOM, 0, 0);
					t.show();
				}
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1){
			Log.i(TAG, "finish and restart");

			Intent intent = new Intent(this, start.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("RESTART", true);
			startActivity(intent);

		}
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		SharedPreferences getData = getSharedPreferences("ChessPlayer", Context.MODE_PRIVATE);
		if (getData.getBoolean("RESTART", false)) {
			finish();
			Intent intent = new Intent(this, start.class);
			//intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			SharedPreferences.Editor editor = getData.edit();
			editor.putBoolean("RESTART", false);
			editor.apply();

			startActivity(intent);
		}
	}

	public static String get_ssActivity(){
		return _ssActivity;
	}
}
