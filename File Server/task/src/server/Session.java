package server;

import java.io.*;
import java.net.Socket;
import static server.Main.*;

public class Session implements Runnable {
    Socket socket;
    DataInputStream input;
    DataOutputStream output;
    ObjectState serObj;
    String[] recResp;

    public Session(Socket socket, DataInputStream input, DataOutputStream output, ObjectState serObj, String[] recResp) {
        this.socket = socket;
        this.input = input;
        this.output = output;
        this.serObj = serObj;
        this.recResp = recResp;
    }



    @Override
    public void run() {
        try {
             if (recResp[0].equals("PUT")) {
                IDNo++;
                String serverFilename;
                if (recResp.length > 1) {
                    serverFilename = recResp[1];
                } else {
                    serverFilename = "rndName" + IDNo + ".txt";
                }
                File f = new File(fileLoc + serverFilename);
                if (!f.exists()) {
                    synchronized (this) {
                        try (FileOutputStream fileOut = new FileOutputStream(f)) {
                            int byteLength = input.readInt();
                            byte[] message = new byte[byteLength];
                            input.readFully(message, 0, byteLength);
                            fileOut.write(message);
                            prevStateMap.put(IDNo, serverFilename);
                            output.writeUTF("200");
                            output.writeInt(IDNo);
                        }
                    }
                } else {
                    IDNo--;
                    output.writeUTF("403");
                }
            } else {
                 synchronized (this) {
                     try {
                         if (prevStateMap == null) {
                             output.writeUTF("404");
                         } else if (prevStateMap.containsKey(Integer.parseInt(recResp[1]))) {
                             int id = Integer.parseInt(recResp[1]);
                             String fileNam = prevStateMap.get(id);
                             if (recResp[0].equals("GET")) {
                                 GetResp(output, fileNam);
                             } else if (recResp[0].equals("DELETE")) {
                                 File f = new File(fileLoc + fileNam);
                                 f.delete();
                                 prevStateMap.remove(id);
                                 output.writeUTF("200");
                             }
                         } else {
                             output.writeUTF("404");
                         }
                     } catch (NumberFormatException e) {
                         if (prevStateMap.containsValue(recResp[1])) {
                             if (recResp[0].equals("GET")) {
                                 GetResp(output, recResp[1]);
                             } else if (recResp[0].equals("DELETE")) {
                                 File f = new File(fileLoc + recResp[1]);
                                 f.delete();
                                 int id = getId(recResp[1]);
                                 prevStateMap.remove(id);
                                 output.writeUTF("200");
                             }
                         } else {
                             output.writeUTF("404");
                         }
                     }
                 }
            }

            try (FileOutputStream fos = new FileOutputStream(fileLoc + "fileState.txt");
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                serObj.ID = IDNo;
                serObj.IdName = prevStateMap;
                oos.writeObject(serObj);
            }
        } catch (IOException e) {e.printStackTrace();}
    }

    private int getId(String mapFileName) {
        int[] Id = new int[1];
        prevStateMap.forEach((id, file)-> {
            if (file.equals(mapFileName)) {
                Id[0] = id;
            }
        });
        return Id[0];
    }

    private void GetResp(DataOutputStream output, String fileName) throws IOException {
        int byteLen;
        byte[] message;
        try (FileInputStream fileInp = new FileInputStream(fileLoc + fileName);
             DataInputStream datafileInp = new DataInputStream(fileInp))
        {
            byteLen = datafileInp.available();
            message = new byte[byteLen];
            fileInp.read(message);
        }

        output.writeUTF("200");
        output.writeInt(byteLen);
        output.write(message);
    }
}
