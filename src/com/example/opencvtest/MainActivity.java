package com.example.opencvtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.res.AssetManager;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class MainActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private static final String TAG = "OCVSample::Activity";

    private OpenCVTestCameraView mOpenCvCameraView;
    private List<Size> mResolutionList;
    private MenuItem[] mEffectMenuItems;
    private SubMenu mColorEffectsMenu;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (OpenCVTestCameraView) findViewById(R.id.java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
	@Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		Mat frame = inputFrame.rgba();
		
		String[] files = null;
		File tempFile = null;
		
		AssetManager assetManager = this.getAssets();		
		try {
			InputStream is = assetManager.open("Icon.png");
			tempFile = File.createTempFile("Icon", "png");
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(is, out);
			Log.i(TAG, "PATH " + tempFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	    Mat topLeftMat = Highgui.imread(tempFile.getAbsolutePath());
		Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(topLeftMat, topLeftMat, Imgproc.COLOR_RGB2GRAY);
    
	    if (topLeftMat.width() > 0){
	    	Log.i(TAG, "WIDTH: YES");
	    } else {
	    	Log.i(TAG, "WIDTH: NO");
	    }
	    Log.i(TAG, "Width " + topLeftMat.cols());
	    Log.i(TAG, "height " + topLeftMat.rows());
	    
//
      // / Create the result matrix
      int result_cols = frame.cols() - topLeftMat.cols() + 1; 
      int result_rows = frame.rows() - topLeftMat.rows() + 1;
      Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
	        	    
	  int match_method = Imgproc.TM_SQDIFF;

	  // Do the Matching and Normalize
	    
      Imgproc.matchTemplate(frame, topLeftMat, result, match_method);
      
//     Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
      
      // Localizing the best match with minMaxLoc
      MinMaxLocResult mmr = Core.minMaxLoc(result);

      Point matchLoc;
      if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
          matchLoc = mmr.minLoc;
      } else {
          matchLoc = mmr.maxLoc;
      }

      // Show me what you got
      Core.rectangle(frame, matchLoc, new Point(matchLoc.x + topLeftMat.cols(),
      matchLoc.y + topLeftMat.rows()), new Scalar(0, 255, 0));

//    // Save the visualized detection.
//    System.out.println("Writing "+ outFile);
//    Highgui.imwrite(outFile, img);
		
		return frame;
	}

}
