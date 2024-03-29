/***************************************/
// YBO 14/02/2020 Création
// YBO 07/09/2023 Ajout setPublicite
/***************************************/

package rybeja.android.chess.puzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import rybeja.android.chess.ChessImageView;
import rybeja.android.chess.ChessViewBase;
import rybeja.android.chess.R;
import rybeja.android.chess.UI;
import rybeja.chess.Move;
import rybeja.chess.board.BoardConstants;
import rybeja.chess.board.BoardMembers;

public class ChessViewMixed extends UI {
    private ChessViewBase _view;
    private TextView _tvPracticeMove, _tvPracticeTime, _tvPracticeAvgTime;
    private TextView _tvPracticeElo; // YBO 23/01/2020
    private Button _butShow;
    private ImageButton _butPause, _butPrevious;
    private Mixed _parent; //YBO 14/02/2020
    private int _numTotal, _iPos;
    private int _elo, _elo_nc; // YBO 23/01/2020
    private Cursor _cursor;
    private Timer _timer;
    private int _ticks, _playTicks;
    private boolean _isPlaying;
    private int _cnt, _num;
    private View _viewBoard;
    private ViewSwitcher _switchTurn;
    private ImageView _imgStatus;
    // YBO 02/04/2020
    private static Uri aPuzzleProvider;
    //private int aType=0;
    private String aFileName;
    private int aTypeFenOrPgn;
    private boolean aforceFirstMove; // YBO 15/04/2020
    private int aBundleTypePosition; // YBO 20/04/2020
    private Resources aResources; // YBO 20/04/2020
    private String aSMixedPos; // YBO 20/04/2020
    private String aSMixedElo; // YBO 20/04/2020
    private String aSMixedTicks; // YBO 20/04/2020
    private String aSReset; // YBO 20/02/2022
    // FIN YBO 02/04/2020
    protected ContentResolver _cr;
    protected SharedPreferences _prefs; // YBO 27/01/2020
    protected SharedPreferences.Editor _editor; // YBO 27/01/2020
    protected Thread _thread;
    protected ProgressDialog _progressDlg;

    protected Handler m_threadHandler = new Handler() {
        /** Gets called on every message that is received */
        // @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                _progressDlg.hide();
                _tvPracticeMove.setText("Installation finished.");
                _cursor = _parent.managedQuery(aPuzzleProvider, MyPuzzleProvider.COLUMNS, null, null, ""); // YBO 14/02/2020
                _numTotal = _cursor.getCount();
                firstPlay();
            } else if (msg.what == 2) {
                _progressDlg.setMessage(_parent.getString(R.string.msg_progress) + String.format(" %d", (_cnt * 100) / _num) + " %");
            } else if (msg.what == 3) {
                _progressDlg.hide();
                _tvPracticeMove.setText("An error occured during install");
            }
        }

    };


    protected Handler m_timerHandler = new Handler() {
        /** Gets called on every message that is received */
        // @Override
        public void handleMessage(Message msg) {
            //_tvPracticeElo.setText("" + _elo); // YBO 21/04/2020
            _tvPracticeTime.setText(formatTime(msg.getData().getInt("ticks")));
            int i = 0;
        }

    };

    //YBO 03/04/2020 : Ajout de typeProb à la signature
    public ChessViewMixed(final Activity activity, int bundleTypePosition) {
        super();
        aforceFirstMove = false; // YBO 15/04/2020
        aBundleTypePosition = bundleTypePosition;
        _parent = (Mixed) activity;
        aResources = _parent.getResources();
        aSMixedPos = aResources.getString(R.string.s_mixed_pos) + aBundleTypePosition;
        aSMixedElo = aResources.getString(R.string.s_mixed_elo) + aBundleTypePosition;
        aSMixedTicks =  aResources.getString(R.string.s_mixed_ticks) + aBundleTypePosition;
        aSReset = aResources.getString(R.string.s_reset) + aBundleTypePosition; // YBO 20/02/2022
        switch (bundleTypePosition) {
            case R.string.start_mat_1:
                aPuzzleProvider = MyPuzzleProvider.CONTENT_URI_MATINONE;
                aFileName = "fic01_matinone.txt";
                aTypeFenOrPgn = R.integer.type_fen;
                break;
            case R.string.start_puzzles_plg_mat_in_2:  //YBO 29/06/2020
                aPuzzleProvider = MyPuzzleProvider.CONTENT_URI_PRACTICES;
                aFileName = "fic02_puzzle_plg_mat_en_2.pgn";
                aTypeFenOrPgn = R.integer.type_pgn;
                break;
            case R.string.start_puzzles_plg_fin:  //YBO 29/06/2020
                aPuzzleProvider = MyPuzzleProvider.CONTENT_URI_PLGFIN;
                aFileName = "fic03_puzzle_plg_fin.pgn";
                aTypeFenOrPgn = R.integer.type_pgn;
                break;
            case R.string.start_puzzles_ctsw:
                _parent = (Mixed) activity;
                aPuzzleProvider = MyPuzzleProvider.CONTENT_URI_PUZZLES;
                aFileName = "fic04_puzzles_cts_white.pgn";
                aTypeFenOrPgn = R.integer.type_pgn;
                aforceFirstMove = true; // YBO 15/04/2020
                break;
            case R.string.start_puzzles_brd_mat_in_2:  //YBO 18/07/2020
                aPuzzleProvider = MyPuzzleProvider.CONTENT_URI_BRDMATIN2;
                aFileName = "fic05_puzzle_brd_mat_en_2.pgn";
                aTypeFenOrPgn = R.integer.type_pgn;
                break;
            case R.string.start_puzzles_brd_mat_in_3:  //YBO 29/06/2020
                aPuzzleProvider = MyPuzzleProvider.CONTENT_URI_BRDMATIN3;
                aFileName = "fic06_puzzle_brd_mat_en_3.pgn";
                aTypeFenOrPgn = R.integer.type_pgn;
        }


        _view = new ChessViewBase(activity);

        _cr = activity.getContentResolver();
        _prefs = activity.getSharedPreferences("ChessPlayer", Context.MODE_PRIVATE); //27/01/2020
        _editor = _prefs.edit();  //27/01/2020

        _tvPracticeMove = (TextView) _parent.findViewById(R.id.TextViewPracticeMove);
        _tvPracticeTime = (TextView) _parent.findViewById(R.id.TextViewPracticeTime);
        _tvPracticeElo = (TextView) _parent.findViewById(R.id.TextViewPracticeElo); // YBO 23/01/2020
        _tvPracticeAvgTime = (TextView) _parent.findViewById(R.id.TextViewPracticeAvgTime);

        _viewBoard = (View) _parent.findViewById(R.id.includeboard);

        setPublicite(_viewBoard); // YBO 07/09/2023

        _switchTurn = (ViewSwitcher) _parent.findViewById(R.id.ImageTurn);

        _imgStatus = (ImageView) _parent.findViewById(R.id.ImageStatus);

        _cnt = 0;
        _num = 1;

        _iPos = 0;
        _elo = 0; // YBO 23/01/2020
        _elo_nc = _parent.getResources().getInteger(R.integer.elo_nc); // YBO 24/01/2020
        _isPlaying = false;

        OnClickListener ocl = new OnClickListener() {
            public void onClick(View arg0) {
                handleClick(_view.getIndexOfButton(arg0));
            }
        };

        OnLongClickListener olcl = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                handleClick(_view.getIndexOfButton(view));
                return true;
            }
        };

        _view.init(ocl, olcl);
        init();

        _butShow = (Button) _parent.findViewById(R.id.ButtonPracticeShow);
        // YBO 16/04/2020 _butPrevious = (ImageButton) _parent.findViewById(R.id.ButtonPracticeNext);
        _butPrevious = (ImageButton) _parent.findViewById(R.id.ButtonPracticePrev); // YBO 16/04/2020

        _butPrevious.setEnabled(false); //YBO 24/01/2020
        _butShow.setEnabled(false); //YBO 24/01/2020

        _butShow.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                jumptoMove(_jni.getNumBoard());
                updateState();
                if (_arrPGN.size() == _jni.getNumBoard() - 1) {
                    //YBO 24/01/2020 _butNext.setEnabled(true);
                    _butPrevious.setEnabled(false); //YBO 24/01/2020
                    _butShow.setEnabled(false);
                }
            }
        });

        _butPrevious.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if(_iPos > 1) {
                    //YBO 27/01/2020 _butNext.setEnabled(false);
                    //YBO 24/01/2020 _butShow.setEnabled(true);
                    _butShow.setEnabled(false);
                    play(_iPos--, _elo--); //YBO 27/01/2020
                    _tvPracticeElo.setText("" + _elo); //YBO 27/01/2020
                    /*
                _switchTurn.setDisplayedChild(1); //YBO 27/01/2020 pour forcer l'aff de _tvPracticeTime
                _switchTurn.setDisplayedChild(0); //YBO 27/01/2020 pour forcer l'aff de _tvPracticeTime
                _tvPracticeElo.setText("" + _elo); //YBO 27/01/2020 */
                    //if(aforceFirstMove) {doMove();} // YBO 16/04/2020


                }

            }
        });
        _butPrevious.setEnabled(true); //YBO 27/01/2020
        _butPause = (ImageButton) _parent.findViewById(R.id.ButtonPracticePause);
        _butPause.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (_viewBoard.getVisibility() == View.VISIBLE) {
                    _timer.cancel();
                    _timer = null;
                    _viewBoard.setVisibility(View.INVISIBLE);
                } else {
                    _viewBoard.setVisibility(View.VISIBLE);
                    scheduleTimer();
                }
            }
        });


    }

    public void init() {

    }


    private String formatTime(int sec) {

        return String.format("%d:%02d", (int) (Math.floor(sec / 60)), sec % 60);
    }

    @Override
    public void paintBoard() {

        int[] arrSelPositions;

        int lastMove = _jni.getMyMove();
        if (lastMove != 0) {
            arrSelPositions = new int[3];
            arrSelPositions[0] = m_iFrom;
            arrSelPositions[1] = Move.getTo(lastMove);
            arrSelPositions[2] = Move.getFrom(lastMove);
        } else {
            arrSelPositions = new int[1];
            arrSelPositions[0] = m_iFrom;
        }

        _view.paintBoard(_jni, arrSelPositions, null);

    }

    public int getPlayMode() {
        return HUMAN_PC;
    }

    public void flipBoard() {
        _view.flipBoard();
        updateState();
    }


    @Override
    protected boolean requestMove(int from, int to) {
        if (_arrPGN.size() <= _jni.getNumBoard() - 1) {
            setMessage("Finished position");
            return false;
        }
        int move = _arrPGN.get(_jni.getNumBoard() - 1)._move;
        int theMove = Move.makeMove(from, to);

        if (Move.equalPositions(move, theMove)) {
            jumptoMove(_jni.getNumBoard());

            updateState();



            if (_arrPGN.size() == _jni.getNumBoard() - 1) {
                //play();
                //play(); // YBO 23/01/2020
                _imgStatus.setImageResource(R.drawable.indicator_ok);
                //YBO 23/01/2020 setMessage("Correct!");
                _isPlaying = false;
                //YBO 24/01/2020 _butNext.setEnabled(true);
                _butShow.setEnabled(false);
                play(_iPos++, _elo++); // YBO 27/01/2020
                saveEditor(_editor); // YBO 27/01/2020
                _editor.commit(); // YBO 27/01/2020
                _tvPracticeElo.setText("" + _elo); //YBO 21/04/2020
            } else {
                jumptoMove(_jni.getNumBoard());
                updateState();
            }

            return true;
        } else {
            _imgStatus.setImageResource(R.drawable.indicator_error);
            setMessage(Move.toDbgString(theMove) + (checkIsLegalMove(from, to) ? " is not expected" : " is an illegal move"));
            m_iFrom = -1;
            _elo -= 10; // YBO 23/01/2020
            _tvPracticeElo.setText("" + _elo); // YBO 23/01/2020
        }

        updateState();

        return true;
    }

    @Override
    public void play() {
        play(_iPos++, _elo);

    } // YBO 27/01/2020

    public void play(int pos, int _elo) {
        m_iFrom = -1;
        String sPGN;
        _isPlaying = true;
        // YBO 27/01/2020 _iPos++;
        //_elo++; // YBO 23/01/2020
        if (_iPos > _numTotal) {
            _isPlaying = false;
            /* YBO 02/04/2020
            _iPos = 0;
            _ticks = 0;
            _elo = _elo_nc; // YBO 23/01/2020
             */
            setMessage("You completed all puzzles!!!");
            return;
        }

        _playTicks = 0;

        _cursor.moveToPosition(_iPos - 1);
        int columnIndex = _cursor.getColumnIndex(MyPuzzleProvider.COL_PGN);
        sPGN = _cursor.getString(columnIndex);

        Log.i("ChessViewPractice", "init: " + sPGN);

        loadPGN(sPGN);

        jumptoMove(0);

        Log.i("ChessViewPractice", _arrPGN.size() + " moves from " + sPGN + " turn " + _jni.getTurn());

        int turn = _jni.getTurn();
        if (turn == BoardConstants.BLACK && false == _view.getFlippedBoard() ||
                turn == BoardConstants.WHITE && _view.getFlippedBoard()) {
            // YBO 23/01/2020 _view.flipBoard();
        }
        _tvPracticeMove.setText("# " + _iPos + "/" + _numTotal);

        if (turn == BoardConstants.BLACK) {
            _switchTurn.setDisplayedChild(1); //YBO 27/01/2020 pour forcer l'aff de _tvPracticeTime
            _switchTurn.setDisplayedChild(0);
        } else {
            _switchTurn.setDisplayedChild(0); //YBO 27/01/2020 pour forcer l'aff de _tvPracticeTime
            _switchTurn.setDisplayedChild(1);
        }

        _imgStatus.setImageResource(R.drawable.indicator_none);

        Float f = (float) (_ticks) / (_iPos);
        //_tvPracticeTime.setText(""+_ticks); // YBO 27/01/2020
        _tvPracticeAvgTime.setText(String.format("%.1f", f));
        _tvPracticeElo.setText("" + _elo); //YBO 23/01/2020


        updateState();
        if(aforceFirstMove) {doMove();} // YBO 16/04/2020
    }


    @Override
    public boolean handleClick(int index) {
        final int iTo = _view.getFieldIndex(index);
        if (m_iFrom != -1) {

            if (_jni.pieceAt(BoardConstants.WHITE, m_iFrom) == BoardConstants.PAWN &&
                    BoardMembers.ROW_TURN[BoardConstants.WHITE][m_iFrom] == 6 &&
                    BoardMembers.ROW_TURN[BoardConstants.WHITE][iTo] == 7
                    ||
                    _jni.pieceAt(BoardConstants.BLACK, m_iFrom) == BoardConstants.PAWN &&
                            BoardMembers.ROW_TURN[BoardConstants.BLACK][m_iFrom] == 6 &&
                            BoardMembers.ROW_TURN[BoardConstants.BLACK][iTo] == 7) {

                final String[] items = _parent.getResources().getStringArray(R.array.promotionpieces);

                AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
                builder.setTitle("Pick promotion piece");
                builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        _jni.setPromo(4 - item);
                        boolean bValid = requestMove(m_iFrom, iTo);
                        m_iFrom = -1;
                        //if (false == bValid) {
                        paintBoard();
                        //}
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        }
        return super.handleClick(iTo);
    }

    @Override
    public void setMessage(String sMsg) {
        _tvPracticeMove.setText(sMsg);
        //_parent.doToast(sMsg);
        //_tvMessage.setText(sMsg);
        //m_textMessage.setText(sMsg);
    }

    @Override
    public void setMessage(int res) {
        _tvPracticeMove.setText(res);
        //_parent.doToast(_parent.getString(res));
    }

    public void OnPause(SharedPreferences.Editor editor) {
        if (_timer != null)
            _timer.cancel();
        _timer = null;
        _isPlaying = false;
        saveEditor(editor); // YBO 27/01/2020
    }

    // YBO 27/01/2020
    private void saveEditor(SharedPreferences.Editor editor) {
        if (_iPos > 0) {
            editor.putInt(aSMixedPos, _iPos - 1);
        }
        editor.putInt(aSMixedElo, _elo); // YBO 23/01/2020
        editor.putInt(aSMixedTicks, _ticks);
        editor.putBoolean(aSReset, false); // YBO 20/02/2022

    }

    /*********************************************************************************************/
    private void setEditor(final SharedPreferences prefs) {
        _ticks =prefs.getInt(aSMixedTicks,0);
        _iPos =prefs.getInt(aSMixedPos,0); // YBO 06/04/2020 s_practice_pos
        _elo =prefs.getInt(aSMixedElo,_elo_nc); // YBO 23/01/2020
        int i = 0;
    }
    /*********************************************************************************************/
// YBO 03/04/2020
    public void OnResume(final SharedPreferences prefs, final InputStream isExtra) {
        //setEditor(prefs);
        switch(aTypeFenOrPgn) {
            case (R.integer.type_fen): OnResumePractice(prefs , isExtra); break;
            case (R.integer.type_pgn): OnResumePuzzle(prefs /*, isExtra*/); break;
        }
    }
    /*********************************************************************************************/
// YBO 03/04/2020
    private void OnResumePractice(final SharedPreferences prefs, final InputStream isExtra) {
        super.OnResume();
        // YBO 20/02/2022
        boolean b = _prefs.getBoolean(aSReset, false);
        if(b){_parent.getContentResolver().delete(aPuzzleProvider,null,null);}
        // FIN YBO

        ChessImageView._colorScheme = prefs.getInt("ColorScheme", 2);

        _view.setFlippedBoard(false);
        _isPlaying = true;
        _playTicks = 0;
        setEditor(prefs); // YBO 05/04/2020

        _cursor = _parent.managedQuery(aPuzzleProvider, MyPuzzleProvider.COLUMNS, null, null, ""); // YBO 14/02/2020

        if (_cursor != null) {


            _numTotal = _cursor.getCount();
            if (_numTotal == 0) {
                _tvPracticeMove.setText("Installing...");

                _progressDlg = ProgressDialog.show(_parent, _parent.getString(R.string.title_installing), _parent.getString(R.string.msg_wait), false, false);

                _thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            InputStream is;
                            if (isExtra == null) {
                                 is = _parent.getAssets().open(aFileName); // YBO 14/02/2020
                                //is = _parent.getAssets().open("practice.txt");
                            } else {
                                is = isExtra;
                            }
                            StringBuffer sb = new StringBuffer();
                            byte[] b = new byte[1024 * 32];
                            int len;
                            while ((len = is.read(b)) > 0) {
                                sb.append(new String(b, 0, len));
                            }
                            is.close();
                            ContentValues values;

                            Uri uri = aPuzzleProvider; // YBO 14/02/2020

                            String arr[] = sb.toString().split("\n");
                            String arr2[];
                            ContentResolver cr = _parent.getContentResolver();
                            _num = arr.length;

                            for (int i = 0; i < _num; i++) {

                                if (i % 100 == 0) {

                                    Message msg = new Message();
                                    msg.what = 2;
                                    m_threadHandler.sendMessage(msg);

                                }

                                arr2 = arr[i].split("@");
                                if (arr2.length == 2) {
                                    if (arr2[0].length() > 0 && arr2[1].length() > 0) {
                                        values = new ContentValues();
                                        String s = "[FEN \"" + arr2[0] + "\"]\n" + arr2[1];
                                        values.put("PGN", s);
                                        cr.insert(uri, values);

                                        _cnt++;
                                    }
                                }
                            }

                            Message msg = new Message();
                            msg.what = 1;
                            m_threadHandler.sendMessage(msg);

                            Log.i("Start", "installed...");
                        } catch (Exception ex) {
                            Log.e("Install", ex.toString());

                            Message msg = new Message();
                            msg.what = 3;
                            m_threadHandler.sendMessage(msg);
                        }
                    }
                });
                _thread.start();

                return;
            }
        } else {
            _tvPracticeMove.setText("There was a problem loading the puzzle.");
            return;
        }


        firstPlay();

    }

/*********************************************************************************************/
// YBO 03/04/2020
private void OnResumePuzzle(final SharedPreferences prefs) {
    super.OnResume();
    // YBO 22/02/2022
    boolean b = _prefs.getBoolean(aSReset, false);
    if(b){_parent.getContentResolver().delete(aPuzzleProvider,null,null);}
    // FIN YBO

    ChessImageView._colorScheme = prefs.getInt("ColorScheme", 2);

    _view.setFlippedBoard(prefs.getBoolean("flippedBoard", false));

    _iPos = prefs.getInt("puzzlePos", 0);

    _numTotal = getNumPuzzles(); // YBO 03/04/2020
    setEditor(prefs); // YBO 03/04/2020

    if (_num == 0) {

        _num = 500; // first puzzle set has fixed amount
        _progressDlg = ProgressDialog.show(_parent, _parent.getString(R.string.title_installing), _parent.getString(R.string.msg_wait), false, false);

        //if(iTmp > 0)
        //	_cr.delete(MyPuzzleProvider.CONTENT_URI_PUZZLES, "1=1", null);

        _thread = new Thread(new Runnable() {
            public void run() {

                try {
                    InputStream is = _parent.getAssets().open(aFileName); // YBO 03/04/2020

                    StringBuffer sb = new StringBuffer();
                    String s = "", data;
                    int pos1 = 0, pos2 = 0, len;
                    byte[] buffer = new byte[2048];

                    while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                        data = new String(buffer, 0, len);
                        sb.append(data);
                        pos1 = sb.indexOf("[Event \"");
                        while (pos1 >= 0) {
                            pos2 = sb.indexOf("[Event \"", pos1 + 10);
                            if (pos2 == -1)
                                break;
                            s = sb.substring(pos1, pos2);

                            processPGN(s);

                            sb.delete(0, pos2);

                            pos1 = sb.indexOf("[Event \"");
                        }
                        //break;

                        //Log.i("run", "left: " + sb);
                        //break;
                    }
                    processPGN(sb.toString());

                    Log.i("run", "Count " + _cnt);

                    Message msg = new Message();
                    msg.what = 1;
                    m_threadHandler.sendMessage(msg);

                    is.close();

                } catch (Exception ex) {
                    Log.e("Install", ex.toString());
                }
            }
        });
        _thread.start();

        return;
    }

    //YBO 03/04/2020 play();
    firstPlay();


}
/*********************************************************************************************/
// YBO 03/04/2020
public int getNumPuzzles() {

    //_cursor = _parent.managedQuery(MyPuzzleProvider.CONTENT_URI_PUZZLES, MyPuzzleProvider.COLUMNS, null, null, "");
    _cursor = _parent.managedQuery(aPuzzleProvider, MyPuzzleProvider.COLUMNS, null, null, "");

    if (_cursor != null) {
        _num = _cursor.getCount();
    } else {
        _num = 0;
    }
    return _num; // YBO 03/04/2020
}
/*********************************************************************************************/
// YBO 03/04/2020
private void processPGN(String s) {

    if (_cnt % 100 == 0) {
        Message msg = new Message();
        msg.what = 2;
        m_threadHandler.sendMessage(msg);

    }

    ContentValues values;
    values = new ContentValues();
    values.put("PGN", s);
    // YBO 12/04/2020 _cr.insert(MyPuzzleProvider.CONTENT_URI_PUZZLES, values);
    _cr.insert(aPuzzleProvider, values); // YBO 12/04/2020

    _cnt++;
}
/*********************************************************************************************/

    public void firstPlay() {
        scheduleTimer();
        play();
    }

    public void scheduleTimer() {
        _timer = new Timer(true);
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (false == _isPlaying ) {
                    // YBO 27/01/2020 Bloque le compteur
                    return;
                }
                _ticks++;
                _playTicks++;

                Message msg = new Message();
                msg.what = 1;
                Bundle bun = new Bundle();
                bun.putInt("ticks", _playTicks);
                msg.setData(bun);
                m_timerHandler.sendMessage(msg);
            }
        }, 1000, 1000);
    }
    /*********************************************************************************************/
    // YBO 16/04/2020
    private void doMove() {
        jumptoMove(_jni.getNumBoard());
        updateState();
    }
    /***************************************/
    // YBO 07/09/2023
    private void setPublicite(@NonNull View view) {
        //AdView adView = view.findViewById(R.id.adView); // assurez-vous que l'ID correspond à celui de votre AdView dans le fichier XML
        AdView adView = _parent.findViewById(R.id.adView); // assurez-vous que l'ID correspond à celui de votre AdView dans le fichier XML
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest); // Ceci charge une annonce dans votre AdView
        if(adView != null){
            Log.i("ChessViewMixed", "setPublicite : " + adView);
        }
        else{
            Log.i("ChessViewMixed", "setPublicite : null" );
        }
    }
    /***************************************/
}