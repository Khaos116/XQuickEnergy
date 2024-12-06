package io.github.lazyimmortal.sesame.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.github.lazyimmortal.sesame.R;

public class ExtendActivity extends BaseActivity {

    Button btnGetTreeItems, btnGetNewTreeItems;
    Button btnQueryAreaTrees, btnGetUnlockTreeItems;
    Button btnSetCustomWalkPathId, btnSetCustomWalkPathIdQueue;
    Button btnDeveloperMode;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_extend);
        setBaseTitle(getString(R.string.extend_options));
        btnGetTreeItems = findViewById(R.id.btn_get_tree_items);
        btnGetNewTreeItems = findViewById(R.id.btn_get_newTree_items);
        btnQueryAreaTrees = findViewById(R.id.btn_query_area_trees);
        btnGetUnlockTreeItems = findViewById(R.id.btn_get_unlock_treeItems);
        btnSetCustomWalkPathId = findViewById(R.id.btn_set_custom_walk_path_id);
        btnSetCustomWalkPathIdQueue = findViewById(R.id.btn_set_custom_walk_path_id_queue);
        btnDeveloperMode = findViewById(R.id.btn_developer_mode);


        btnGetTreeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendItemsBroadcast("getTreeItems", "", "");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnGetNewTreeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendItemsBroadcast("getNewTreeItems", "", "");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnQueryAreaTrees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendItemsBroadcast("queryAreaTrees", "", "");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnGetUnlockTreeItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendItemsBroadcast("getUnlockTreeItems", "", "");
                Toast.makeText(ExtendActivity.this, "已发送查询请求，请在森林日志查看结果！", Toast.LENGTH_SHORT).show();
            }
        });

        btnSetCustomWalkPathId.setOnClickListener(v -> {
            Context context = ExtendActivity.this;
            EditText input = new EditText(context);

            new AlertDialog.Builder(context)
                    .setTitle(getString(R.string.set_custom_walk_path_id))
                    .setView(input)
                    .setPositiveButton(getString(R.string.update_custom_walk_path_id), (dialog, which) -> {
                        String text = input.getText().toString().trim();
                        sendItemsBroadcast("setCustomWalkPathId", "setCustomWalkPathId", text);
                    }).setNegativeButton(getString(R.string.clear_custom_walk_path_id), (dialog, which) -> {
                        sendItemsBroadcast("setCustomWalkPathId", "clearCustomWalkPathId", "");
                    }).show();
        });
        btnSetCustomWalkPathIdQueue.setOnClickListener(v -> {
            Context context = ExtendActivity.this;
            EditText input = new EditText(context);

            new AlertDialog.Builder(context)
                    .setTitle(getString(R.string.set_custom_walk_path_id_queue))
                    .setView(input)
                    .setPositiveButton(getString(R.string.add_custom_walk_path_id_queue), (dialog, which) -> {
                        String text = input.getText().toString().trim();
                        sendItemsBroadcast("addCustomWalkPathIdQueue", "", text);
                    }).setNegativeButton(getString(R.string.clear_custom_walk_path_id_queue), (dialog, which) -> {
                        sendItemsBroadcast("clearCustomWalkPathIdQueue", "", "");
                    }).show();
        });

        btnDeveloperMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startActivity(new Intent(ExtendActivity.this, Class.forName("io.github.lazyimmortal.sesame.ui.AlphaActivity")));
                } catch (Exception e) {
                    Toast.makeText(ExtendActivity.this, "不符合开启资格！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendItemsBroadcast(String type, String method, String data) {
        Intent intent = new Intent("com.eg.android.AlipayGphone.sesame.rpctest");
        intent.putExtra("type", type);
        intent.putExtra("method", method);
        intent.putExtra("data", data);
        sendBroadcast(intent);
    }
}
