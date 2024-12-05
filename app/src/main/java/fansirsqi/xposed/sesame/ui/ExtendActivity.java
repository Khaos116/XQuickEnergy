package fansirsqi.xposed.sesame.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import fansirsqi.xposed.sesame.R;
import fansirsqi.xposed.sesame.util.Log;

/**
 * 扩展功能页面
 */
public class ExtendActivity extends BaseActivity {

    /**
     * 初始化Activity
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend); // 设置布局文件

        // 初始化按钮并设置点击事件
        initButtonsAndSetListeners();
    }

    /**
     * 初始化按钮并设置监听器
     */
    private void initButtonsAndSetListeners() {
        // 定义按钮变量并绑定按钮到对应的View
        Button btnGetTreeItems = findViewById(R.id.get_tree_items);
        Button btnGetNewTreeItems = findViewById(R.id.get_newTree_items);
//        下面这两个按钮未找到对应功能暂不开启
//        Button btnQueryAreaTrees = findViewById(R.id.query_area_trees);
//        Button btnGetUnlockTreeItems = findViewById(R.id.get_unlock_treeItems);

        // 设置Activity标题
        setBaseTitle("扩展功能");
        // 为每个按钮设置点击事件
        btnGetTreeItems.setOnClickListener(new TreeItemsOnClickListener());
        btnGetNewTreeItems.setOnClickListener(new NewTreeItemsOnClickListener());
//        btnQueryAreaTrees.setOnClickListener(new AreaTreesOnClickListener());
//        btnGetUnlockTreeItems.setOnClickListener(new UnlockTreeItemsOnClickListener());
    }

    /**
     * 发送广播事件
     * @param type 广播类型
     */
    private void sendItemsBroadcast(String type) {
        Intent intent = new Intent("com.eg.android.AlipayGphone.sesame.rpctest");
        intent.putExtra("method", "");
        intent.putExtra("data", "");
        intent.putExtra("type", type);
        sendBroadcast(intent); // 发送广播
        Log.debug("发送广播：" + type);
    }

    /**
     * 获取树项目按钮的点击监听器
     */
    private class TreeItemsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendItemsBroadcast("getTreeItems");
            Toast.makeText(ExtendActivity.this, "已发送请求，请在全部日志查看结果！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取新树项目按钮的点击监听器
     */
    private class NewTreeItemsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendItemsBroadcast("getNewTreeItems");
            Toast.makeText(ExtendActivity.this, "已发送请求，请在全部日志查看结果！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 查询未解锁🔓地区
     */
    private class AreaTreesOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendItemsBroadcast("queryAreaTrees");
            Toast.makeText(ExtendActivity.this, "已发送请求，请在全部日志查看结果！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 查询未解锁🔓🌳木项目
     */
    private class UnlockTreeItemsOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            sendItemsBroadcast("getUnlockTreeItems");
            Toast.makeText(ExtendActivity.this, "已发送请求，请在全部日志查看结果！", Toast.LENGTH_SHORT).show();
        }
    }
}
