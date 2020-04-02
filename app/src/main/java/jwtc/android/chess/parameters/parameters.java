package jwtc.android.chess.parameters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

import jwtc.android.chess.MyBaseActivity;
import jwtc.android.chess.R;

/**
 * Activity for reading data from an NDEF Tag.
 *
 * @author YBO    : 08/02/2020 : Création
 */
//----------------------------------------------------------------------------

public class parameters extends MyBaseActivity {
    //----------------------------------------------------------------------------
    private EditText et_number_puzzle;
    private EditText et_elo;
    private EditText et_total_time;
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor; // YBO 08/02/2020
    private Resources resources;
    //----------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        et_number_puzzle = (EditText) findViewById(R.id.et_number_puzzle);
        et_elo = (EditText) findViewById(R.id.et_elo);
        et_total_time = (EditText) findViewById(R.id.et_total_time);
        resources = this.getResources();
        prefs = this.getSharedPreferences(resources.getString(R.string.s_chess_player), Context.MODE_PRIVATE);
        editor = prefs.edit();
        et_number_puzzle.setText("" + (1 + prefs.getInt(resources.getString(R.string.s_practice_pos) ,0)));
        et_elo.setText("" + prefs.getInt(resources.getString(R.string.s_practice_elo),0));
        et_total_time.setText(""+ prefs.getInt(resources.getString(R.string.s_practice_ticks),0));
    }
    //----------------------------------------------------------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            saveEditor();
            finish();
        }
        return true;
    }
    //----------------------------------------------------------------------------
    protected void onDestroy() {
        super.onDestroy();
        saveEditor();
    }
    //----------------------------------------------------------------------------
    private void saveEditor(){
        int i;
        i = getInteger(et_number_puzzle );
        if(i>=0){ editor.putInt(resources.getString(R.string.s_practice_pos), i-1); }
        i = getInteger(et_elo );
        if(i>=0){ editor.putInt(resources.getString(R.string.s_practice_elo), i); }
        i = getInteger(et_total_time );
        if(i>=0){ editor.putInt(resources.getString(R.string.s_practice_ticks), i); }
        editor.commit();
    }
    //----------------------------------------------------------------------------
    private int getInteger(EditText et){
        int i=-1;
        if(!et.getText().toString().isEmpty()){
            try {
                i = Integer.parseInt(et.getText().toString());
            }catch(Exception ex) {System.out.println("parameters.getInteger.et="+et);}
        }
        return i;
    }
    //----------------------------------------------------------------------------


    //----------------------------------------------------------------------------

}
