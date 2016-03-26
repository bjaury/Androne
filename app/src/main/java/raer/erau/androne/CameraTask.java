package raer.erau.androne;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;


/**
 * Created by Shawn_ on 1/22/2016.
 */
public class CameraTask extends AsyncTask<Void,Void,Void> {
    private Camera myCamera;
    private int pictureNumber;
    private String time;
    public static boolean useFocus=false;

    public CameraTask(int number){
        this.pictureNumber = number;
    }

    public boolean isCameraOpen() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }


    @Override
    protected Void doInBackground(Void ... param){
        Log.d("Camera", "Start");
        while(isCameraOpen()){}

        myCamera = Camera.open();
        Camera.Parameters parameters = myCamera.getParameters();
        //parameters.setRotation((new Camera.CameraInfo().orientation + 360) % 360);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        myCamera.setParameters(parameters);
        try{
            myCamera.setPreviewTexture(new SurfaceTexture(10));
        } catch (IOException e) {
            e.printStackTrace();
        }
        myCamera.startPreview();
        if(useFocus)
            myCamera.autoFocus(focusCallback);
        myCamera.takePicture(null, null, mPicture);


        /*
        time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()).toString();

        File file = new File(Environment.getExternalStorageDirectory() + "/SUAS", pictureNumber + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(time.getBytes());
            fos.close();
        } catch (IOException e) {}
        sendTime(file);*/

        return null;
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            myCamera.release();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            File file = new File(Environment.getExternalStorageDirectory() + "/SUAS",pictureNumber +".jpg");
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Mat img = Imgcodecs.imread(file.getAbsolutePath());
            Mat imgSmall = new Mat();

            Imgproc.resize(img, imgSmall, new Size(img.width() * .5, img.height() * .5));
            Mat imgKey= new Mat();
            Mat center = new Mat();
            Mat labels = new Mat();


            File newImg = new File(Environment.getExternalStorageDirectory() + "/SUAS/" + pictureNumber + ".jpg");


            Imgcodecs.imwrite(newImg.getAbsolutePath(), imgSmall);
            //Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/SUAS/Processed/key " + pictureNumber + ".jpg",imgKey);
            img.release();
            imgKey.release();
            imgSmall.release();
            center.release();
            labels.release();




            new GeoTransform().transform(newImg);

            new TCPTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,newImg);
        }
    };

    private Camera.AutoFocusCallback focusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
        }
    };

}
