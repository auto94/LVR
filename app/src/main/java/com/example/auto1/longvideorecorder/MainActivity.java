
        package com.example.auto1.longvideorecorder;

        import android.app.Dialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.graphics.Paint;
        import android.graphics.Typeface;
        import android.os.BatteryManager;
        import android.os.Environment;
        import android.os.StatFs;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.InputFilter;
        import android.text.Spanned;
        import android.text.TextWatcher;
        import android.view.KeyEvent;
        import android.view.View;
        import android.view.ViewGroup;
        import android.view.inputmethod.EditorInfo;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.widget.RelativeLayout;
        import android.widget.SeekBar;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.ToggleButton;
        import java.text.DecimalFormat;
        import java.text.DecimalFormatSymbols;
        import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private boolean isSizeSelected = false;
    private boolean isTimeSelected = false;
    private boolean isBatterySelected = false;
    private boolean isNONESelected = false;
    private ExpandOrCollapse mAnimationManager;
    private RelativeLayout relLayoutSize;
    private ToggleButton limitButtonSize;
    private ToggleButton noLimitToggleButton;

    int selectedFps;
    int selectedResolution = 0;
    int maxVideoFileSize = 0;
    int minAllowedBattery = 0;
    int maxHours = 0;
    int maxMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedFps = 0;
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        noLimitToggleButton = findViewById(R.id.noLimitButton);
        //Code for the limits of videos:
        TextView limitTV = findViewById(R.id.limitByTV);
        final Typeface fontLimit = Typeface.createFromAsset(getAssets(), "fonts/Heebo-Regular.ttf");
        limitTV.setTextSize(18);
        limitTV.setTypeface(fontLimit);

        relLayoutSize = findViewById(R.id.relativeSize);
        relLayoutSize.setVisibility(View.GONE);
        mAnimationManager = new ExpandOrCollapse();

        limitButtonSize = findViewById(R.id.sizeButton);
        limitButtonSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSizeSelected) {
                    mAnimationManager.collapse(relLayoutSize, 500, 0);
                    isSizeSelected = false;
                }
                else {
                    mAnimationManager.expand(relLayoutSize, 500, 250);
                    isSizeSelected = true;
                    noLimitToggleButton.setChecked(false);
                    isNONESelected = false;
                }
            }
        });

        //Code for size seekbar
        //This next bit is for retrieving the available storage space and then making the bigger one from external and internal the focus for the seekbar max. value
        //free internal storage space
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        long blockSizeInternal = statFs.getBlockSizeLong();
        long availableSizeInternal = statFs.getAvailableBlocksLong()*blockSizeInternal;

        final long maxAvailableSizeInBytes;
        //checks if there is an SD card in phone
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            // We can read and write the media
            // free SD card storage space
            StatFs statFsSD = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long sdAvailSize = statFsSD.getAvailableBlocksLong()*statFsSD.getBlockSizeLong();

            if (sdAvailSize> availableSizeInternal) {
                maxAvailableSizeInBytes = sdAvailSize;
            }
            else {
                maxAvailableSizeInBytes = availableSizeInternal;
            }
        }
        else {
            //
            maxAvailableSizeInBytes = availableSizeInternal;
        }

        final double gigaBytesAvailable = (maxAvailableSizeInBytes/(1024.00*1024.00*1024.00));
        final int approxMegaBytesAvailable = (int)(gigaBytesAvailable*1000);

        final SeekBar sizeSeekBar = findViewById(R.id.sizeSeekBar);
        sizeSeekBar.setMax(approxMegaBytesAvailable);
        final EditText sizeEditText = findViewById(R.id.sizeEditText);


        sizeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    maxVideoFileSize = Math.round(Float.parseFloat(s.toString()) * 1000f);

                    if (maxVideoFileSize/1000.0 > gigaBytesAvailable) {
                        sizeSeekBar.setProgress((int) maxAvailableSizeInBytes/1000);
                        sizeEditText.setText(Double.toString(gigaBytesAvailable));
                    }
                    else {
                        sizeSeekBar.setProgress(maxVideoFileSize);
                        sizeEditText.setSelection(sizeEditText.getText().length());
                    }

                } catch (Exception e) {}
            }
        });

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DecimalFormat df = new DecimalFormat();
                DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                dfs.setGroupingSeparator(',');
                dfs.setDecimalSeparator('.');
                df.setDecimalFormatSymbols(dfs);
                df.setMinimumFractionDigits(3);
                df.setMaximumFractionDigits(3);
                if (fromUser)  {
                    sizeEditText.setText(df.format(progress/1000.0));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sizeEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sizeEditText.clearFocus();
                    return false;
                }
                return false;
            }
        });

        final TextView sizeTitleTV = findViewById(R.id.fileSizeLimit);
        sizeTitleTV.setTypeface(fontLimit, Typeface.BOLD);

        //Code for TIME limit
        final RelativeLayout relLayoutTime = findViewById(R.id.relativeTime);
        final TextView timeLimitTV = findViewById(R.id.timeLimitTV);
        timeLimitTV.setTypeface(fontLimit, Typeface.BOLD);

        final TextView recordForTV = findViewById(R.id.recordingForTV);
        recordForTV.setTypeface(fontLimit);
        recordForTV.setPaintFlags(recordForTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        recordForTV.setText(getResources().getString(R.string.enterTimeAboveString));

        final ToggleButton limitButtonTime = findViewById(R.id.timeButton);
        limitButtonTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimeSelected) {
                    mAnimationManager.collapse(relLayoutTime, 500, 0);
                    isTimeSelected = false;
                }
                else {
                    mAnimationManager.expand(relLayoutTime, 500, 270);
                    isTimeSelected = true;
                    noLimitToggleButton.setChecked(false);
                    isNONESelected = false;
                }
            }
        });



        //Input filter for hours EditText. Only allows numbers 00-23 and max 2 digits
        InputFilter hourFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.length() == 0) {
                    return null;// deleting, keep original editing
                }

                String result = "";
                result += dest.toString().substring(0, dstart);
                result += source.toString().substring(start, end);
                result += dest.toString().substring(dend, dest.length());

                if (result.length() > 2) {
                    return "";// max 2 digits
                }

                boolean allowEdit = true;
                char c;
                if (result.length() > 0) {
                    c = result.charAt(0);
                    allowEdit &= (c >= '0' && c <= '2' && !(Character.isLetter(c)));
                }

                if (result.length() > 1) {
                    c = result.charAt(1);
                    if(result.charAt(0) == '0' || result.charAt(0) == '1')
                        allowEdit &= (c >= '0' && c <= '9');
                    else
                        allowEdit &= (c >= '0' && c <= '3');
                }

                return allowEdit ? null : "";
            }
        };

        //Input filter for minutes EditText, only allows numbers 00-59 and max 2 digits
        InputFilter minuteFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.length() == 0) {
                    return null;// deleting, keep original editing
                }

                String result = "";
                result += dest.toString().substring(0, dstart);
                result += source.toString().substring(start, end);
                result += dest.toString().substring(dend, dest.length());

                if (result.length() > 2) {
                    return "";// max 2 digits
                }

                boolean allowEdit = true;
                char c;
                if (result.length() > 0) {
                    c = result.charAt(0);
                    allowEdit &= (c >= '0' && c <= '5' && !(Character.isLetter(c)));
                }

                if (result.length() > 1) {
                    c = result.charAt(1);
                    allowEdit &= (c >= '0' && c <= '9' && !(Character.isLetter(c)));
                }

                return allowEdit ? null : "";
            }
        };

        final EditText hoursET = findViewById(R.id.timeHourET);
        hoursET.setFilters(new InputFilter[] {hourFilter} );
        hoursET.setHint("hh");

        final EditText minutesET = findViewById(R.id.timeMinuteET);
        minutesET.setFilters(new InputFilter[] {minuteFilter});
        minutesET.setHint("mm");

        hoursET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction (TextView v, int currentAction, KeyEvent event) {
                if (hoursET.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(),"Please enter a valid number, 0-23",Toast.LENGTH_LONG).show();
                    return true;
                }
                maxHours = Integer.parseInt(hoursET.getText().toString());
                if (currentAction == EditorInfo.IME_ACTION_DONE) {
                    minutesET.requestFocus();
                    return true;
                }
                else {
                    return false;
                }
            }
        });

        minutesET.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int currentAction, KeyEvent event) {
                if (minutesET.getText().toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter a valid number, 0-59", Toast.LENGTH_LONG).show();
                    return true;
                }
                maxMinutes = Integer.parseInt(minutesET.getText().toString());
                if (currentAction == EditorInfo.IME_ACTION_DONE) {
                    recordForTV.setText("Record for approximately " + timeUntil(maxHours, maxMinutes));
                    return false;
                }
                else {
                    return false;
                }
            }
        });


//        final EditText editTime = findViewById(R.id.timeET);
//        editTime.setOnEditorActionListener();

        //Code for BATTERY LIMIT
        final RelativeLayout relLayoutBattery = findViewById(R.id.relativeBattery);
        final TextView batteryLimitTV = findViewById(R.id.batteryLimitTV);
        final TextView batteryDisplayTV = findViewById(R.id.batteryDisplayTV);
        final SeekBar batterySeekBar = findViewById(R.id.batterySeekBar);
        batterySeekBar.setMax(getBatteryPercentage(this));
        batteryLimitTV.setTypeface(fontLimit, Typeface.BOLD);
        batteryDisplayTV.setTypeface(fontLimit);
        batteryDisplayTV.setText("0 %");

        final ToggleButton limitButtonBattery = findViewById(R.id.batteryButton);
        limitButtonBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBatterySelected) {
                    mAnimationManager.collapse(relLayoutBattery, 500, 0);
                    isBatterySelected = false;
                }
                else {
                    mAnimationManager.expand(relLayoutBattery, 500, 250);
                    isBatterySelected = true;
                    noLimitToggleButton.setChecked(false);
                    isNONESelected = false;
                }
            }
        });

        batterySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                batteryDisplayTV.setText(progress + "%");
                minAllowedBattery = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Code for NONE limit button
        noLimitToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNONESelected) {
                    limitButtonBattery.setChecked(false);
                    limitButtonTime.setChecked(false);
                    limitButtonSize.setChecked(false);

                    mAnimationManager.collapse(relLayoutBattery, 500, 0);
                    isBatterySelected = false;

                    mAnimationManager.collapse(relLayoutSize, 500, 0);
                    isSizeSelected = false;

                    mAnimationManager.collapse(relLayoutTime, 500, 0);
                    isTimeSelected = false;

                    isNONESelected = true;
                }
                else {
                    isNONESelected = false;
                }
            }
        });

        final TextView selectFPSTV = findViewById(R.id.selectFpsTV);
        selectFPSTV.setTypeface(fontLimit);
        selectFPSTV.setTextSize(18);

        // Code for question mark next to FPS
        // Opens alertDialog in which it is explained what FPS means
        ImageView fpsInfoIV = findViewById(R.id.fpsInfoIV);
        fpsInfoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("FPS Info");
                builder.setIcon(getResources().getDrawable(R.drawable.fps_icon_alertdialog));
                builder.setPositiveButton("HIDE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button btnPositive = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
                    }
                });
                builder.setMessage(getText(R.string.fpsAlertDialogText));
                builder.show();

            }
        });

        //Setup for the FPS ToggleButtons, basicly making it so that only one can be selected at a time.
        final ToggleButton toggle15 = findViewById(R.id.button15);
        final ToggleButton toggle30 = findViewById(R.id.button30);
        final ToggleButton toggle60 = findViewById(R.id.button60);
        final ToggleButton toggle120 = findViewById(R.id.button120);
        final ToggleButton toggleMax = findViewById(R.id.buttonMax);
        toggleMax.setChecked(true);

        toggle15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle30.setChecked(false);
                toggle60.setChecked(false);
                toggle120.setChecked(false);
                toggleMax.setChecked(false);

                // If no FPS button is selected, select the "MAX" as default and set it as checked.
                if (!toggle15.isChecked() && !toggle30.isChecked() && !toggle60.isChecked() && !toggle120.isChecked() && !toggleMax.isChecked()) {
                    toggleMax.setChecked(true);
                }
            }
        });

        toggle30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle15.setChecked(false);
                toggle60.setChecked(false);
                toggle120.setChecked(false);
                toggleMax.setChecked(false);

                // If no FPS button is selected, select the "MAX" as default and set it as checked.
                if (!toggle15.isChecked() && !toggle30.isChecked() && !toggle60.isChecked() && !toggle120.isChecked() && !toggleMax.isChecked()) {
                    toggleMax.setChecked(true);
                }
            }
        });

        toggle60.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle15.setChecked(false);
                toggle30.setChecked(false);
                toggle120.setChecked(false);
                toggleMax.setChecked(false);

                // If no FPS button is selected, select the "MAX" as default and set it as checked.
                if (!toggle15.isChecked() && !toggle30.isChecked() && !toggle60.isChecked() && !toggle120.isChecked() && !toggleMax.isChecked()) {
                    toggleMax.setChecked(true);
                }
            }
        });

        toggle120.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle15.setChecked(false);
                toggle30.setChecked(false);
                toggle60.setChecked(false);
                toggleMax.setChecked(false);

                // If no FPS button is selected, select the "MAX" as default and set it as checked.
                if (!toggle15.isChecked() && !toggle30.isChecked() && !toggle60.isChecked() && !toggle120.isChecked() && !toggleMax.isChecked()) {
                    toggleMax.setChecked(true);
                }
            }
        });

        toggleMax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle15.setChecked(false);
                toggle30.setChecked(false);
                toggle60.setChecked(false);
                toggle120.setChecked(false);

                // If no FPS button is selected, select the "MAX" as default and set it as checked.
                if (!toggle15.isChecked() && !toggle30.isChecked() && !toggle60.isChecked() && !toggle120.isChecked() && !toggleMax.isChecked()) {
                    toggleMax.setChecked(true);
                }
            }
        });

        //Code for resolution select:
        final TextView selectResTV = findViewById(R.id.selectResolutionTV);
        selectResTV.setTypeface(fontLimit);
        selectResTV.setTextSize(18);

        //Spinner code. Defines the look of the spinner together with the xml files.
        //TODO: Make spinner selections change value of int (use case)
        Spinner resolutionSpinner = findViewById(R.id.resolutionSpinner);
        final String[] spinnerItems = new String[]{"2560 × 1440", "1920 × 1080", "1366 × 768", "1280 × 720",  "1024 × 768", "640 × 480"};
        resolutionSpinner.setPrompt("Select Resolution");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.custom_spinner_layout, spinnerItems) {
            public View getView(int position, View convertView,ViewGroup parent) {

                View v = super.getView(position, convertView, parent);

                ((TextView) v).setTextSize(16);
                ((TextView) v).setTypeface(fontLimit);
                //If you want to change the boldness of the selected res, u can add it to typeface here,
                //To make all list elements bold u have to add textstyle=bold in custom_spinner_layout.

                return v;

            }
        };
        //adapter.setDropDownViewResource(R.layout.custom_spinner_layout);
        resolutionSpinner.setAdapter(adapter);

        resolutionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        selectedResolution = 0;
                        break;
                    case 1:
                        selectedResolution = 1;
                        break;
                    case 2:
                        selectedResolution = 2;
                        break;
                    case 3:
                        selectedResolution = 3;
                        break;
                    case 4:
                        selectedResolution = 4;
                        break;
                    case 5:
                        selectedResolution = 5;
                        break;
                    case 6:
                        selectedResolution = 6;
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //disable audio text +  checkbox setup
        final TextView disableAudioTV = findViewById(R.id.disableAudioTV);
        final Typeface checkBoxTextTypeface = Typeface.createFromAsset(getAssets(), "fonts/Heebo-Medium.ttf");
        disableAudioTV.setTypeface(checkBoxTextTypeface);
        disableAudioTV.setTextSize(20);

        final CheckBox disableAudioCB = findViewById(R.id.audioCheckBox);

        // Use front camera text + checkbox setup:
        final TextView useFrontCameraTV = findViewById(R.id.frontCameraTV);
        useFrontCameraTV.setTypeface(checkBoxTextTypeface);
        useFrontCameraTV.setTextSize(20);

        final CheckBox useFrontCameraCB = findViewById(R.id.frontCameraCheckBox);

        // START RECORIDNG button setup
        final Button startButton = findViewById(R.id.startRecordingButton);
        startButton.setText("Start\nRecording");
        startButton.setTextSize(20);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isAudioMuted = false; //boolean for saving state
                String allInfo = "";
                //Code to check if audio should be excluded
                if (disableAudioCB.isChecked()) {
                    //Audio should not be recorded:
                    isAudioMuted = true;
                    allInfo = allInfo + "Audio Recording Disabled ";
                }

                boolean isFrontCameraSelected = false;
                if (useFrontCameraCB.isChecked()) {
                    isFrontCameraSelected = true;
                    allInfo = allInfo + "Front camera selected ";
                }
                else {
                    allInfo = allInfo + "Back camera selected ";
                }

                //Code for which fps button is toggled on/selected. Default is MAX (selectedFPS = 0)
                if (toggle15.isChecked()) {
                    selectedFps = 15;
                    allInfo = allInfo + "FPS: 15 ";
                }
                else if (toggle30.isChecked()) {
                    selectedFps = 30;
                    allInfo = allInfo + "FPS: 30 ";
                }
                else if (toggle60.isChecked()) {
                    selectedFps = 60;
                    allInfo = allInfo + "FPS: 60 ";
                }
                else if (toggle120.isChecked()) {
                    selectedFps = 120;
                    allInfo = allInfo + "FPS: 120 ";
                }
                else {
                    selectedFps = 0;
                    allInfo = allInfo + "FPS: 0 ";
                }

                //SIZE limit check
                if (isSizeSelected) {
                    //0 represents ALL AVAILABLE SPACE
                    allInfo = allInfo + "Max Size: " + maxVideoFileSize/1000.0 + " GB ";
                }
                if (isBatterySelected) {
                    allInfo = allInfo + "Min Battery: " + minAllowedBattery + " % ";
                }

                if (isTimeSelected) {
                    allInfo = allInfo + "Record until: " + maxHours + " : " + maxMinutes + " ";
                }

                allInfo = allInfo + "Selected resolution: " + spinnerItems[selectedResolution];

                Toast testToast = Toast.makeText(getApplicationContext(), allInfo, Toast.LENGTH_LONG);
                testToast.show();
            }
        });

    }

    //function for current battery percentage
    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    /**
     * Calculates the hours and day from currentTime to input Time (hours and minutes)
     * @param maxHours to which hour we record, from user input
     * @param maxMinutes to which minute (in hour) it records, from user input
     * @return String "XX hours and YY minutes." from current time to input time.
     */
    public static String timeUntil (int maxHours, int maxMinutes) {
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
        int currentMinutes = currentTime.get(Calendar.MINUTE);
        int diffHours = maxHours - currentHour;
        int diffMinutes = maxMinutes - currentMinutes;

        if (diffHours <= 0) {
            diffHours = 24 - Math.abs(diffHours);
        }

        if (diffMinutes < 0) {
            diffHours--;
            diffMinutes = 60 - Math.abs(diffMinutes);
        }

        return Math.abs(diffHours) + " hours and " + Math.abs(diffMinutes) + " minutes.";
    }

}