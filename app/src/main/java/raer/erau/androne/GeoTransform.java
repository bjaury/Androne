package raer.erau.androne;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Shawn_ on 3/4/2016.
 */
public class GeoTransform {
    final int r_earth = 6371000;
    double hfov=69,vfov=49.6;
    final double pi = Math.PI;
    String name,line;

    public void transform(File file){
        Log.d("Geo","Start Geo");
        Log.d("Byte",(byte)1010000+"");
        Mat m = Imgcodecs.imread(file.getAbsolutePath());
        name = file.getName().substring(0,file.getName().indexOf("."));
        String[] param=new String[6];
        File geo = new File(file.getParent()+"/geo"+name+".txt");
        if(geo.exists()&& !geo.isDirectory()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(geo));
                line =br.readLine();
                //Log.d("GeoTran",line);
                line=line.split("\t")[1];
                param=line.split(",");
            }catch (FileNotFoundException e){}catch (IOException e){}
        }
        else {
            param = new String[6];
            for (int i = 0; i < param.length; i++)
                param[i] = "0";
        }
        //for(int i=0;i<param.length;i++)
        //   Log.d("GeoTran",param[i]);
        Mat end = flattenImage(m, Double.valueOf(param[3]), -Double.valueOf(param[4]), Double.valueOf(param[5]));
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory() + "/SUAS/Processed/" + name + ".jpg", end);
        new TCPTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new File(Environment.getExternalStorageDirectory() + "/SUAS/Processed/" + name + ".jpg"));
        Log.d("Geo", "Img save");
        double[][] points = getPoints(Double.valueOf(param[0]),Double.valueOf(param[1]),Double.valueOf(param[2])*.3048,Double.valueOf(param[3]),Double.valueOf(param[4]),Double.valueOf(param[5]));
        String cornerCord = "";
        for(double[] point:points){
            for(double cord:point){
                cornerCord=cornerCord+" " +cord;
            }
            cornerCord=cornerCord+"\n";
        }


        File cord = new File(Environment.getExternalStorageDirectory() + "/SUAS/Processed", name + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(cord);
            fos.write(cornerCord.getBytes());
            fos.close();
        } catch (IOException e) {}

        sendTime(file);
        Log.d("Geo", "End Geo");
    }

    Mat flattenImage(Mat img, double psy,double theta, double phi){
        int xSize = img.cols();
        int ySize = img.rows();

        psy = pi*2-(psy+pi/2);

        double[][] R = new double[][]{{Math.cos(theta)*Math.cos(psy),Math.cos(phi)*Math.sin(psy)+Math.sin(phi)*Math.sin(theta)*Math.cos(psy),Math.sin(phi)*Math.sin(psy)-Math.cos(phi)*Math.sin(theta)*Math.cos(psy)},
                {-Math.cos(theta)*Math.sin(psy),Math.cos(phi)*Math.cos(psy)-Math.sin(phi)*Math.sin(theta)*Math.sin(psy),Math.sin(phi)*Math.cos(psy)+Math.cos(phi)*Math.sin(theta)*Math.sin(psy)},
                {Math.sin(theta),-Math.sin(phi)*Math.cos(theta),Math.cos(phi)*Math.cos(theta)}};
        //for(int x=0;x<R.length;x++)
        //    for (int i=0;i<R[0].length;i++)
        //        Log.d("Point calc "+x+" "+i,R[x][i]+"");
        double[][] p = {{-.5,.5,1},
                        {.5,.5,1},
                        {.5,-.5,1},
                        {-.5,-.5,1}};

        double[][] newPoints = new double[4][3];

        for(int i=0;i<4;i++){
            double[] temp = new double[]{R[0][0]*p[i][0]+R[0][1]*p[i][1]+R[0][2]*p[i][2],
                                        R[1][0]*p[i][0]+R[1][1]*p[i][1]+R[1][2]*p[i][2],
                                        R[2][0]*p[i][0]+R[2][1]*p[i][1]+R[2][2]*p[i][2]};
            temp[0]=temp[0]/temp[2];
            temp[1]=temp[1]/temp[2];
            temp[2]=temp[2]/temp[2];
            newPoints[i]=temp;
        }
        //for(int x=0;x<4;x++)
        //    for(int i=0;i<3;i++)
        //        Log.d("NewPoints "+x+" "+i,newPoints[x][i]+"");

        float xOffset = Float.POSITIVE_INFINITY;
        float yOffset = Float.POSITIVE_INFINITY;
        for(int i=0;i<newPoints.length;i++){
            if (newPoints[i][0]<xOffset)
                xOffset=(float)newPoints[i][0];
            if (newPoints[i][1]<yOffset)
                yOffset=(float)newPoints[i][1];
        }
        //Log.d("yOff point ",yOffset+"");
        //Log.d("xOff point ",xOffset+"");
        double[][] dst = new double[4][2];
        float xMax = Float.NEGATIVE_INFINITY;
        float yMax = Float.NEGATIVE_INFINITY;
        for(int i=0;i<newPoints.length;i++){
            dst[i][0]=(newPoints[i][0]-xOffset)*xSize;
            dst[i][1]=(newPoints[i][1]-yOffset)*ySize;
            if(dst[i][0]>xMax)
                xMax = (float)dst[i][0];
            if(dst[i][1]>yMax)
                yMax = (float)dst[i][1];
        }
        //Log.d("yMax point",yMax+"");
        //Log.d("xMax point", xMax + "");

        //for(int x=0;x<4;x++)
        //    for(int i=0;i<2;i++)
        //        Log.d("New points "+x+" "+i,dst[x][i]+"");
        MatOfPoint2f source = new MatOfPoint2f(new org.opencv.core.Point(0,ySize),
                                                new org.opencv.core.Point(xSize,ySize),
                                                new org.opencv.core.Point(xSize,0),
                                                new org.opencv.core.Point(0,0));

        MatOfPoint2f temp = new MatOfPoint2f(new org.opencv.core.Point(dst[0]),
                                                new org.opencv.core.Point(dst[1]),
                                                new org.opencv.core.Point(dst[2]),
                                                new org.opencv.core.Point(dst[3]));

        Mat m = Imgproc.getPerspectiveTransform(source, temp);
        Mat imgNew = new Mat();
        Imgproc.warpPerspective(img,imgNew,m, new Size(xMax,yMax),Imgproc.INTER_NEAREST);
        img.release();
        source.release();
        temp.release();

        return imgNew;
    }

    double[][] getPoints(double lat,double lon,double alt,double roll,double pitch,double yaw){
        double[][] corners = getCorners();



        double[][] points=new double[4][2];
        for(int i=0;i<corners.length;i++){
            corners[i]=rotateVector(corners[i],roll,pitch,yaw);
            points[i]=projectToGround(corners[i],lat,lon,alt);
        }

        return points;
    }

    double[] projectToGround(double[] vec,double lat,double lon,double alt){

        vec[0]=(vec[0]/vec[2])*alt;
        vec[1]=(vec[1]/vec[2])*alt;
        vec[2]=(vec[2]/vec[2])*alt;

        double[] newGeo =  new double[]{lat+(vec[1]/r_earth)*(180/pi),lon+(vec[0]/r_earth)*(180/pi)/(Math.cos(lat*pi/180))};

        return newGeo;
    }

    double[] rotateVector(double[] vec,double phi,double theta,double psi){
        double[][] R = new double[][]{{Math.cos(phi),0.0,Math.sin(phi)},
                                        {0.0,1.0,0.0},
                                        {-Math.sin(phi),0.0,Math.cos(phi)}};

        double[][] P = new double[][]{{1.0,0.0,0.0},
                                        {0.0,Math.cos(theta),Math.sin(theta)},
                                        {0.0,-Math.sin(theta),Math.cos(theta)}};

        double[][] Y = new double[][]{{Math.cos(psi),Math.sin(psi),0.0},
                                        {-Math.sin(psi),Math.cos(psi),0.0},
                                        {0.0,0.0,1.0}};

        double[] a= new double[]{R[0][0]*vec[0]+R[0][1]*vec[1]+R[0][2]*vec[2],
                                R[1][0]*vec[0]+R[1][1]*vec[1]+R[1][2]*vec[2],
                                R[2][0]*vec[0]+R[2][1]*vec[1]+R[2][2]*vec[2]};

        double[] b= new double[]{P[0][0]*a[0]+P[0][1]*a[1]+P[0][2]*a[2],
                                P[1][0]*a[0]+P[1][1]*a[1]+P[1][2]*a[2],
                                P[2][0]*a[0]+P[2][1]*a[1]+P[2][2]*a[2]};

        double[] c= new double[]{Y[0][0]*b[0]+Y[0][1]*b[1]+Y[0][2]*b[2],
                                Y[1][0]*b[0]+Y[1][1]*b[1]+Y[1][2]*b[2],
                                Y[2][0]*b[0]+Y[2][1]*b[1]+Y[2][2]*b[2]};

        return c;
    }


    double[][] getCorners(){

        hfov =hfov*Math.PI/180;
        vfov =vfov*Math.PI/180;

        double[][] points = new double[4][3];

        //Upper left
        points[0][0]= Math.tan(vfov/2);
        points[0][1]= Math.tan(hfov/2);
        points[0][2]= 1;

        //Lower left
        points[1][0]= -Math.tan(vfov/2);
        points[1][1]= Math.tan(hfov/2);
        points[1][2]= 1;

        //Lower right
        points[2][0]= -Math.tan(vfov/2);
        points[2][1]= -Math.tan(hfov/2);
        points[2][2]= 1;

        //Upper right
        points[3][0]= Math.tan(vfov/2);
        points[3][1]= -Math.tan(hfov/2);
        points[3][2]= 1;

        return points;
    }



    void sendTime(File f){
        new TextTCPTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,f);
    }
}
