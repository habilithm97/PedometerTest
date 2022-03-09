package com.example.pedometertest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
*센서 매니저 클래스
-장치에 내장되어 있는 센서의 리스트를 제공함
-시스템 서비스의 일종이므로 객체를 생성하지 않고 객체의 참조값을 얻어야함
-getDefaultSensor() 메서드가 가장 많이 사용되는데 주어진 타입에 대한 디폴트 센서를 얻을 수 있음
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView tv;

    int cnt = 0; // 걸음수 초기값

    long lastTime;

    float x, y, z, speed, lastX, lastY, lastZ;
    int SHAKE_THRESHOLD = 800;
    int DATA_X = SensorManager.DATA_X;
    int DATA_Y = SensorManager.DATA_Y;
    int DATA_Z = SensorManager.DATA_Z;

    Sensor stepCount;
    SensorManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.tv);
        Button btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetConfirm();
            }
        });

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCount = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // 가속도 센서(중력 가속도)

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) { // 여기서 센서를 동작 시킴(흔듬 감지)
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 최근 측정한 시간과 현재 시간을 비교하여 흔듬을 감지함
            long currentTime = System.currentTimeMillis(); // 현재 시간 정보를 가져와서 할당
            long gabOfTime = (currentTime - lastTime); // 현재 시간 - 최근 측정한 시간

            if (gabOfTime > 100) { // 0.1초 이상이면
                lastTime = currentTime;

                x = sensorEvent.values[SensorManager.DATA_X];
                y = sensorEvent.values[SensorManager.DATA_Y];
                z = sensorEvent.values[SensorManager.DATA_Z];
                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    tv.setText("" + (++cnt) + " 걸음");
                }
                lastX = sensorEvent.values[DATA_X];
                lastY = sensorEvent.values[DATA_Y];
                lastZ = sensorEvent.values[DATA_Z];
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    @Override
    protected void onStart() {
        super.onStart();

        if(stepCount != null) { // 센서가 널이면
            manager.registerListener(this, stepCount, SensorManager.SENSOR_DELAY_GAME); // 센서 관리자로 센서 딜레이 설정
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(stepCount != null) { // 센서가 널이면
            manager.unregisterListener(this); // 센서 감지 해제
        }
    }

    public void resetConfirm() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("걸음수 초기화");
        builder.setMessage("걸음수를 정말 초기화하시겠습니까 ?");
        builder.setIcon(R.drawable.warning);
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        cnt = 0;
                        tv.setText(cnt + " 걸음");
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }
}