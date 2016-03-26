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
 * Created by Shawn_ on 1/28/2016.
 */
public class TextTCPTask extends AsyncTask<File,Void,Void> {

    private File text;
    private Socket socket;

    @Override
    protected Void doInBackground(File... param){
        text = param[0];
        String ip = MainActivity.IP;
        int port = MainActivity.port+1;
        Log.d("TCP","Start TCP");
        try {
            socket = new Socket(ip, port);
            InputStream in = new FileInputStream(text);
            OutputStream out = socket.getOutputStream();
            byte[] buffer = new byte[(int)text.length()];
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
