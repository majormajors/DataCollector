package com.mattmayers.android.datacollector.writer;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by matt on 8/2/14.
 */
public class FileSessionWriter extends SessionWriter {
    private static final String BASE_DIR = "/sdcard/DataCollector";

    public FileSessionWriter(Context context) {
        super(context);
    }

    @Override
    public void writeData() {
        new File(BASE_DIR).mkdirs();
        FileWriter writer = null;
        try {
            writer = new FileWriter(
                    String.format("%s/%s", BASE_DIR, getFilePath()), false);
            writer.write(getSessionJson());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
