package raer.erau.androne;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Shawn_ on 2/20/2016.
 */
public class MultiTCPTask extends AsyncTask<File,Void,Void> {
    private File picture;
    private Socket socket;

    @Override
    protected Void doInBackground(File... param){
        picture = param[0];
        String ip = MainActivity.IP;
        int port = MainActivity.port;
        /*while(!isPortOpen(port)){
            if(port>= MainActivity.maxport)
                port=MainActivity.port;
            else
                port++;
        }*/
        Log.d("TCP", "Start TCP");
        try {
            socket = new Socket(ip, port);
            InputStream in = new FileInputStream(picture);
            OutputStream out = socket.getOutputStream();
            byte[] buffer = new byte[(int)picture.length()];
            int temp;
            while ((temp=in.read(buffer)) !=-1){
                out.write(buffer,0,temp);
            }
            out.flush();
            socket.close();
        }catch (IOException e){
            Log.d("Error",e.toString());
        }

        return null;
    }
    private boolean isPortOpen(int port){
        ServerSocket s=null;
        try{
            s = new ServerSocket(port);
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }finally {
            if (s!= null)
                try{
                    s.close();
                }catch (IOException e){}
        }
        return true;
    }

}
