package me.k4m1s.voicemeetercontroller;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

public class SocketCommunication extends Thread {

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

    private long lastSyncTime = System.currentTimeMillis();

    public void run() {
        Socket socket = MainActivity.getInstance().getConnectionManager().getSocket();

        new Thread(()->{
            try {
                while(true){
                    Thread.sleep(1000);
                    this.sendData("ping");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            String line = "";
            BufferedReader in = null;
            String[] splited;
            String[] values;
            int progressL;
            int progressR;
            try{
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("in failed");
                System.exit(-1);
            }
            if (in == null) {
                System.out.println("in is null");
                System.exit(-1);
                return;
            }
            while(!socket.isClosed()) {
                try {
                    line = in.readLine();
                } catch (IOException e) {
                    System.out.println("Read failed");
                    System.exit(-1);
                }
                if (line.equals("INITIALSTART")) {
                    lastSyncTime = System.currentTimeMillis();
                    try {
                        line = in.readLine();
                    } catch (IOException e) {
                        System.out.println("Read failed");
                        System.exit(-1);
                    }
                    while(!line.equals("INITIALEND")) {
                        lastSyncTime = System.currentTimeMillis();
                        handleReceivedData(line);
                        try {
                            line = in.readLine();
                        } catch (IOException e) {
                            System.out.println("Read failed");
                            System.exit(-1);
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if(line.startsWith("V")) {
                    line = line.substring(1);
                    splited = line.split(Pattern.quote(":"));
                    values = splited[1].split(Pattern.quote(","));
                    progressL = (int) Helper.map(Math.round(Float.parseFloat(values[0])), -80, 12, 0, 100);
                    progressR = (int) Helper.map(Math.round(Float.parseFloat(values[1])), -80, 12, 0, 100);
                    setProgressValue(splited[0],Integer.parseInt(splited[0].substring(2)), progressL, progressR);
                } else if (line.equals("pong")) {
                    // pong
                } else {
                    lastSyncTime = System.currentTimeMillis();
                    handleReceivedDataSync(line);
                }
            }
        }).start();

        try {
            OutputStream outputStream = socket.getOutputStream();
            String data;
            while (true) {
                data = queue.take();
                outputStream.write(data.getBytes());
                outputStream.write('\n');
                outputStream.flush();
            }
        } catch(SocketException e) {
            MainActivity.getInstance().changeScreenToStartScreen("Lost connection to the server.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void setProgressValue(String type, int index, int progressL, int progressR) {
        MainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (type.startsWith("BS")) {
                    MainActivity.getInstance().bus[index].setVUValue(progressL, progressR);
                } else if (type.startsWith("HS")) {
                    MainActivity.getInstance().hardwareStrips[index].setVUValue(progressL, progressR);
                } else if (type.startsWith("SS")) {
                    MainActivity.getInstance().softwareStrips[index].setVUValue(progressL, progressR);
                }
            }
        });
    }

    private void handleReceivedData(String data) {
        String[] splited = data.split(Pattern.quote(":"));
        String type = splited[0].substring(0, 2);
        int id = Integer.parseInt(splited[0].substring(2));
        switch(type) {
            case "HS":
                MainActivity.getInstance().hardwareStrips[id].setData(splited[1]);
                break;
            case "SS":
                MainActivity.getInstance().softwareStrips[id].setData(splited[1]);
                break;
            case "BS":
                MainActivity.getInstance().bus[id].setData(splited[1]);
                break;
        }
    }
    String[] splited;
    String type;
    int id;
    private void handleReceivedDataSync(String data) {
        MainActivity.getInstance().runOnUiThread(() -> {
            splited = data.split(Pattern.quote(":"));
            type = splited[0].substring(0, 2);
            id = Integer.parseInt(splited[0].substring(2));
            switch(type) {
                case "HS":
                    MainActivity.getInstance().hardwareStrips[id].setDataSync(splited[1]);
                    break;
                case "SS":
                    MainActivity.getInstance().softwareStrips[id].setDataSync(splited[1]);
                    break;
                case "BS":
                    MainActivity.getInstance().bus[id].setDataSync(splited[1]);
                    break;
            }
        });
    }

    public void sendData(String data) {
        if (System.currentTimeMillis() - lastSyncTime < 1000) {
            return;
        }
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
