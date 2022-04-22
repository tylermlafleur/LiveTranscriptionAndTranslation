package GoogleAPI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class FileOutput {
    FileWriter writer;
    String fileName;

    public FileOutput(String fileName) {
        this.fileName = fileName;
    }

    public void openFile() {
        try{
            writer = new FileWriter(fileName, true);

        } catch (IOException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }

    public void writeToFile(String text) throws IOException {
        writer.write(text);
    }

    public void closeFile() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
