package rybeja.android.chess.puzzle;

import rybeja.android.chess.*;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;


import rybeja.chess.Move;
import rybeja.chess.board.BoardConstants;
import rybeja.chess.board.BoardMembers;
import rybeja.android.chess.ChessImageView;
import rybeja.android.chess.ChessViewBase;
import rybeja.android.chess.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.*;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.*;

/**
 * YBO : 23/01/2020 : No flip et elo
 * YBO : 24/01/2020 : On cache le bouton next et show
 * YBO : 27/01/2020 : Sauvegarde elo tick et pos à chaque changement de problème
 */
public class ChessViewPractice extends UI {
    private ChessViewBase _view;
    private TextView _tvPracticeMove, _tvPracticeTime, _tvPracticeAvgTime;
    private TextView _tvPracticeElo; // YBO 23/01/2020
    private Button _butShow;
    private ImageButton _butPause, _butNext;
    private practice _parent;
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
                _cursor = _parent.managedQuery(MyPuzzleProvider.CONTENT_URI_PRACTICES, MyPuzzleProvider.COLUMNS, null, null, "");
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
            _tvPracticeTime.setText(formatTime(msg.getData().getInt("ticks")));
        }

    };

    public ChessViewPractice(final Activity activity) {
        super();
        _parent = (practice) activity;
        _view = new ChessViewBase(activity);

        _cr = activity.getContentResolver();
        _prefs = activity.getSharedPreferences("ChessPlayer", Context.MODE_PRIVATE); //27/01/2020
        _editor = _prefs.edit();  //27/01/2020

        _tvPracticeMove = (TextView) _parent.findViewById(R.id.TextViewPracticeMove);
        _tvPracticeTime = (TextView) _parent.findViewById(R.id.TextViewPracticeTime);
        _tvPracticeElo = (TextView) _parent.findViewById(R.id.TextViewPracticeElo); // YBO 23/01/2020
        _tvPracticeAvgTime = (TextView) _parent.findViewById(R.id.TextViewPracticeAvgTime);

        _viewBoard = (View) _parent.findViewById(R.id.includeboard);

        _switchTurn = (ViewSwitcher) _parent.findViewById(R.id.ImageTurn);

        _imgStatus = (ImageView) _parent.findViewById(R.id.ImageStatus);

        _cnt = 0;
        _num = 1;

        _iPos = 0;
        _elo = 0 ; // YBO 23/01/2020
        _elo_nc =_parent.getResources().getInteger(R.integer.elo_nc); // YBO 24/01/2020
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
        _butNext = (ImageButton) _parent.findViewById(R.id.ButtonPracticePrev); // YBO 16/04/2020 ButtonPracticeNext

        _butNext.setEnabled(false); //YBO 24/01/2020
        _butShow.setEnabled(false); //YBO 24/01/2020

        _butShow.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {

                jumptoMove(_jni.getNumBoard());
                updateState();
                if (_arrPGN.size() == _jni.getNumBoard() - 1) {
                    //YBO 24/01/2020 _butNext.setEnabled(true);
                    _butNext.setEnabled(false); //YBO 24/01/2020
                    _butShow.setEnabled(false);
                }
            }
        });

        _butNext.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                //YBO 27/01/2020 _butNext.setEnabled(false);
                //YBO 24/01/2020 _butShow.setEnabled(true);
                _butShow.setEnabled(false);
                play(_iPos--, _elo--); //YBO 27/01/2020
                /* _tvPracticeElo.setText("" + _elo); //YBO 27/01/2020
                _switchTurn.setDisplayedChild(1); //YBO 27/01/2020 pour forcer l'aff de _tvPracticeTime
                _switchTurn.setDisplayedChild(0); //YBO 27/01/2020 pour forcer l'aff de _tvPracticeTime
                _tvPracticeElo.setText("" + _elo); //YBO 27/01/2020 */


            }
        });
        _butNext.setEnabled(true); //YBO 27/01/2020
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
                play(); // YBO 27/01/2020
                saveEditor(_editor); // YBO 27/01/2020
                _editor.commit(); // YBO 27/01/2020
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
    public void play(){ play(_iPos++, _elo++); } // YBO 27/01/2020
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
        sPGN = _cursor.getString(_cursor.getColumnIndex(MyPuzzleProvider.COL_PGN));

        Log.i("ChessViewPractice", "init: " + sPGN);

        loadPGN(sPGN);

        jumptoMove(0);

        Log.i("ChessViewPractice", _arrPGN.size() + " moves from " + sPGN + " turn " + _jni.getTurn());

        int turn = _jni.getTurn();
        if (turn == BoardConstants.BLACK && false == _view.getFlippedBoard() ||
                turn == BoardConstants.WHITE && _view.getFlippedBoard()) {
            // YBO 23/01/2020 _view.flipBoard();
        }
        _tvPracticeMove.setText("# " + _iPos + "/" + _numTotal );

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
    private void saveEditor(SharedPreferences.Editor editor){
        if (_iPos > 0) {
            editor.putInt("practicePos", _iPos - 1);
        }
        editor.putInt("practiceElo", _elo); // YBO 23/01/2020
        editor.putInt("practiceTicks", _ticks);
    }

    public void OnResume(final SharedPreferences prefs, final InputStream isExtra) {
        super.OnResume();

        ChessImageView._colorScheme = prefs.getInt("ColorScheme", 0);

        _view.setFlippedBoard(false);
        _isPlaying = true;
        _ticks = prefs.getInt("practiceTicks", 0);
        _playTicks = 0;

        _iPos = prefs.getInt("practicePos", 0);
        _elo = prefs.getInt("practiceElo", _elo_nc); // YBO 23/01/2020

        _cursor = _parent.managedQuery(MyPuzzleProvider.CONTENT_URI_PRACTICES, MyPuzzleProvider.COLUMNS, null, null, "");

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
                                is = _parent.getAssets().open("practice.txt");
                                //is = _parent.getAssets().open("Mixed.txt"); // YBO 14/02/2020
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

                            Uri uri = MyPuzzleProvider.CONTENT_URI_PRACTICES;

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
}