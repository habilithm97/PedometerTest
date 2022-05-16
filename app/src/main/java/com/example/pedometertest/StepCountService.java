package com.example.pedometertest;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

// 백그라운드 실행을 위해 서비스 클래스 별도 생성
// SensorEventListener : 센서 값이 변경되거나 센서 이벤트의 정보를 제공하는 센서 이벤트 객체를 생성
public class StepCountService extends Service implements SensorEventListener {

    int cnt = StepCount.Step;
    long lastTime;
    float x, y, z, speed, lastX, lastY, lastZ;
    int SHAKE_THRESHOLD = 800; // 민감도 -> 작을 수록 느린 스피드에서도 감지

    SensorManager manager;
    Sensor accelerometerSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        manager = (SensorManager)getSystemService(SENSOR_SERVICE); // 센서 서비스의 인스턴스를 만들 수 있음
        accelerometerSensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); // 특정 센서의 인스턴스를 만들 수 있음(센서의 기능을 결정할 수 있음)
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(accelerometerSensor != null) { // 센서가 널이 아니면
            manager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME); // 센서 관리자로 센서 딜레이 설정 후 등록
        }
        return START_STICKY; // 서비스가 강제 종료 되었을 경우 시스템이 Intent 값을 null로 초기화 시켜서 서비스를 재시작함
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (manager != null) { // 센서 매니저가 널이 아니면
            // SensorEventListener 해제
            manager.unregisterListener(this);
            StepCount.Step = 0;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) { // 여기서 센서를 동작 시킴(흔듬 감지)
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { // 가속도 센서
            // 최근 측정한 시간과 현재 시간을 비교하여 흔듬을 감지함
            long currentTime = System.currentTimeMillis(); // 현재 시간 정보를 가져와서 할당
            long gabOfTime = (currentTime - lastTime); // 현재 시간 - 최근 측정한 시간

            if (gabOfTime > 100) { // 현재 시간과 최근 측정한 시간을 비교하여 0.1초 이상되었을 때 흔듬을 감지함
                lastTime = currentTime; // 현재 시간을 최근 측정한 시간으로
            }
            x = event.values[SensorManager.DATA_X];
            y = event.values[SensorManager.DATA_Y];
            z = event.values[SensorManager.DATA_Z];
            speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;

            if(speed > SHAKE_THRESHOLD) { // Intent에 서비스를 담아서 MainActivity의 방송 수신자 클래스로 보냄
                Intent intent = new Intent("com.example.pedometertest");

                StepCount.Step = cnt++;

                String msg = StepCount.Step / 2 + "";
                intent.putExtra("stepCountService", msg);
                sendBroadcast(intent);
            }
            lastX = event.values[SensorManager.DATA_X];
            lastY = event.values[SensorManager.DATA_Y];
            lastZ = event.values[SensorManager.DATA_Z];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }
}