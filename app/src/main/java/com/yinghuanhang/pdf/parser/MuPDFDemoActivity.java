package com.yinghuanhang.pdf.parser;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.artifex.mupdfdemo.MuPDFCore;

import java.io.File;
import java.util.Locale;

/**
 * Created by Cao-Human on 2018/1/2
 */

public class MuPDFDemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_mupdf_parser);
        RecyclerView recycler = (RecyclerView) findViewById(R.id.parser_display);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        recycler.setLayoutManager(manager);
        recycler.setAdapter(mAdapter);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        onFindingPortableDocumentFormat(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onFindingPortableDocumentFormat(intent);
    }

    private MuPDFDemoAdapter mAdapter = new MuPDFDemoAdapter();

    /**
     * 查看当前是否包含文档内容
     *
     * @param intent 执行进程信息
     */
    private void onFindingPortableDocumentFormat(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            if (!onFindingFromActionSEND(intent)) {
                Toast.makeText(this, "打开文档失败", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (!onFindingFromActionVIEW(intent)) {
                Toast.makeText(this, "打开文档失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean onFindingFromActionSEND(Intent extraIntent) {
        Uri uri = extraIntent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri == null) {  // 数据内容为空
            return false;
        }
        String path = Uri.decode(uri.getEncodedPath());
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            return false;
        }
        onFindingPortablePathToParse(path);
        return true;
    }

    private boolean onFindingFromActionVIEW(Intent extraIntent) {
        String path = Uri.decode(extraIntent.getData().getEncodedPath());
        if (TextUtils.isEmpty(path) || !(new File(path).exists())) {
            return false;
        }
        onFindingPortablePathToParse(path);
        return true;
    }

    private void onFindingPortablePathToParse(String path) {
        if (!TextUtils.isEmpty(path)) {
            try {
                MuPDFCore core = new MuPDFCore(path);
                onParsingPortableDocument(core, path);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "打开文档失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 解析文档
     *
     * @param document 文档路径
     */
    private void onParsingPortableDocument(final MuPDFCore core, final String document) {
        if (core.needsPassword()) {
            final EditText editor = (EditText) findViewById(R.id.parser_word);
            editor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (EditorInfo.IME_ACTION_DONE == actionId) {
                        onEnteringPasswordsForDocument(core, document, editor.getText().toString());
                    }
                    return false;
                }
            });
            editor.setVisibility(View.VISIBLE);

            return;
        }

        onParsingToStorage(core, document);
    }

    private void onEnteringPasswordsForDocument(MuPDFCore core, String document, String password) {
        if (core.authenticatePassword(password)) {
            onParsingToStorage(core, document);
            return;
        }
        Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onParsingToStorage(MuPDFCore core, String document) {
        File file = new File(document);
        String name = file.getName().toLowerCase();
        DocumentParser parser = new DocumentParser(core) {
            @Override
            protected void onProgressUpdate(String... values) {
                mAdapter.insert(values[0]);
            }
        };
        parser.execute(name.replace(".pdf", ""));
    }

    private static class DocumentParser extends AsyncTask<String, String, Boolean> {
        DocumentParser(MuPDFCore core) {
            mParser = core;
        }

        private MuPDFCore mParser;

        @Override
        protected Boolean doInBackground(String... params) {
            MuPDFCore.Cookie cookie = mParser.new Cookie();
            for (int index = 0; index < mParser.countPages(); index++) {
                String name = String.format(Locale.getDefault(), "%s(%d)", params[0], (index + 1));
                String path = FileUtils.onBuildingCache("temporary/" + params[0], name, "png");
                if (new File(path).exists()) {
                    publishProgress(path);
                    continue;
                }
                PointF size = mParser.getPageSize(index);
                Bitmap bitmap = Bitmap.createBitmap((int) size.x, (int) size.y, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.BLACK);
                mParser.drawPage(bitmap, index, (int) size.x, (int) size.y, 0, 0, (int) size.x, (int) size.y, cookie);
                FileUtils.onSaveBitmapTo(bitmap, path);
                bitmap.recycle();
                publishProgress(path);
            }
            cookie.destroy();
            return true;
        }
    }
}
