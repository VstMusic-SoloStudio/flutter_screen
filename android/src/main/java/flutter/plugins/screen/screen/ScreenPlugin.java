package flutter.plugins.screen.screen;

import android.app.Activity;
import android.provider.Settings;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;

/**
 * ScreenPlugin
 */
public class ScreenPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

  private Activity activity;
  private MethodChannel channel;

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
    this.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
    this.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
    this.channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "github.com/clovisnicolas/flutter_screen");
    this.channel.setMethodCallHandler(this);
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
    this.channel.setMethodCallHandler(null);
    this.channel = null;
  }

  @Override
  public void onMethodCall(MethodCall call, @NonNull Result result) {
    if(this.activity == null) {
      result.success(null);
      return;
    }
    switch(call.method){
      case "brightness":
        result.success(getBrightness());
        break;
      case "setBrightness":
        double brightness = call.argument("brightness");
        WindowManager.LayoutParams layoutParams = this.activity.getWindow().getAttributes();
        layoutParams.screenBrightness = (float)brightness;
        this.activity.getWindow().setAttributes(layoutParams);
        result.success(null);
        break;
      case "resetBrightness":
        WindowManager.LayoutParams params = this.activity.getWindow().getAttributes();
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        this.activity.getWindow().setAttributes(params);
        result.success(null);
        break;
      case "isKeptOn":
        int flags = this.activity.getWindow().getAttributes().flags;
        result.success((flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0) ;
        break;
      case "keepOn":
        Boolean on = call.argument("on");
        if (on) {
          System.out.println("Keeping screen on ");
          this.activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else{
          System.out.println("Not keeping screen on");
          this.activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        result.success(null);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  private float getBrightness(){
    float result = this.activity.getWindow().getAttributes().screenBrightness;
    if (result < 0) { // the application is using the system brightness
      try {
        result = Settings.System.getInt(this.activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / (float)255;
      } catch (Settings.SettingNotFoundException e) {
        result = 1.0f;
        e.printStackTrace();
      }
    }
    return result;
  }

}
