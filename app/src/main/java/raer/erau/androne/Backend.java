package raer.erau.androne;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Shawn_ on 2/27/2016.
 */
public class Backend extends AsyncTask<Void,Void,Void> {
    protected Void doInBackground(Void... param){
        Log.d("Backend","Setup");
        String input;
        ServerSocket socket = null;
        do {
            try {
                socket = new ServerSocket(8000);
            } catch (IOException e) {}
        }while(socket==null);
        Log.d("Backend","Server Setup");
        while(true) {
            try {
                Log.d("Backend","Loop Start");
                Socket connectionSocket = socket.accept();
                Log.d("Backend","Connceted");
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                input = inFromClient.readLine();
                Log.d("Backend",input);
                input.toLowerCase();
                switch (input){
                    case "test":
                        MainActivity.runCamera();
                        outToClient.writeBytes("Test");
                        break;
                    case "start":
                        MainActivity.takingPictures = true;
                        MainActivity.start();
                        outToClient.writeBytes("Started taking pictures");
                        break;
                    case "set ip":
                        outToClient.writeBytes("What is ground station IP");
                        Socket IP =socket.accept();
                        MainActivity.IP = new BufferedReader(new InputStreamReader(IP.getInputStream())).readLine();
                        new DataOutputStream(IP.getOutputStream()).writeBytes("Ground station IP is set to " + MainActivity.IP);
                        //MainActivity.netAddress.setText("IP Address:" + MainActivity.IP);
                        break;
                    case "set port":
                        outToClient.writeBytes("What is ground station port");
                        Socket port =socket.accept();
                        MainActivity.port = Integer.parseInt(new BufferedReader(new InputStreamReader(port.getInputStream())).readLine());
                        new DataOutputStream(port.getOutputStream()).writeBytes("Ground station port is set to " + MainActivity.port);
                        break;
                    case "set picture time":
                        outToClient.writeBytes("What is picture time");
                        Socket time =socket.accept();
                        MainActivity.picTime = Integer.getInteger(new BufferedReader(new InputStreamReader(time.getInputStream())).readLine());
                        //MainActivity.time.setText("Picture interval:" + MainActivity.picTime);
                        new DataOutputStream(time.getOutputStream()).writeBytes("Picture time is set to " + MainActivity.picTime);
                        break;
                    case "set focus":
                        outToClient.writeBytes("Use focus?");
                        Socket focus = socket.accept();
                        String response = new BufferedReader(new InputStreamReader(focus.getInputStream())).readLine();
                        if(response.equals("yes"))
                            CameraTask.useFocus = true;
                        else if(response.equals("no"))
                            CameraTask.useFocus = false;
                        new DataOutputStream(focus.getOutputStream()).writeBytes("Use focus set to: "+CameraTask.useFocus);
                        break;
                    case "stop":
                        MainActivity.takingPictures = false;
                        outToClient.writeBytes("Stoped taking pictures");
                        break;
                    default:
                        outToClient.writeBytes("Invalid Input");
                }
            }catch (IOException e){}
        }
    }
}
