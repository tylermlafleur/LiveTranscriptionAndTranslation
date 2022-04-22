package GoogleAPI;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileOutput {
    public static void writeToFile(String fileName, String text) throws IOException {
        FileWriter writer = new FileWriter(fileName, true);
        writer.write(text);
        writer.close();

    }
}
