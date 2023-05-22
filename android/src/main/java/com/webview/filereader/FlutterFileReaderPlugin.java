package com.webview.filereader;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.tencent.tbs.reader.ITbsReader;
import com.tencent.tbs.reader.ITbsReaderCallback;
import com.tencent.tbs.reader.TbsFileInterfaceImpl;

import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterX5Plugin
 */
public class FlutterFileReaderPlugin implements MethodChannel.MethodCallHandler, FlutterPlugin, ActivityAware {
    private static final String TAG = "FileReader";
    private int x5LoadStatus = -1; // -1 未加载状态  5 成功 10 失败

    public static final String channelName = "wv.io/FileReader";
    private Context ctx;
    private MethodChannel methodChannel;
    private FlutterPluginBinding pluginBinding;
    private ActivityPluginBinding activityBinding;

    private Handler mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 100) {
                if (methodChannel != null) {
                    methodChannel.invokeMethod("onLoad", isEngineLoaded());
                }
            }
            return false;
        }
    });


    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
        String method = methodCall.method;
        if ("init".equals(method)) {
            String licenseKey = methodCall.argument("licenseKey");
            initEngineAsync(pluginBinding.getApplicationContext(), licenseKey);
            pluginBinding.getPlatformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(pluginBinding.getBinaryMessenger(), activityBinding.getActivity(), this));
            result.success(null);
        } else if ("isLoad".equals(method)) {
            result.success(isEngineLoaded());
        } else if ("openFileByMiniQb".equals(method)) {
            String filePath = (String) methodCall.arguments;
            result.success(openFileByMiniQb(filePath));
        }
    }


    /**
     * 初始化SDK(该方法每次App启动后仅需调用一次)
     *
     * @param context
     * @param licenseKey 证书值
     */
    private void initEngineAsync(Context context, String licenseKey) {
        //调用该接口设置LicenseKey, 才能初始化SDK
        TbsFileInterfaceImpl.setLicenseKey(licenseKey);
        TbsFileInterfaceImpl.initEngineAsync(context, new ITbsReaderCallback() {
            @Override
            public void onCallBackAction(Integer actionType, Object args, Object result) {
                Log.i(TAG, "actionType=" + actionType + "，args=" + args + "，result=" + result);
                if (ITbsReader.OPEN_FILEREADER_ASYNC_LOAD_READER_ENTRY_CALLBACK == actionType) {
                    Log.e(TAG, "异步加载SDK结束. 加载SDK" + ((int) args == 0 ? "成功" : "失败"));
                    onX5LoadComplete();
                }
            }
        });
    }

    public FlutterFileReaderPlugin() {

    }

    private void onDestroyed() {
        Log.e(TAG, "销毁");
        ctx = null;
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler = null;
        methodChannel = null;
        pluginBinding = null;
        activityBinding = null;
    }


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        FlutterFileReaderPlugin plugin = new FlutterFileReaderPlugin();
        registrar.platformViewRegistry().registerViewFactory("FileReader", new X5FileReaderFactory(registrar.messenger(), registrar.activity(), plugin));
    }


    public boolean openFileByMiniQb(String filePath) {
        if (ctx != null) {
            Bundle param = new Bundle();
            param.putString("filePath", filePath);
            String fileExt = FilenameUtils.getExtension(filePath);
            param.putString("fileExt", fileExt);
            param.putString("tempPath", Objects.requireNonNull(ctx.getExternalFilesDir("temp")).getAbsolutePath());
            if (!TbsFileInterfaceImpl.canOpenFileExt(fileExt)) return false;
            TbsFileInterfaceImpl.getInstance().openFileReader(ctx, param, new ITbsReaderCallback() {
                @Override
                public void onCallBackAction(Integer actionType, Object args, Object result) {
                    Log.i(TAG, "actionType=" + actionType + "，args=" + args + "，result=" + result);
                    if (ITbsReader.OPEN_FILEREADER_STATUS_UI_CALLBACK == actionType) {
                        if (args instanceof Bundle) {
                            int id = ((Bundle) args).getInt("typeId");
                            if (ITbsReader.TBS_READER_TYPE_STATUS_UI_SHUTDOWN == id) {
                                TbsFileInterfaceImpl.getInstance().closeFileReader();
                            }
                        }
                    }
                }
            }, null);


        }
        return true;
    }


    private void onX5LoadComplete() {
        mainHandler.sendEmptyMessage(100);
    }


    int isEngineLoaded() {
        if (ctx != null && TbsFileInterfaceImpl.isEngineLoaded()) {
            x5LoadStatus = 5;
        }
        return x5LoadStatus;
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        Log.e(TAG, "onAttachedToEngine");

        pluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        Log.e(TAG, "onDetachedFromEngine");
        onDestroyed();
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        Log.e(TAG, "onAttachedToActivity");
        activityBinding = binding;
        ctx = pluginBinding.getApplicationContext();
        methodChannel = new MethodChannel(pluginBinding.getBinaryMessenger(), channelName);
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.e(TAG, "onDetachedFromActivityForConfigChanges");
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        Log.e(TAG, "onReattachedToActivityForConfigChanges");
    }

    @Override
    public void onDetachedFromActivity() {
        Log.e(TAG, "onDetachedFromActivity");
    }

}

