package core.processing.processes.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessStream extends Thread {
    private BufferedReader bufferedReader;
    private List<String> list = new ArrayList<>();

    public ProcessStream(InputStream inputStream) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    public List<String> getList() {
        return this.list;
    }

    @Override
    public void run() {
        String input;
        try {
            while ((input = bufferedReader.readLine()) != null) {
                this.list.add(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
