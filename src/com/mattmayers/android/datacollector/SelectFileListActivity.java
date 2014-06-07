package com.mattmayers.android.datacollector;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by matt on 6/7/14.
 */
public class SelectFileListActivity extends ListActivity {
    public static final String EXTRA_FILE_LIST = "EXTRA_FILE_LIST";
    public static final String DATA_FILE_PATH = "DATA_FILE_PATH";

    private ArrayAdapter<String> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_file_list);

        List<String> fileList = getIntent().getStringArrayListExtra(EXTRA_FILE_LIST);
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String file = mAdapter.getItem(position);
        Intent intent = new Intent();
        intent.putExtra(DATA_FILE_PATH, file);
        setResult(RESULT_OK, intent);
        finish();
    }
}
