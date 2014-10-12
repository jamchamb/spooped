package edu.rutgers.jamchamb.spooped;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

public class SpoopService extends Service {

    private WindowManager windowManager;
    private ImageView spoopyGhost;

    public SpoopService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        spoopyGhost = new ImageView(this);
        spoopyGhost.setImageResource(R.drawable.spoopy_ghost);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 100;

        windowManager.addView(spoopyGhost, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(spoopyGhost != null) windowManager.removeView(spoopyGhost);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
