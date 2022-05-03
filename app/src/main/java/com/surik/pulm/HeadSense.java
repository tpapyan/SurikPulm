package com.surik.pulm;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.surik.pulm.algorithm.*;
import com.surik.pulm.model.*;
import com.surik.pulm.timer.*;
import com.surik.pulm.utils.GraphUtilities;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * all main processes are here, HeadSense is the main activity when app is lunched
 */
public class HeadSense extends AppCompatActivity implements HeadSenseModelClientInterface {

    private static final String ICP_VALUES_FILE_NAME = "icp_values";
    private static final String RP_VALUES_FILE_NAME = "rp_values";
    private static final int ACOUSTIC_GRAPH_LINE_STROKE = 2;
    private static final int SENT_SIGNAL_LINE_STROKE = 2;
    private static final String TAG = "HeadSense";
    private static boolean stopedAlgo = false;
    private static boolean badAlg = false;
    private Alg mAlg = null;
    private Handler mHandler = null;
    private Boolean mUserStop = false;

    private Switch starter;

    private LinearLayout mSentSignalWaveform;
    private ImageView mIcpClearButton;
    private ImageView mSettingsButton;
    private TextView mTimeFromStartValue;
    private TextView mDemo;
    private HeadSenseModel mHeadSenseModel;
    private static Context context;
    private static boolean applicationFirstLaunch = false;

    private int progressBarDuration = 0;
    private ProgressBar progressBar;
    private TextView progressBarPercent;
    private Runnable progressBarRunnable;
    private Handler progressBarHandler;

    private Timer_ timer;
    private Timer_task timerTask;
    private boolean timer_started;
    private boolean timer_stoped;

    private Total_Timer total_timer;
    private Total_Timer_task total_timerTask;
    private boolean total_timer_started;
    private boolean total_timer_stoped;
    private static int total_timer_minutes = 0;
    private static int total_timer_hours = 0;
    private static String stop_date = "";

    private static int finishCount = 0;
    private static int badAlgCount = 0;

    private static boolean algorithmFinished = false;

    private static int qualityGoodCount = 0;
    private static int qualityAllCount = 0;

    private static int algorithmCount = 0;
    private static int algorithmCountProgress = 2;
    private static int badSignalCount = 0;

    private static int demoIndex = 0;

    private long delayMillis;

    private boolean oldversion;

    private boolean starterTurningOff = false;
    private boolean isInDisconnectstate;

    private static String wavFilePath;
    private static final Object object = new Object();

    PointF[] peaksEN12s = null;
    PointF[] peaks12s = null;

    private static int startSignalProcessingCount = 0;

    public boolean isRestarted = false;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int PERMISSION_REQUEST_CODE = 7;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main_view);
        //Initialize result launcher
        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //Initialize result data
                        Intent intent = result.getData();

                        if (intent != null) {
//                            try {
//                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
//                                        getContentResolver(), intent.getData()
//                                );
//                                image_view.setImageBitmap(bitmap);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                        }

                    }
                }
        );

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                isRestarted = false;
            } else {
                isRestarted = extras.getBoolean("reopened");
            }
        } else {
            isRestarted = false;
        }
        mUserStop = true;
        setContext(this);

        if (ContextCompat.checkSelfPermission(HeadSense.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(HeadSense.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            normalStart();

        } else {

            askPermission();
        }


    }

    private void askPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                normalStart();
            } else {
                Toast.makeText(HeadSense.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void normalStart() {

        if (!oldversion && ValuesOfSettings.getInstance().isEmpty()) {
            openSettings();
            setApplicationFirstLaunch(true);
        }
        starterTurningOff = false;
        initializeView();
        initializeTimer();
        initializeProgressBar();
        initializeListeners();


        mHeadSenseModel = new HeadSenseModel(this, this);
        try {
            mHeadSenseModel.start();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        isRestarted = false;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        Log.d(TAG, "Permission callback called-------");
//        switch (requestCode) {
//            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
//                Map<String, Integer> perms = new HashMap<>();
//                // Initialize the map with both permissions
//                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
//                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
//                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
//                // Fill with actual results from user
//                if (grantResults.length > 0) {
//                    for (int i = 0; i < permissions.length; i++)
//                        perms.put(permissions[i], grantResults[i]);
//                    // Check for both permissions
//                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
//                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
//                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                        Log.d(TAG, "sms & location services permission granted");
//                        // process the normal flow
//                        //else any one or both the permissions are not granted
//                    } else {
//                        Log.d(TAG, "Some permissions are not granted ask again ");
//                    }
//                }
//            }
//        }

//    }

    private void showDialogOK(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permissionsdialog)
                .setPositiveButton(R.string.ok, okListener)
//                .setNegativeButton(R.string.cancel, okListener)
                .create()
                .show();
    }

    private boolean checkAndRequestPermissions() {
        return true;
    }

    public void turnOffSwitcher() {
        starterTurningOff = true;
        starter.setChecked(false);
    }


    /**
     * checks OS version
     *
     * @return true if version is less than 4.3
     */
    private boolean checkOSVersion() {
        String OSVersion = android.os.Build.VERSION.RELEASE;
        if (OSVersion.length() > 3)
            OSVersion = OSVersion.substring(0, 3);
        if (Float.valueOf(OSVersion) < 6.0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle(R.string.alertdialogtitle);
            alertDialogBuilder
                    .setMessage(R.string.alertdialogtext)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                            System.exit(0);
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return true;
        }
        return false;
    }

    public void startTotalTimerTask() {
        synchronized (object) {
            if (total_timer_minutes != 0) {
                evaluateTotalTimeDuration();
            }
            if (!total_timer_started) {
                total_timer_started = true;
                total_timer_stoped = false;

                if (total_timerTask != null) {
                    total_timerTask.setStarted(true);

                    total_timer = new Total_Timer();
                    total_timer.getTimer().schedule(total_timerTask, 0, 60 * 1000);
                }
            }
        }
    }

    private void evaluateTotalTimeDuration() {
        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.ENGLISH);
        String inputString1 = stop_date;
        String inputString2 = myFormat.format(new Date());
        long diff = 0;
        try {
            Date date1 = myFormat.parse(inputString1);
            Date date2 = myFormat.parse(inputString2);
            diff = date2.getTime() - date1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long result = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
        if (result >= 1) {
            total_timer_minutes = 0;
            total_timer_hours = 0;
        }
    }

    public void stopTotalTimertask() {
        synchronized (object) {
            if (!total_timer_stoped) {
                total_timer_stoped = true;
                total_timer_started = false;
                if (total_timerTask != null) {
                    setTotal_timer_minutes(getTotal_timer_minutes() - 1);
                    total_timerTask.stopHandler();
                }
                if (total_timerTask != null && total_timerTask.cancel()) {
                    total_timerTask = null;
                }
                if (total_timer != null) {
                    total_timer.getTimer().cancel();
                    total_timer = null;
                }
            }
        }
    }

    public void startTimerTask() {
        synchronized (object) {
            if (!timer_started) {
                timer_started = true;
                timer_stoped = false;

                if (timerTask != null) {
                    timerTask.setStarted(true);
                    timerTask.setTimerMinutes(0);
                    timerTask.setTimerHours(0);

                    timer = new Timer_();
                    timer.getTimer().schedule(timerTask, 0, 60 * 1000);
                }
            }
        }
    }

    public void stoptimertask() {
        if (!timer_stoped) {
            timer_stoped = true;
            timer_started = false;
            if (timerTask != null) {
                timerTask.setTimerMinutes(timerTask.getTimerMinutes() - 1);
                timerTask.stopHandler();
            }
            if (timerTask != null && timerTask.cancel()) {
                timerTask = null;
            }
            if (timer != null) {
                timer.getTimer().cancel();
                timer = null;
            }
        }
    }


    public void updateView(String time) {
        mTimeFromStartValue.setText(time);
    }


    private void initializeProgressBar() {
        progressBarHandler = new Handler();
        progressBarDuration = 0;
        progressBar.setProgress(0);
        progressBarRunnable = new Runnable() {

            @Override
            public void run() {
                int seconds = ValuesOfSettings.getInstance().getSoundLength() + 1;
                long speed;
                double epsilon = 0.00000001;
//                long speed = (long)(seconds*15.625) - 10*(seconds-6)/6;
                switch (ValuesOfSettings.getInstance().getSoundLength()) {
                    case 10:
                        speed = (long) (seconds * 9);
                        break;
                    case 15:
                        speed = (long) (seconds * 10);
                        break;
                    case 20:
                        speed = (long) (seconds * 10);
                        break;
                    default:
                        speed = (long) (seconds * 10);
                        break;
                }
                if (progressBarDuration < 100 && !stopedAlgo && !mUserStop) {
                    progressBarDuration = progressBarDuration + 1;
                    progressBar.setProgress(progressBarDuration);
                    progressBarPercent.setText(progressBarDuration + "%");
                    progressBarHandler.postDelayed(progressBarRunnable, speed);
                } else {
                    progressBarDuration = 0;
                }
            }
        };
    }

    private void initializeTimer() {
        timerTask = new Timer_task(HeadSense.this);
        synchronized (object) {
            total_timerTask = new Total_Timer_task(HeadSense.this);
        }
    }

    public void enableSettingsAndClear() {
        mSettingsButton.setClickable(true);
        mIcpClearButton.setClickable(true);
    }

    public void disableSettingsAndClear() {
        mSettingsButton.setClickable(false);
        mIcpClearButton.setClickable(false);
    }

    /**
     * inits listeners
     */
    private void initializeListeners() {

        starter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cancelProgressBar();
                if (isChecked) {
                    startSignalProcessingCount = 0;
                    timeToStart();
                } else {
                    if (!starterTurningOff)
                        timeToStop();
                }
            }
        });


        mIcpClearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                TextView title = new TextView(context);
                title.setText(R.string.clear_title);
                title.setGravity(Gravity.CENTER);
                title.setTextSize(getResources().getDimension(R.dimen.clear_dialog_title) / getResources().getDisplayMetrics().density);
                alertDialogBuilder.setCustomTitle(title);
                alertDialogBuilder.setMessage(R.string.clear_body)
                        .setCancelable(false).setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                        .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                clearIcpGraph();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                textView.setTextSize(getResources().getDimension(R.dimen.clear_dialog_body) / getResources().getDisplayMetrics().density);
                textView.setGravity(Gravity.CENTER);

                int h = (int) getResources().getDimension(R.dimen.clear_dialog_height);
                int w = (int) getResources().getDimension(R.dimen.clear_dialog_width);

                alertDialog.getWindow().setLayout(w, h);
            }
        });

        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openSettings();
            }
        });
    }


    public void cancelProgressBar() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarPercent.setText("0%");
                progressBar.setProgress(0);
            }
        });

        progressBarDuration = 0;
        progressBarHandler.removeCallbacks(progressBarRunnable);
    }

    public void timeToStart() {
        start();
    }

    public void timeToStop() {
        stop();
    }

    /**
     * inits view components
     */
    private void initializeView() {

        oldversion = checkOSVersion();
        starter = (Switch) findViewById(R.id.starter);
        int colorOn = 0xFF009E4E;
        int colorOff = 0xFFE31B23;

        int colorDisabled = 0xFF333333;
        StateListDrawable thumbStates = new StateListDrawable();
        final int[] intValueOn = new int[]{android.R.attr.state_checked};
        thumbStates.addState(intValueOn, new ColorDrawable(colorOn));
        final int[] intValueDis = new int[]{-android.R.attr.state_enabled};
        thumbStates.addState(intValueDis, new ColorDrawable(colorDisabled));
        final int[] intValueOff = new int[]{};
        thumbStates.addState(intValueOff, new ColorDrawable(colorOff)); // this one has to come last
        // starter.setThumbDrawable(thumbStates);
        mSentSignalWaveform = (LinearLayout) findViewById(R.id.sentSignalWaveform);
//        mSentSignalWaveform1 = (LinearLayout) findViewById(R.id.sentSignalWaveform1);
//        mSentSignalWaveform3 = (LinearLayout) findViewById(R.id.sentSignalWaveform3);
        mIcpClearButton = (ImageView) findViewById(R.id.icpClearButton);
        mSettingsButton = (ImageView) findViewById(R.id.settingsButton);
        mTimeFromStartValue = (TextView) findViewById(R.id.tfsValue);
//        mTotalTime = (TextView) findViewById(R.id.totalTime);
        mDemo = (TextView) findViewById(R.id.demo_txtview);
        mDemo.setText(ValuesOfSettings.getInstance().isDemo() ? getResources().getString(R.string.demo_string) : "");
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(100);
        progressBar.setScaleY(2f);
        progressBarPercent = (TextView) findViewById(R.id.pbarpercent);
    }

    public void prepareForRunningStartTaskInBackground() {
        mHeadSenseModel.setModelFromPreferences();
        if (stopedAlgo) {
            setStopedAlgo(false);
        }
        ValuesOfSettings.getInstance().initSharedPreferences();
        try {
            FileOutputStream fosICP = openFileOutput(ICP_VALUES_FILE_NAME, Context.MODE_PRIVATE);
            fosICP.close();
            FileOutputStream fosRP = openFileOutput(RP_VALUES_FILE_NAME, Context.MODE_PRIVATE);
            fosRP.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StartTask startTask = new StartTask(false);
        startTask.execute("start task");
    }

    /**
     * open settings
     */
    public void openSettings() {
        createShortCut();
        mUserStop = true;
        Intent intent = new Intent(this, SettingsView.class);
        startActivityForResult(intent, HeadSense_Activities.SETTINGS_ACTIVITY.ordinal());
    }

    /**
     * creates shortcut
     */
    public void createShortCut() {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());

        Intent removeIntent = new Intent();
        removeIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        removeIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Shortcut Name");
        removeIntent.putExtra("duplicate", false);
        removeIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");

        this.sendBroadcast(removeIntent);

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "surik_pulm");
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.headsense_final_icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        this.sendBroadcast(intent);
    }

    @SuppressWarnings("deprecation")
    public void headSenseModelStatusChanged(HeadSenseModelEnum status) {
        switch (status) {
            case ALGORITHM_START: {
                startAlgWithSpecifiedData();
                break;
            }
            case ALGORITHM_HAS_FINISHED_WITH_SUCCESS: {
                processSuccessedAlgorithmResult();
                continuesMode();
                break;
            }
            case ALGORITHM_HAS_FINISHED_WITH_FAILURE: {
                processSuccessedAlgorithmResult();
                continuesMode();
                break;
            }
            case AUDIO_RECORD_STOPPED: {
                if (!stopedAlgo) {
                    processSavingAudioDataBuffer();
                }
                break;
            }
            case AUDIO_RECORD_FAILED: {

                break;
            }
            case FILES_WERE_SAVED: {

                break;
            }
            default:
                break;
        }
    }

//    private void finishAllSession() {
//        Log.e("snp", "finishAllSession");
//        if (ValuesOfSettings.getInstance().isContinuesMode()) {
//                continuesMode();
//        }
//    }

    private void stopQualityPercent() {
        qualityGoodCount = 0;
        qualityAllCount = 0;

    }

    private void processSavingAudioDataBuffer() {
        HeadSense.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHeadSenseModel.saveAudioDataBuffer(null);
            }
        });
    }

    private void updateUI_AlgorithmIsGood() {
        HeadSense.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (finishCount == 0 || (!stopedAlgo && !mUserStop)) {
                    try {
                        processDataAndDisplayGraphs(peaks12s, peaksEN12s);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                algorithmFinished = true;

//                if (finishCount == 0) {
//                    finishAllSession();
//                    dataForAlgorithm = new ArrayList<short[]>();
//                    algorithmIndex = 0;
//                }
            }
        });
    }

    private void updateUI_AlgorithmIsBad() {
        HeadSense.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (badSignalCount == 2 || ValuesOfSettings.getInstance().getSoundLength() > 12) {
                        badSignalCount = 0;
                    } else {
                        badSignalCount++;
                    }
                    if (finishCount == 0 || (!stopedAlgo && !mUserStop)) {
                        try {
                            processDataAndDisplayGraphs(peaks12s, peaksEN12s);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                algorithmFinished = true;
            }
        });
    }

    private void processSuccessedAlgorithmResult() {
        mAlg = mHeadSenseModel.getAlgorithm();
        peaksEN12s = mAlg.getPeaksEN12s();
        peaks12s = mAlg.getPeaks12s();
        if (!HeadSense.isBadAlg()) {
            updateUI_AlgorithmIsGood();
        } else {
            if (!isInDisconnectstate) {
                updateUI_AlgorithmIsBad();
            }
//            dataForAlgorithm = new ArrayList<short[]>();
//            algorithmIndex = 0;
        }
    }

    private void continuesMode() {
        if (!ValuesOfSettings.getInstance().isContinuesMode() || mUserStop) {
            mUserStop = false;
            return;
        }
        enableStop();
        delayMillis = ValuesOfSettings.getInstance().getRecording_interval();
        HeadSense.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                mHandler = new Handler();
                if (delayMillis < 3) {
                    mHandler.postDelayed(startRecordAgain, delayMillis * 1200);
                } else {
                    mHandler.postDelayed(startRecordAgain, delayMillis * 1000);
                }

            }
        });
        Log.i(TAG, "Time between records started. timestamp - " + System.currentTimeMillis());

    }

    private TimerTask startRecordAgain = new TimerTask() {
        public void run() {
            if (!mUserStop) {
                System.out.println("Starting again timestamp - " + System.currentTimeMillis());
                StartTask startTask = new StartTask(true);
                startTask.execute("start task");
            }
        }
    };
    private TimerTask stopRecordAndPlay = new TimerTask() {
        public void run() {
            mHeadSenseModel.stopPlayingAndRecording();
        }
    };

    /**
     * clears all values on screen
     */
    public void clearIcpGraph() {
        try {
            progressBar.setProgress(0);
            progressBarDuration = 0;
            progressBarPercent.setText(progressBarDuration + "%");
            mSentSignalWaveform.removeAllViews();
            mSentSignalWaveform.addView(GraphUtilities.generateGraph(this, null, 0, 0, 0, 0, 1, 0, 0, "", ValuesOfSettings.getInstance().getGraph_type() == Graph_Types.ACOUSTIC));
            mTimeFromStartValue.setText("");
            setTotal_timer_minutes(0);
            setTotal_timer_hours(0);
            ValuesOfSettings.getInstance().initSharedPreferences();
            FileOutputStream fosICP = openFileOutput(ICP_VALUES_FILE_NAME, Context.MODE_PRIVATE);
            fosICP.close();
            FileOutputStream fosRP = openFileOutput(RP_VALUES_FILE_NAME, Context.MODE_PRIVATE);
            fosRP.close();
            progressBar.setProgress(0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't clear the icp values");
        }
    }

    public void startSignalProcessing(boolean f) {
        setStartSignalProcessingCount(getStartSignalProcessingCount() + 1);
        setFinishCount(ValuesOfSettings.getInstance().getSoundLength() / 12);
        setBadAlgCount(0);
        isInDisconnectstate = false;
        startProgressBar();
        startAlgorithm();

        if (!f) {
            startTimerTask();
            startTotalTimerTask();
        }
    }

    public void startProcessingAlgorithm(boolean fromCountiniouseMode) {

        if (!fromCountiniouseMode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTimeFromStartValue.setText("00:00");
                }
            });
        }

        starterTurningOff = false;
        disableSettingsAndClear();
        startSignalProcessing(fromCountiniouseMode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mUserStop) {
            stop();
            Intent myIntent = new Intent(context, HeadSense.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            myIntent.putExtra("reopened", true);
            finish();
            try {
                Thread.sleep(500);
                context.startActivity(myIntent);
            } catch (Exception e) {
                Log.e(TAG, "Error while trying to take the snapshot." + e);
            }
        }

    }

    /**
     * starts algorithm
     */
    public void startAlgorithm() {
        enableStop();

        mUserStop = false;
        mHeadSenseModel.startPlayingAndRecording();
    }


    /**
     * enable stop button
     */
    private void enableStop() {
        if (ValuesOfSettings.getInstance().isContinuesMode()) {
            startRecordAgain.cancel();
            stopRecordAndPlay.cancel();
        }
    }

    public void start() {
        if (ValuesOfSettings.getInstance().isDemo()) {
            setAlgorithmCountProgress(1);
        } else {
            setAlgorithmCountProgress(2);
        }
        setDemoIndex(0);
//        setDataForAlgorithm(new ArrayList<short[]>());
//        setAlgorithmIndex(0);
        HeadSense.setAlgorithmFinished(true);
        timerTask = new Timer_task(HeadSense.this);
        synchronized (object) {
            total_timerTask = new Total_Timer_task(HeadSense.this);
        }
        prepareForRunningStartTaskInBackground();
        isInDisconnectstate = false;
    }


    /**
     * stops process
     */
    public void stop() {
        setDemoIndex(0);
        setAlgorithmCount(0);
        setStopedAlgo(true);
        mUserStop = true;
        starterTurningOff = false;
        setFinishCount(0);
//        setDataForAlgorithm(new ArrayList<short[]>());
//        setAlgorithmIndex(0);
        HeadSense.setWavFilePath("");
        enableSettingsAndClear();
        stoptimertask();
        stopTotalTimertask();
        cancelProgressBar();
        if (mHandler != null && startRecordAgain != null) {
            startRecordAgain.cancel();
            mHandler.removeCallbacks(startRecordAgain);
        }
        enableStart();
        SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.ENGLISH);
        setStop_date(myFormat.format(new Date()));

        stopQualityPercent();
    }

    /**
     * enables start
     */
    private void enableStart() {
        HeadSense.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                turnOffSwitcher();
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //when returning from the snapshot window
        starterTurningOff = false;
        //when returning from the settings window
        if (resultCode == HeadSense_Activities.SETTINGS_ACTIVITY.ordinal()) {
            clearIcpGraph();
            mDemo.setText(ValuesOfSettings.getInstance().isDemo() ? getResources().getString(R.string.demo_string) : "");
            mHeadSenseModel.setModelFromPreferences();
            finishCount = ValuesOfSettings.getInstance().getSoundLength() / 12;
            if (ValuesOfSettings.getInstance().isDemo()) {
                try {
                    copyFileFromAsset("sound.wav", "demo/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void processDataAndDisplayGraphs(PointF[] sig14, PointF[] sig1) throws IOException {
        if (sig14 != null && sig1 != null && sig14.length != 0) {
            int lineColor = 0;
            float scale = Resources.getSystem().getDisplayMetrics().density;
            mSentSignalWaveform.removeAllViews();
//            mSentSignalWaveform1.removeAllViews();
//            mSentSignalWaveform3.removeAllViews();
            lineColor = getResources().getColor(R.color.SentSignalWaveformLineColor);
            if (ValuesOfSettings.getInstance().getGraph_type() == Graph_Types.ICP) {
                mSentSignalWaveform.addView(GraphUtilities.generateGraph(this,
                        GraphUtilities.convertPointFArrayToGraphDataSeries(sig1), lineColor, (int) (SENT_SIGNAL_LINE_STROKE * scale), lineColor, (int) (scale * 0.5f),
                        1, GraphUtilities.pointFArrayXMinValue(sig1)
                        , GraphUtilities.pointFArrayXMaxValue(sig1), "", false));
            } else {
                mSentSignalWaveform.addView(GraphUtilities.generateGraph(this,
                        GraphUtilities.convertPointFArrayToGraphDataSeries(sig14), lineColor, (int) (ACOUSTIC_GRAPH_LINE_STROKE * scale), lineColor, (int) (scale * 1f),
                        1, GraphUtilities.pointFArrayXMinValue(sig14)
                        , GraphUtilities.pointFArrayXMaxValue(sig14), "", false));
            }

        }
    }

    private void startAlgWithSpecifiedData() {
        StartAlgorithm startAlgorithm = new StartAlgorithm();
        startAlgorithm.execute("");
    }

    private class StartAlgorithm extends AsyncTask<String, Void, String> {

        private StartAlgorithm() {
            super();
        }

        @Override
        protected String doInBackground(String... urls) {

            mHeadSenseModel.startAlgorithmWithSpecifiedData();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void copyFileFromAssetToSD(String fileName, String path) throws IOException {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = this.getAssets().open(fileName);
            outputStream = new FileOutputStream(path + fileName);
            copyFile(inputStream, outputStream);
            inputStream.close();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyFileFromAsset(String fileName, String folderName) throws IOException {
        String directoryStr = ValuesOfSettings.getInstance().getBaseDirectory();

        if (!new File(directoryStr + folderName).exists()) {
            File folder = new File(directoryStr + folderName);
            if (!folder.isDirectory() && !folder.mkdir()) {
                folder.mkdirs();
                Log.e(TAG, "The directory: " + folder
                        + " could not be created, Base directory is still: "
                        + folder);
            }
        }
        File directory = new File(directoryStr + folderName);
        File[] contents = directory.listFiles();
        if (!new File(directoryStr + folderName + fileName).exists()) {
            copyFileFromAssetToSD(fileName, directoryStr + folderName);
        }
    }


    /**
     * starter task
     */
    private class StartTask extends AsyncTask<String, Void, String> {

        private boolean fromCountiniouseMode;

        private StartTask(boolean f) {
            super();
            fromCountiniouseMode = f;
        }

        @Override
        protected String doInBackground(String... urls) {
            startProcessingAlgorithm(fromCountiniouseMode);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public static boolean isStopedAlgo() {
        return stopedAlgo;
    }

    public static void setStopedAlgo(boolean stopedAlgo) {
        HeadSense.stopedAlgo = stopedAlgo;
    }

    public static boolean isBadAlg() {
        return badAlg;
    }

    public static void setBadAlg(boolean badAlg) {
        HeadSense.badAlg = badAlg;
    }

    public static boolean isApplicationFirstLaunch() {
        return applicationFirstLaunch;
    }

    public static void setApplicationFirstLaunch(boolean applicationFirstLaunch) {
        HeadSense.applicationFirstLaunch = applicationFirstLaunch;
    }

    public void startProgressBar() {
        progressBarHandler.post(progressBarRunnable);
    }

    public static int getQualityAllCount() {
        return qualityAllCount;
    }

    public static void setQualityAllCount(int qualityAllCount) {
        HeadSense.qualityAllCount = qualityAllCount;
    }

    public static int getQualityGoodCount() {
        return qualityGoodCount;
    }

    public static void setQualityGoodCount(int qualityGoodCount) {
        HeadSense.qualityGoodCount = qualityGoodCount;
    }

    public static int getAlgorithmCount() {
        return algorithmCount;
    }

    public static void setAlgorithmCount(int algorithmCount) {
        HeadSense.algorithmCount = algorithmCount;
    }

    public static int getTotal_timer_minutes() {
        return total_timer_minutes;
    }

    public static void setTotal_timer_minutes(int total_timer_minutes) {
        HeadSense.total_timer_minutes = total_timer_minutes;
    }

    public static int getTotal_timer_hours() {
        return total_timer_hours;
    }

    public static void setTotal_timer_hours(int total_timer_hours) {
        HeadSense.total_timer_hours = total_timer_hours;
    }

    public static int getFinishCount() {
        return finishCount;
    }

    public static void setFinishCount(int finishCount) {
        HeadSense.finishCount = finishCount;
    }

//    public static List<short[]> getDataForAlgorithm() {
//        return dataForAlgorithm;
//    }
//
//    public static void setDataForAlgorithm(List<short[]> dataForAlgorithm) {
//        HeadSense.dataForAlgorithm = dataForAlgorithm;
//    }

    public static boolean isAlgorithmFinished() {
        return algorithmFinished;
    }

    public static void setAlgorithmFinished(boolean algorithmFinished) {
        HeadSense.algorithmFinished = algorithmFinished;
    }

//    public static int getAlgorithmIndex() {
//        return algorithmIndex;
//    }
//
//    public static void setAlgorithmIndex(int algorithmIndex) {
//        HeadSense.algorithmIndex = algorithmIndex;
//    }

    public static String getWavFilePath() {
        return wavFilePath;
    }

    public static void setWavFilePath(String wavFilePath) {
        HeadSense.wavFilePath = wavFilePath;
    }

    public static int getDemoIndex() {
        return demoIndex;
    }

    public static void setDemoIndex(int demoIndex) {
        HeadSense.demoIndex = demoIndex;
    }

    public static int getBadAlgCount() {
        return badAlgCount;
    }

    public static void setBadAlgCount(int badAlgCount) {
        HeadSense.badAlgCount = badAlgCount;
    }

    public static void setContext(Context context) {
        HeadSense.context = context;
    }

    public static int getStartSignalProcessingCount() {
        return startSignalProcessingCount;
    }

    public static void setStartSignalProcessingCount(int startSignalProcessingCount) {
        HeadSense.startSignalProcessingCount = startSignalProcessingCount;
    }

    public static void setAlgorithmCountProgress(int algorithmCountProgress) {
        HeadSense.algorithmCountProgress = algorithmCountProgress;
    }

    public static void setStop_date(String stop_date) {
        HeadSense.stop_date = stop_date;
    }
}