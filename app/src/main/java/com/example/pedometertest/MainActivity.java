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
*센서
 -가속 센서(Accelerometer) : 다양한 기준의 축(Axe)을 따라 기기가 얼마만큼의 속도로 움직이는지 측정함(가속도 감지, 외부의 충격량과 방향 감지)
  ->중력 정보와 선형 가속 정보가 같이 계산되므로 가장 자주 사용되는 센서 중의 하나임
  ->단말을 테이블 위에 놓아두었을 때(가만히 있을 때)에는 가속 센서의 값은 +9.81이 됨
 -자이로스코프 센서(Gyroscope) : 가속 센서보다 더 많은 축을 기준으로 시간에 따라 최전하는 정보까지 확인 가능함(회전 정보 감지, 다양한 축을 따른 회전각 감지)

*센서 매니저
 -각 센서 정보를 포함함 -> 장치에 내장되어 있는 센서의 리스트를 제공함
 -센서로부터 정보를 받을 때는 SensorEvent 객체로 전달되므로 이 객체를 처리하여 각 센서의 값을 바로 확인 가능함
 -시스템 서비스 객체이므로 객체를 생성하지 않고 객체의 참조값을 얻어야함
 -getDefaultSensor() 메서드가 가장 많이 사용되는데 주어진 타입에 대한 디폴트 센서를 얻을 수 있음
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView tv;

    int cnt = 0; // 걸음수 초기값

    long lastTime; // 최근 측정한 시간

    float x, y, z, speed, lastX, lastY, lastZ;
    int SHAKE_THRESHOLD = 800; // 민감도 -> 작을 수록 느린 스피드에서도 감지를 함
    int DATA_X = SensorManager.DATA_X;
    int DATA_Y = SensorManager.DATA_Y;
    int DATA_Z = SensorManager.DATA_Z;

    SensorManager manager;
    Sensor stepCount;

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

        manager = (SensorManager)getSystemService(SENSOR_SERVICE); // 시스템 서비스 객체이므로 객체를 생성하지 않고 객체의 참조값을 얻어야함
        stepCount = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // 가속도 센서(중력 가속도)
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) { // 여기서 센서를 동작 시킴(흔듬 감지)
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { // 가속도 센서
            // 최근 측정한 시간과 현재 시간을 비교하여 흔듬을 감지함
            long currentTime = System.currentTimeMillis(); // 현재 시간 정보를 가져와서 할당
            long gabOfTime = (currentTime - lastTime); // 현재 시간 - 최근 측정한 시간

            if (gabOfTime > 100) { // 현재 시간과 최근 측정한 시간을 비교하여 0.1초 이상되었을 때 흔듬을 감지함
                lastTime = currentTime; // 현재 시간을 최근 측정한 시간으로

                x = sensorEvent.values[SensorManager.DATA_X];
                y = sensorEvent.values[SensorManager.DATA_Y];
                z = sensorEvent.values[SensorManager.DATA_Z];
                speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    tv.setText((++cnt) + " 걸음");
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