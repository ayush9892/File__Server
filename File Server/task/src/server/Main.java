package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class Main {
    static String fileLoc = "C:\\Users\\AYUSH KUMAR\\IdeaProjects\\File Server\\File Server\\task\\src\\server\\data\\";
    static Map<Integer, String> prevStateMap;
    static int IDNo;


    public static void main(String[] args) {
        System.out.println("Server started!");
        ExecutorService exSer = Executors.newFixedThreadPool(6);
        final int PORT = 23456;
        ObjectState serObj = new ObjectState();

        try (FileInputStream fis = new FileInputStream(fileLoc + "fileState.txt");
             ObjectInputStream ois = new ObjectInputStream(fis))
        {
            ObjectState IdMap = (ObjectState) ois.readObject();
            prevStateMap = IdMap.IdName;
            IDNo = IdMap.ID;
        } catch (IOException | ClassNotFoundException e) {
            prevStateMap = serObj.IdName;
            IDNo = serObj.ID;
        }


        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                String[] recResp = input.readUTF().split(" ");
                if (recResp[0].equals("exit")) {
                    break;
                }
                Session session = new Session(socket, input, output, serObj, recResp);
                exSer.submit(session);
            }
            exSer.shutdown();
            System.exit(1);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
}
