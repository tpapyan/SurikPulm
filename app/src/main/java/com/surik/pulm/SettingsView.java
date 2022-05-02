package com.surik.pulm;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.surik.pulm.fileSystem.FileCache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Settings view provides all settings section, saves them to shared preferances
 */
@SuppressLint("SdCardPath")
public class SettingsView extends AppCompatActivity {

    private TextView ibrText;
    private EditText mTimeBetweenRecordsText;
    private EditText mBaseDirectoryText;
    private CheckBox mContinuesMode;
    private Button graphType;
    private Button soundLength;

    private CheckBox demo;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
        }

        //Initialize result launcher
        ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //Initialize result data
                        Intent intent = result.getData();

                        if(intent!=null){
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

        initializeView();
        HeadSense.setTotal_timer_minutes(0);
        HeadSense.setTotal_timer_hours(0);

        //if the shared preferences list is empty, we need to initialize it.
        if (HeadSense.isApplicationFirstLaunch()) {
            HeadSense.setApplicationFirstLaunch(false);
        }
        updateScreen();
        initSystemInfo();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
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
//                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
////                        // shouldShowRequestPermissionRationale will return true
//                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
////                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
////                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
////                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
////                            showDialogOK(new DialogInterface.OnClickListener() {
////                                @Override
////                                public void onClick(DialogInterface dialog, int which) {
////                                    switch (which) {
////                                        case DialogInterface.BUTTON_POSITIVE:
////                                            checkAndRequestPermissions();
////                                            break;
////                                        case DialogInterface.BUTTON_NEGATIVE:
//////                                                    System.exit(0);
////                                            // proceed with logic by disabling the related features or quit the app.
////                                            break;
////                                    }
////                                }
////                            });
////                        }
////                        //permission is denied (and never ask again is  checked)
////                        //shouldShowRequestPermissionRationale will return false
////                        else {
//////                            ValuesOfSettings.getInstance().clearApplicationData();
////                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show();
////                            //                            //proceed with logic by disabling the related features or quit the app.
////                        }
//                    }
//                }
//            }
//        }
//
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
        List<String> listPermissionsNeeded = new ArrayList<>();
//        int permission_WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int permission_RECORD_AUDIO = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
//        int permission_CAMERA = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        if (permission_WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//        if (permission_RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
//        }
//        if (permission_CAMERA != PackageManager.PERMISSION_GRANTED) {
//            listPermissionsNeeded.add(Manifest.permission.CAMERA);
//        }
//        if (!listPermissionsNeeded.isEmpty()) {
//            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
//            return false;
//        }
        return true;
    }

    /**
     * initializing view components
     */
    private void initializeView() {
        mTimeBetweenRecordsText = (EditText) findViewById(R.id.timeBetRecordsText);
        mBaseDirectoryText = (EditText) findViewById(R.id.baseDirectoryText);
        mContinuesMode = (CheckBox) findViewById(R.id.continuesModeCheckBox);
        soundLength = (Button) findViewById(R.id.soundLengthButton);

        demo = (CheckBox) findViewById(R.id.demo_mode);
        ibrText = (TextView) findViewById(R.id.ibrText);
    }

    public void openSoundLengthDialog(View v) {
        final Dialog d = new Dialog(SettingsView.this, R.style.spinnerStyle);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(R.layout.spinner_list_view_ethnicity);
        ListView list = (ListView) d.findViewById(R.id.list);
        String[] values = getResources().getStringArray(R.array.soundLength);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, values);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                soundLength.setText(((TextView) view).getText().toString());
                d.dismiss();
            }
        });

        d.show();

    }

    public void disableIBR() {
        mTimeBetweenRecordsText.setEnabled(false);
        ibrText.setTextColor(Color.GRAY);
    }

    public void enableIBR() {
        mTimeBetweenRecordsText.setEnabled(true);
        ibrText.setTextColor(Color.WHITE);
    }

    /**
     * init system information
     */

    private void initSystemInfo() {

        StringBuilder builder = new StringBuilder();
        builder.append("android : ").append(Build.VERSION.RELEASE);

        Field[] fields = Build.VERSION_CODES.class.getFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            int fieldValue = -1;

            try {
                fieldValue = field.getInt(new Object());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            if (fieldValue == Build.VERSION.SDK_INT) {
                builder.append(",").append(fieldName).append(",");
                builder.append("sdk=").append(fieldValue);
            }
        }

    }

    /**
     * delete cached data
     *
     * @param v
     */
    public void deleteSharedP(View v) {
        ValuesOfSettings.getInstance().initDefaultValues();
        ValuesOfSettings.getInstance().initSharedPreferences();
        updateScreen();
    }


    public void enableContinuesMode(View v) {
        if (mContinuesMode.isChecked()) {
            enableIBR();
        } else {
            disableIBR();
        }
    }

    /**
     * sets model preferances
     */
    public void setModelFromPreferences() {
        FileCache.getInstance().setBaseDirectoryMain(ValuesOfSettings.getInstance().getBaseDirectory());
        FileCache.getInstance().setBaseSignalsDirectory(ValuesOfSettings.getInstance().getBaseDirectory() + "Signals/");
    }

    /**
     * applies all changes
     *
     * @param v
     */
    public void apply(View v) {
        if (validation()) {
            updatePreferences();
            setModelFromPreferences();
            hideSoftKeyboard();
            Intent resultIntent = new Intent();
            setResult(HeadSense_Activities.SETTINGS_ACTIVITY.ordinal(), resultIntent);
            finish();
        }
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    getCurrentFocus().getWindowToken(), 0);
        }

    }

    private boolean validation() {
        boolean noErrors = true;

        if (mTimeBetweenRecordsText.getText().toString().length() == 0 ||
                Integer.parseInt(mTimeBetweenRecordsText.getText().toString()) < 0 || Integer.parseInt(mTimeBetweenRecordsText.getText().toString()) > 3600) {

            noErrors = false;
        }

        return noErrors;

    }



    /**
     * updates screen values
     */
    private void updateScreen() {
        mTimeBetweenRecordsText.setText(Integer.toString(ValuesOfSettings.getInstance().getRecording_interval()));
        mBaseDirectoryText.setText(ValuesOfSettings.getInstance().getBaseDirectory().substring(12, ValuesOfSettings.getInstance().getBaseDirectory().length()));
        mContinuesMode.setChecked(ValuesOfSettings.getInstance().isContinuesMode());
        String[] graph_types = getResources().getStringArray(R.array.graph);


        boolean locdemo = ValuesOfSettings.getInstance().isDemo();
        if (locdemo) {
            demo.setChecked(true);
        } else {
            demo.setChecked(false);
        }

        if (mContinuesMode.isChecked()) {
            enableIBR();
        } else {
            disableIBR();
        }

        mTimeBetweenRecordsText.setText(Integer.toString(ValuesOfSettings.getInstance().getRecording_interval()));
        soundLength.setText(Integer.toString(ValuesOfSettings.getInstance().getSoundLength()));

    }

    /**
     * updates cached data
     */
    private void updatePreferences() {

        ValuesOfSettings.getInstance().setRecording_interval(mTimeBetweenRecordsText.getText().toString().length() != 0 ? Integer.parseInt(mTimeBetweenRecordsText.getText().toString()) : 2);

        ValuesOfSettings.getInstance().setSoundLength(Integer.parseInt(soundLength.getText().toString()));

        ValuesOfSettings.getInstance().setDemo(demo.isChecked());
        ValuesOfSettings.getInstance().initSharedPreferences();

    }

}