package rybeja.android.chess.puzzle;

import android.os.Bundle;

import rybeja.android.chess.MyPreferenceActivity;
import rybeja.android.chess.R;

public class puzzlePrefs extends MyPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.puzzleprefs);

    }
}

