package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    private static final int PORT = 23456;
    private static final String IP = "127.0.0.1";

    public static void main(String[] args) {
        System.out.println("Client started!");
        try (Socket socket = new Socket(IP, PORT);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");
            String choice = sc.next();
            if(choice.equals("exit")) {
                output.writeUTF("exit");
            } else {
                String folderLoc = "C:\\Users\\AYUSH KUMAR\\IdeaProjects\\File Server\\File Server\\task\\src\\client\\data\\";
                String response;
                int reply;
                switch(choice) {
                    case "1":
                        System.out.print("Do you want to get the file by name or by id (1 - name, 2 - id): ");
                        reply = sc.nextInt();
                        if (reply == 1) {
                            System.out.print("Enter name: ");
                            String BY_NAME = sc.next();
                            output.writeUTF("GET " + BY_NAME);
                        } else if (reply == 2) {
                            System.out.print("Enter id: ");
                            int BY_ID = sc.nextInt();
                            output.writeUTF("GET " + BY_ID);
                        }
                        System.out.println("The request was sent.");

                        response = input.readUTF();
                        if(response.equals("200")) {
                            int byteLength = input.readInt();
                            byte[] message = new byte[byteLength];
                            input.readFully(message, 0, byteLength);
                            System.out.print("The file was downloaded! Specify a name for it: ");
                            String fileName = sc.next();
                            try (FileOutputStream fileOut = new FileOutputStream(folderLoc+fileName)) {
                                fileOut.write(message);
                                System.out.println("File saved on the hard drive!");
                            }
                        } else if(response.equals("404")){
                            System.out.println("The response says that this file is not found!");
                        }
                        break;
                    case "2":
                        System.out.print("Enter name of the file: ");
                        String filename = sc.next();
                        File f = new File(folderLoc+filename);
                        String serverFileName;
                        if(f.exists()) {
                            System.out.print("Enter name of the file to be saved on server: ");
                            sc.nextLine();
                            serverFileName = sc.nextLine();
                        } else {
                            System.out.println("There is no any file present in your location");
                            break;
                        }
                        int byteLen;
                        byte[] message;
                        try (FileInputStream fileInp = new FileInputStream(folderLoc+filename);
                             DataInputStream datafileInp = new DataInputStream(fileInp))
                        {
                            byteLen = datafileInp.available();
                            message = new byte[byteLen];
                            fileInp.read(message);
                        }

                        output.writeUTF("PUT " + serverFileName);
                        output.writeInt(byteLen);
                        output.write(message);

                        System.out.println("The request was sent.");

                        response = input.readUTF();
                        if(response.equals("200")) {
                            int fileID = input.readInt();
                            System.out.println("Response says that file is saved! ID = " + fileID);
                        } else if(response.equals("403")) {
                            System.out.println("The response says that saving the file was forbidden!");
                        }
                        break;
                    case "3":
                        System.out.print("Do you want to delete the file by name or by id (1 - name, 2 - id): ");
                        reply = sc.nextInt();
                        if (reply == 1) {
                            System.out.print("Enter name: ");
                            String BY_NAME = sc.next();
                            output.writeUTF("DELETE " + BY_NAME);
                        } else if (reply == 2) {
                            System.out.print("Enter id: ");
                            int BY_ID = sc.nextInt();
                            output.writeUTF("DELETE " + BY_ID);
                        }
                        System.out.println("The request was sent.");

                        response = input.readUTF();
                        if(response.equals("200")) {
                            System.out.println("The response says that the file was successfully deleted!");
                        } else if(response.equals("404")) {
                            System.out.println("The response says that this file is not found!");
                        }
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
