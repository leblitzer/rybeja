package rybeja.android.chess.parameters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import rybeja.android.chess.MyBaseActivity;
import rybeja.android.chess.R;

/**
 * Activity for reading data from an NDEF Tag.
 *
 * @author YBO
 * 08/02/2020 : Création
 * 11/03/2021 : Ajout design piece
 * 20/02/2022 : Bouton reset
 */
//----------------------------------------------------------------------------

public class parameters extends MyBaseActivity {
    //----------------------------------------------------------------------------
    private EditText et_number_puzzle;
    private EditText et_elo;
    private EditText et_total_time;
    private EditText et_design_pieces; // YBO 11/03/2021
    private Button b_save;
    private Button b_reset; // YBO 20/02/2022
    protected SharedPreferences prefs;
    protected SharedPreferences.Editor editor; // YBO 08/02/2020
    private Resources resources;
    private int _bundleTypePosition; // YBO 20/04/2020
    private String aSMixedPos; // YBO 20/04/2020
    private String aSMixedElo; // YBO 20/04/2020
    private String aSMixedTicks; // YBO 20/04/2020
    private String aSDesignPieces; // YBO 11/03/2020
    private String aSReset; // YBO 20/02/2022
    //----------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);
        // YBO 20/04/2020
        Intent intent = getIntent();
        if (intent != null){
            if (intent.hasExtra(getString(R.string.bundle_type_position))){
                _bundleTypePosition = intent.getIntExtra(getString(R.string. bundle_type_position), 0);
            }
        }
        // FIN YBO 20/04/2020
        et_number_puzzle = (EditText) findViewById(R.id.et_number_puzzle);
        et_elo = (EditText) findViewById(R.id.et_elo);
        et_total_time = (EditText) findViewById(R.id.et_total_time);
        et_design_pieces = (EditText) findViewById(R.id.et_design_pieces);
        b_save = (Button) findViewById(R.id.b_save);
        b_reset = (Button) findViewById(R.id.b_reset); // YBO 20/02/2022
        resources = this.getResources();
        prefs = this.getSharedPreferences(resources.getString(R.string.s_chess_player), Context.MODE_PRIVATE);
        editor = prefs.edit();
        aSMixedPos = resources.getString(R.string.s_mixed_pos)+_bundleTypePosition;
        aSMixedElo = resources.getString(R.string.s_mixed_elo)+_bundleTypePosition;
        aSMixedTicks =resources.getString(R.string.s_mixed_ticks)+_bundleTypePosition;
        aSDesignPieces = resources.getString(R.string.s_design_pieces);
        aSReset = resources.getString(R.string.s_reset) + _bundleTypePosition; // YBO 20/02/2022
        et_number_puzzle.setText("" + (1 + prefs.getInt(aSMixedPos,0)));
        et_elo.setText("" + prefs.getInt(aSMixedElo, 0));
        et_total_time.setText( "" + prefs.getInt(aSMixedTicks, 0));
        et_design_pieces.setText("" + prefs.getInt(aSDesignPieces, 0));
        b_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEditor();
            }
        });
        b_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetBDTable();
            }
        }); // YBO 20/02/2022
    }
    //----------------------------------------------------------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //saveEditor();
            finish();
        }
        return true;
    }
    //----------------------------------------------------------------------------
    protected void onDestroy() {
        super.onDestroy();
        //saveEditor();
    }
    //----------------------------------------------------------------------------
    private void saveEditor(){
        int i;
        i = getInteger(et_number_puzzle );
        if(i>=1){ editor.putInt(aSMixedPos, i-1); }
        i = getInteger(et_elo );
        if(i>=0){ editor.putInt(aSMixedElo, i); }
        i = getInteger(et_total_time );
        if(i>=0){ editor.putInt(aSMixedTicks, i); }
        i = getInteger(et_design_pieces );
        if(i>=0){ editor.putInt(aSDesignPieces, i); }
        editor.commit();
    }
    //----------------------------------------------------------------------------
    // YBO 20/02/2022
    private void resetBDTable(){
        editor.putBoolean(aSReset, true);
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

}
