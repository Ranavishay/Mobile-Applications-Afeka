package com.example.cargame1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import android.media.MediaPlayer;



import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class GameActivity extends AppCompatActivity implements SensorEventListener{

    private static final int ROWS = 5;
    private static final int COLS = 11;
    private final int rowCarIndex=8;
    private int carIndex = 2;
    private int carCrash = 0;
    private int countUpdateUI = 0;

    private static int DELAY = 1000;
    final Handler handler = new Handler();
    private ImageView[][] matrixImages = new ImageView[ROWS][COLS];
    private int[][] matrixThingsOnScreen = new int[ROWS][COLS];
    private final int carObstacle=1;
    private final int coin=2;


    private ImageView main_IMG_heart1;
    private ImageView main_IMG_heart2;
    private ImageView main_IMG_heart3;
    private Button leftButton;
    private Button rightButton;

    private TextView main_score;
    private int score = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, DELAY);
            try {
                updateUI();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private static List<ScoreDate> tenScoresList = new ArrayList<>();
    private MediaPlayer mediaCrash;


    private void updateUI() throws InterruptedException {
        countUpdateUI++;
        for (int i = matrixImages.length-1; i >= 0; i--) {
            for (int j = matrixImages[i].length - 1; j > 0; j--) {
                if (matrixImages[i][j - 1].getDrawable() != null && matrixImages[i][j - 1].getDrawable() != matrixImages[carIndex][rowCarIndex].getDrawable()) {
                    matrixImages[i][j - 1].setImageResource(0);
                    if (matrixThingsOnScreen[i][j - 1] == carObstacle) {
                        if(matrixImages[i][j]==matrixImages[carIndex][rowCarIndex]){
                            crash();
                            matrixImages[i][j].setImageResource(R.drawable.car);
                        }
                        else {
                            matrixImages[i][j].setImageResource(R.drawable.car_obstacle);
                            matrixThingsOnScreen[i][j] = carObstacle;
                        }
                        matrixThingsOnScreen[i][j-1] =0;
                    }
                    if (matrixThingsOnScreen[i][j - 1] == coin) {
                        if(matrixImages[i][j]==matrixImages[carIndex][rowCarIndex]){
                            matrixImages[i][j].setImageResource(R.drawable.car);
                            score+=10;
                        }
                        else {
                            matrixImages[i][j].setImageResource(R.drawable.coin);
                            matrixThingsOnScreen[i][j] = coin;
                        }
                        matrixThingsOnScreen[i][j-1] =0;
                    }
                }
            }
        }
        for (int i = 0; i < matrixImages.length; i++) {
            if (matrixImages[i][matrixImages[0].length-1].getDrawable()!=null){
                matrixImages[i][matrixImages[0].length-1].setImageResource(0);
                matrixThingsOnScreen[i][matrixImages[0].length-1] =0;
            }
        }
        if (countUpdateUI % 3 == 0) {
            Random random = new Random();
            int randomNumber = random.nextInt(5);
            matrixImages[randomNumber][0].setImageResource(R.drawable.car_obstacle);
            matrixThingsOnScreen[randomNumber][0] = carObstacle;
        }
        if (countUpdateUI % 7 == 0) {
            Random random = new Random();
            int randomNumber = random.nextInt(5);
            matrixImages[randomNumber][0].setImageResource(R.drawable.coin);
            matrixThingsOnScreen[randomNumber][0] = coin;
        }
        score+=1;
        main_score.setText("Score: " + score);
    }

    private void crash() {
        if (mediaCrash != null) {
            mediaCrash.start();
        }
        vibrate();
        toast("Get Crash!!");
        carCrash++;
        if (carCrash==1) {
            main_IMG_heart3.setVisibility(View.INVISIBLE);
        }else {
            if (carCrash == 2) {
                main_IMG_heart2.setVisibility(View.INVISIBLE);
            }else {
                if (carCrash == 3) {
                    main_IMG_heart1.setVisibility(View.INVISIBLE);
                    //TimeUnit.SECONDS.sleep(1);
                    saveScoreAndDate(score);
                    carCrash = 0;
                    score=0;
                    carIndex = 2;
                    main_IMG_heart1.setVisibility(View.VISIBLE);
                    main_IMG_heart2.setVisibility(View.VISIBLE);
                    main_IMG_heart3.setVisibility(View.VISIBLE);
                    for (int i=0; i < matrixThingsOnScreen.length; i++) {
                        for (int j = 0; j < matrixThingsOnScreen[i].length; j++) {
                            matrixThingsOnScreen[i][j] = 0;
                            matrixImages[i][j].setImageResource(0);
                        }
                    }
                    //matrixImages[carIndex][rowCarIndex].setImageResource(R.drawable.car);
                    countUpdateUI = 0;
                    handler.removeCallbacks(runnable);
                    toast("Game Over");
                    finish();
                }
            }
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        if (intent != null) {
            String mode = intent.getStringExtra("Mode");
            DELAY = intent.getIntExtra("Delay",DELAY);
            if (mode != null) {
                if (mode.equals("buttons")) {
                    leftButton = findViewById(R.id.buttonLeft);
                    rightButton = findViewById(R.id.buttonRight);
                    leftButton.setOnClickListener(view -> moveLeft());
                    rightButton.setOnClickListener(view -> moveRight());
                } else if (mode.equals("tilt")) {
                    leftButton = findViewById(R.id.buttonLeft);
                    rightButton = findViewById(R.id.buttonRight);
                    leftButton.setVisibility(View.INVISIBLE);
                    rightButton.setVisibility(View.INVISIBLE);
                }
            }
        }
        getMatrixFromViews();

        findViews();

        //leftButton.setOnClickListener(view -> moveLeft());
        //rightButton.setOnClickListener(view -> moveRight());
        handler.postDelayed(runnable, DELAY);

        loadScoresSharedPreferences();

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);


        mediaCrash = MediaPlayer.create(this, R.raw.crash_sound);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float y = event.values[1];
            if (y > 2.0f) {
                moveRight();
            } else if (y < -2.0f) {
                moveLeft();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        if (mediaCrash != null) {
            mediaCrash.release();
            mediaCrash = null;
        }
    }



    private void loadScoresSharedPreferences() {
        {
            SharedPreferences preferences = getSharedPreferences("Scores", MODE_PRIVATE);
            String topScoresJson = preferences.getString("ScoresList", "");

            Gson gson = new Gson();
            Type type = new TypeToken<List<ScoreDate>>(){}.getType();
            tenScoresList = gson.fromJson(topScoresJson, type);

            if (tenScoresList == null) {
                tenScoresList = new ArrayList<>();
            }
        }
    }
    private void findViews() {
        //leftButton = findViewById(R.id.buttonLeft);
        //rightButton = findViewById(R.id.buttonRight);
        main_IMG_heart1 =findViewById(R.id.main_IMG_heart1);
        main_IMG_heart2=findViewById(R.id.main_IMG_heart2);
        main_IMG_heart3=findViewById(R.id.main_IMG_heart3);
        main_score = findViewById(R.id.score);
    }

    private void moveRight() {
        if (carIndex != 4){
            carIndex++;
            matrixImages[carIndex-1][rowCarIndex].setImageResource(0);
            if (matrixImages[carIndex][rowCarIndex].getDrawable()!=null) {
                if (matrixThingsOnScreen[carIndex][rowCarIndex]==carObstacle) {
                    crash();
                }
                if (matrixThingsOnScreen[carIndex][rowCarIndex]==coin) {
                    score+=10;
                }

            }
            matrixImages[carIndex][rowCarIndex].setImageResource(R.drawable.car);
        }
    }

    private void moveLeft() {
        if (carIndex != 0){
            carIndex--;
            matrixImages[carIndex+1][rowCarIndex].setImageResource(0);
            if (matrixImages[carIndex][rowCarIndex].getDrawable()!=null) {
                if (matrixThingsOnScreen[carIndex][rowCarIndex]==carObstacle) {
                    crash();
                }
                if (matrixThingsOnScreen[carIndex][rowCarIndex]==coin) {
                    score+=10;
                }
            }
            matrixImages[carIndex][rowCarIndex].setImageResource(R.drawable.car);
        }
    }

    private void getMatrixFromViews() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                //matrixImages[i][j] = findViewById(R.id.index1_1 + i*COLS + j);
                @SuppressLint("DiscouragedApi") int resourceId = getResources().getIdentifier("index" + (i + 1) + "_" + (j + 1), "id", getPackageName());
                matrixImages[i][j] = findViewById(resourceId);
            }
        }
    }


    private void saveScoreAndDate(int newScore) {
        Date currentDate = new Date();
        ScoreDate newScoreDate = new ScoreDate(newScore, currentDate);
        loadScoresSharedPreferences();
        int index = 0;
        for (int i = 0; i < tenScoresList.size(); i++) {
            if (newScore > tenScoresList.get(i).getScore()) {
                index = i;
                break;
            }
        }
        tenScoresList.add(index, newScoreDate);
        //if (index<=10) {
        // TO DO  -  new score

        //}
        if (tenScoresList.size() > 10) {
            tenScoresList.remove(10);
        }
        updateScoresSharedPreferences();
    }

    private void updateScoresSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("Scores", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String tenScoresJson = gson.toJson(tenScoresList);
        editor.putString("ScoresList", tenScoresJson);
        editor.apply();
    }

    public static List<ScoreDate> getTenScoresList() {
        return tenScoresList;
    }
}
