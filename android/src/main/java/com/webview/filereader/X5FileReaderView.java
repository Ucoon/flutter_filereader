package com.webview.filereader;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.tencent.tbs.reader.ITbsReader;
import com.tencent.tbs.reader.ITbsReaderCallback;
import com.tencent.tbs.reader.TbsFileInterfaceImpl;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;

public class X5FileReaderView implements PlatformView, ITbsReaderCallback, MethodChannel.MethodCallHandler {
    private static final String TAG = "FileReader";
    private MethodChannel methodChannel;
    private FrameLayout readerView;//内容显示区域
    private final String tempPath;
    FlutterFileReaderPlugin plugin;


    X5FileReaderView(Context context, BinaryMessenger messenger, int id, Map<String, Object> params, FlutterFileReaderPlugin plugin) {
        this.plugin = plugin;
        tempPath = Objects.requireNonNull(context.getExternalFilesDir("temp")).getAbsolutePath();
        methodChannel = new MethodChannel(messenger, FlutterFileReaderPlugin.channelName + "_" + id);
        methodChannel.setMethodCallHandler(this);
        //这里的Context需要Activity
        readerView = new FrameLayout(context);
        readerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {

        switch (methodCall.method) {
            case "openFile":
                if (isSupportFile((String) methodCall.arguments)) {
                    openFile((String) methodCall.arguments);
                    result.success(true);
                } else {
                    result.success(false);
                }
                break;
            case "canOpen":
                result.success(isSupportFile((String) methodCall.arguments));
                break;


        }
    }


    void openFile(String filePath) {
        if (isSupportFile(filePath)) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            File bsReaderTempFile = new File(tempPath);
            if (!bsReaderTempFile.exists()) {
                bsReaderTempFile.mkdir();
            }
            //加载文件
            Bundle param = new Bundle();
            param.putString("filePath", filePath);
            param.putString("fileExt", FilenameUtils.getExtension(filePath));
            param.putString("tempPath", tempPath);
            //调用openFileReader打开文件
            readerView.post(() -> {
                int height = readerView.getHeight();
                // 自定义layout模式必须设置这个值，否则可能导致文档内容显示不全
                param.putInt("set_content_view_height", height);
                TbsFileInterfaceImpl.getInstance().openFileReader(
                        readerView.getContext(), param, X5FileReaderView.this, readerView);
            });

        }

    }


    boolean isSupportFile(String filePath) {
        String fileExt = FilenameUtils.getExtension(filePath);
        return TbsFileInterfaceImpl.canOpenFileExt(fileExt);
    }

    @Override
    public View getView() {
        return readerView;
    }

    @Override
    public void dispose() {
        Log.d(TAG, "FileReader Close");
        readerView.removeAllViews();
        methodChannel.setMethodCallHandler(null);
        methodChannel = null;
        readerView = null;
    }

    @Override
    public void onCallBackAction(Integer actionType, Object args, Object result) {
        Log.i(TAG, "actionType=" + actionType + "，args=" + args + "，result=" + result);
        if (ITbsReader.OPEN_FILEREADER_STATUS_UI_CALLBACK == actionType) {
            if (args instanceof Bundle) {
                int id = ((Bundle) args).getInt("typeId");
                if (ITbsReader.TBS_READER_TYPE_STATUS_UI_SHUTDOWN == id) {
                    dispose();      // 加密文档弹框取消需关闭activity
                }
            }
        }

    }
}
