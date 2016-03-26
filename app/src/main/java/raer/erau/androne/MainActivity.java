package raer.erau.androne;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends Activity implements SurfaceHolder.Callback{
    Button net;
    static int picNumber,picTime=5,port=8888,groundComPort=23000;
    static String IP="";
    Switch takePicture,cameraPreview;
    static boolean takingPictures;
    static Handler handler = new Handler();
    static TextView netAddress,time;
    Camera myCamera;
    SurfaceHolder surfaceHolder;
    SurfaceView cameraView;

    BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Backend().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        //new GroundComm().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        picNumber=1;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("IP Address");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                IP = input.getText().toString();
                netAddress = (TextView) findViewById(R.id.ipAdress);
                netAddress.setText("IP Address:" + IP);
            }
        });
        builder.show();


        time = (TextView)findViewById(R.id.picTime);
        time.setText("Picture interval:" + picTime);


        findViewById(R.id.cameraSurface).setVisibility(View.INVISIBLE);

        takePicture = (Switch) findViewById(R.id.pictureSwitch);
        takePicture.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cameraPreview.setVisibility(View.INVISIBLE);
                    takingPictures = true;
                    start();
                } else {
                    cameraPreview.setVisibility(View.VISIBLE);
                    takingPictures = false;
                    handler.removeCallbacks(takePictures);
                }
            }
        });


        cameraPreview = (Switch) findViewById(R.id.preview);
        cameraPreview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cameraView = (SurfaceView) findViewById(R.id.cameraSurface);
                    surfaceHolder = cameraView.getHolder();
                    surfaceHolder.addCallback(MainActivity.this);
                    net.setVisibility(View.INVISIBLE);
                    cameraView.setVisibility(View.VISIBLE);
                    takePicture.setVisibility(View.INVISIBLE);
                } else {
                    cameraView.setVisibility(View.INVISIBLE);
                    surfaceHolder.removeCallback(MainActivity.this);
                    surfaceHolder = null;
                    cameraView = null;
                    net.setVisibility(View.VISIBLE);
                    takePicture.setVisibility(View.VISIBLE);
                }
            }
        });



        net = (Button)findViewById(R.id.restNet);
        net.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CameraTask(picNumber++).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        Button test = (Button)findViewById(R.id.geoTest);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GeoTransform().transform(new File(Environment.getExternalStorageDirectory()+"/SUAS","test.jpg"));
            }
        });
    }


    public static void start(){
        handler.postDelayed(takePictures, picTime * 1000);
    }
    public static void runCamera(){
        new CameraTask(picNumber++).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static Runnable takePictures = new Runnable() {
        @Override
        public void run() {
            new CameraTask(picNumber++).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            if(takingPictures)
                start();
        }
    };

    /*private Runnable running = new Runnable() {
        @Override
        public void run() {
            while(true) {
                while(takingPictures) {

                }
            }
        }
    };*/



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        myCamera = Camera.open();
        myCamera.setDisplayOrientation(90);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            myCamera.stopPreview();
        }
        catch (Exception e){}
        try{
            myCamera.setPreviewDisplay(surfaceHolder);
            myCamera.startPreview();
        }
        catch (Exception e){}
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        if(myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
        }
        myCamera = null;
    }


    @Override
    protected void onResume() {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mBaseLoaderCallback);
        new File(Environment.getExternalStorageDirectory()+"/SUAS").mkdir();
        new File(Environment.getExternalStorageDirectory()+"/SUAS/Processed").mkdir();
        super.onResume();
    }
}
