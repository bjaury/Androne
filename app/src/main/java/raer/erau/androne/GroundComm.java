package raer.erau.androne;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by Shawn_ on 3/26/2016.
 */
public class GroundComm extends AsyncTask<Void,Void,Void> {

    byte header = 0x0A;
    Socket comm=null;

    @Override
    protected Void doInBackground(Void... params) {
        Log.d("Ground Communication","Start");
        do {
            try {
                comm = new Socket(MainActivity.IP, MainActivity.port);
            } catch (IOException e) {}
        }while (comm==null);
        Log.d("Ground Communication","Connected");
        return null;
    }

    void sendMessage(int id,int sequence,byte[] size,byte[] data){
        int checkSum=10+data.length;
        byte[] msg = new byte[checkSum];
        msg[0]=header;
        msg[1]=(byte)160;
        msg[2]=(byte)id;
        msg[3]=(byte)sequence;
        msg[4]=size[0];
        msg[5]=size[1];
        for(int i=0;i<data.length;i++){
            msg[i+6]=data[i];
        }
        byte[] temp =ByteBuffer.allocate(4).putInt(checkSum).array();
        for(int i=0;i<temp.length;i++){
            msg[msg.length-4+i]=temp[i];
        }
        try {
            OutputStream out = comm.getOutputStream();
            out.write(msg);
        }catch (IOException e){}




    }
}
