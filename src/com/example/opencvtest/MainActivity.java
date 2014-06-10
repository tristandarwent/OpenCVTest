package com.example.opencvtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends Activity implements CvCameraViewListener2, OnTouchListener {
	private static final String TAG = "OCVSample::Activity";

    private OpenCVTestCameraView mOpenCvCameraView;
//    private List<Size> mResolutionList;
//    private MenuItem[] mEffectMenuItems;
//    private SubMenu mColorEffectsMenu;
//    private MenuItem[] mResolutionMenuItems;
//    private SubMenu mResolutionMenu;
    private boolean takeCapture = false; 
    
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
		Log.i(TAG, "TAPPED");
		takeCapture = true;
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
		
		// Taking inputFrame and putting it into a Mat
		Mat frame = inputFrame.rgba();
		
		// TopLeft clone Mat
		Rect topLeftRect = new Rect(0, (int)(frame.rows() - frame.rows()*0.4), (int)(frame.cols()*0.4), (int)(frame.rows()*0.4));
		Mat topLeftFrame = new Mat(frame, topLeftRect).clone();
		
		// BottomRight clone Mat
		Rect bottomRightRect = new Rect((int)(frame.cols() - frame.cols()*0.4), 0, (int)(frame.cols()*0.4), (int)(frame.rows()*0.4));
		Mat bottomRightFrame = new Mat(frame, bottomRightRect).clone();
		
		// Puts marker into tempFiles and gets path
		String[] files = null;
		File tempFileTL = null;
		File tempFileBR = null;
		
		AssetManager assetManager = this.getAssets();		
		try {
			InputStream isTL = assetManager.open("TLMarker.png");
			InputStream isBR = assetManager.open("BRMarker.png");
			
			tempFileTL = File.createTempFile("TLMarker", "png");
			tempFileBR = File.createTempFile("BRMarker", "png");
			
			tempFileTL.deleteOnExit();
			tempFileBR.deleteOnExit();
			
			FileOutputStream outTL = new FileOutputStream(tempFileTL);
			FileOutputStream outBR = new FileOutputStream(tempFileBR);
			
			IOUtils.copy(isTL, outTL);
			IOUtils.copy(isBR, outBR);
			Log.i(TAG, "PATH " + tempFileTL.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Mat for Top Left Marker
	    Mat topLeftMarker = Highgui.imread(tempFileTL.getAbsolutePath());
	
		// Mat for Bottom Right Marker
	    Mat bottomRightMarker = Highgui.imread(tempFileBR.getAbsolutePath());
	    
	    // Converts all images to Grayscale
//	    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
	    Imgproc.cvtColor(topLeftFrame, topLeftFrame, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(bottomRightFrame, bottomRightFrame, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(topLeftMarker, topLeftMarker, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(bottomRightMarker, bottomRightMarker, Imgproc.COLOR_RGB2GRAY);
    
		// For testing and debugging
	    if (bottomRightMarker.width() > 0){
	    	Log.i(TAG, "WIDTH: YES");
	    } else {
	    	Log.i(TAG, "WIDTH: NO");
	    }
	    Log.i(TAG, "WidthFrame " + frame.cols());
	    Log.i(TAG, "heightFrame " + frame.rows());
	    Log.i(TAG, "WidthFrameCut " + bottomRightFrame.cols());
	    Log.i(TAG, "heightFrameCut " + bottomRightFrame.rows());
	    Log.i(TAG, "Width " + bottomRightMarker.cols());
	    Log.i(TAG, "height " + bottomRightMarker.rows());
	    
	  // Create the result matrix for top left frame
	  int result_cols_tl = topLeftFrame.cols() - topLeftMarker.cols() + 1; 
	  int result_rows_tl = topLeftFrame.rows() - topLeftMarker.rows() + 1;
	  Mat resultTL = new Mat(result_rows_tl, result_cols_tl, CvType.CV_32FC1);
	    
      // Create the result matrix for bottom right frame
      int result_cols_br = bottomRightFrame.cols() - bottomRightMarker.cols() + 1; 
      int result_rows_br = bottomRightFrame.rows() - bottomRightMarker.rows() + 1;
      Mat resultBR = new Mat(result_rows_br, result_cols_br, CvType.CV_32FC1);
	        	    
      // I have no idea what this is... yet
	  int match_method = Imgproc.TM_SQDIFF;

	  // Do the Matching for bottom right
      Imgproc.matchTemplate(topLeftFrame, topLeftMarker, resultTL, match_method);
	  
	  // Do the Matching for bottom right
      Imgproc.matchTemplate(bottomRightFrame, bottomRightMarker, resultBR, match_method);
      
      // Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
      
      // Localizing the best match with minMaxLoc
      MinMaxLocResult mmrTL = Core.minMaxLoc(resultTL);
      MinMaxLocResult mmrBR = Core.minMaxLoc(resultBR);
      
      Log.i(TAG, "mmrTL " + mmrTL.maxVal);
	  Log.i(TAG, "mmrBR " + mmrBR.maxVal);

      // Gets point of bottom right match
      Point matchLocTL;
      Point matchLocBR;
      if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
    	  matchLocTL = mmrTL.minLoc;
          matchLocBR = mmrBR.minLoc;
      } else {
    	  matchLocTL = mmrTL.maxLoc;
    	  matchLocBR = mmrBR.maxLoc;
      }
      
      // Show me what you got
      Core.rectangle(frame, new Point(matchLocTL.x, (matchLocTL.y + frame.rows()*0.6)), new Point(matchLocTL.x + topLeftMarker.cols(),
    		  (matchLocTL.y + frame.rows()*0.6) + topLeftMarker.rows()), new Scalar(0, 255, 0));
      Core.rectangle(frame, new Point((matchLocBR.x + frame.cols()*0.6), matchLocBR.y), new Point((matchLocBR.x + frame.cols()*0.6) + bottomRightMarker.cols(),
    		  matchLocBR.y + bottomRightMarker.rows()), new Scalar(0, 255, 0));
      
      
      // X and Y coordinates for the capture points
      int captureTLx = (int) matchLocTL.x;
      int captureTLy = (int) (matchLocTL.y + frame.rows()*0.6) + topLeftMarker.rows();
      int captureBRx = (int) (matchLocBR.x + frame.cols()*0.6) + bottomRightMarker.cols();
      int captureBRy = (int) matchLocBR.y;
      
      // Draw rectangle of capture area for test purposes
      Core.rectangle(frame, new Point(captureTLx, captureTLy), new Point(captureBRx, captureBRy), new Scalar(255, 0, 0));
      
      if (takeCapture) {
    	  
    	  Log.i(TAG, "TAPPED 2");
    	  
    	  takeCapture = false;
    	  
	      // Get Mat of capture area
	      Rect captureRect = new Rect(new Point(captureTLx, captureTLy), new Point(captureBRx, captureBRy));
	      Mat capture = new Mat(frame, captureRect).clone();
	   
	      // convert to bitmap:
	      
	      // TO SWITCH FROM CAMERA TO TEST IMAGE ------------------
	      // Comment out code you wish to disable and uncomment code for mode you wish to enable
	      
//	      // For openCV Camera
//	      final Bitmap bm = Bitmap.createBitmap(capture.cols(), capture.rows(),Bitmap.Config.ARGB_8888);
//	      Utils.matToBitmap(capture, bm);
	      
	      // For test image
	      final Bitmap bm = BitmapFactory.decodeResource(getResources(),
                  R.drawable.upload);
	      
	      // -----------------------
	      
	      
	      runOnUiThread(new Runnable() {
	    	     @Override
	    	     public void run() {
	    	    	 
	    	    	 // Find imageView
	    	    	 ImageView iv = (ImageView) findViewById(R.id.captureImg);
	    	    	 
	    	    	 // Set cropped capture into imageView
//	    	    	 iv.setImageBitmap(bm);
	    	    	 
	    	    	 // Hide the cameraView to see the imageView behind it
	    	    	 mOpenCvCameraView.setVisibility(SurfaceView.INVISIBLE);
	    	    	 
	    	    	 // Run findContours function, passing through cropped image
	    	    	 findContour(bm);
	    	    }
	    	});
      }

      // Save the visualized detection.
      // System.out.println("Writing "+ outFile);
      // Highgui.imwrite(outFile, img);
		
		return frame;
	}
	
	public void findContour(Bitmap img) {
		Log.i(TAG, "RAN!");
		
		// FindContours function
		
		
		// Takes cropped image and converts it to Mat
		Bitmap imgCopy = img.copy(Bitmap.Config.ARGB_8888, true); 
		Mat originalImage = new Mat();
		Utils.bitmapToMat(imgCopy, originalImage);
		
		Log.i(TAG, "WidthFrame " + originalImage.cols());
	    Log.i(TAG, "heightFrame " + originalImage.rows());
	    
	    // Sets up for findContours
	    Mat manipulateImage = new Mat();
	    Imgproc.cvtColor(originalImage, manipulateImage, Imgproc.COLOR_RGB2GRAY);
	    Imgproc.blur(manipulateImage, manipulateImage, new Size(3,3));
	    Imgproc.Canny(manipulateImage, manipulateImage, 150, 150);
	    Imgproc.dilate(manipulateImage, manipulateImage, new Mat());
	    
	    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	    Mat hierarchy = new Mat();
		
	    // Finds the contours
	    Imgproc.findContours(manipulateImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
	    
	    // Loops through contours, allowing us to pinpoint the largest one
	    double maxArea = -1;
	    int maxAreaI = -1;
	    
	    for (int i=0; i<contours.size(); i++){
	    	Mat contour = contours.get(i);
	        double contourarea = Imgproc.contourArea(contour);
	        Log.i(TAG, "contourArea " + contourarea);
	        if (contourarea > maxArea) {
	            maxArea = contourarea;
	            maxAreaI = i;
	            Log.i(TAG, "maxArea " + maxArea);
	        }
	    }
	    
	    // Mat to store result from drawContours
	    Mat resultMat = Mat.zeros(manipulateImage.rows(),manipulateImage.cols(),manipulateImage.type());;
	    
	    // Draws contours
	    Imgproc.drawContours(resultMat, contours, maxAreaI, new Scalar(255), 4);
	    
	    Log.i(TAG, "resultMat cols " + resultMat.cols());
	    Log.i(TAG, "resultMat rows " + resultMat.rows());
	    
	    
	    // Converts result for drawContours to Bitmap
	    final Bitmap bm = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(),Bitmap.Config.ARGB_8888);
	    Utils.matToBitmap(resultMat, bm);
	    
	    runOnUiThread(new Runnable() {
   	     @Override
   	     public void run() {
   	    	 
   	    	// Displays in imageView
   	    	ImageView iv = (ImageView) findViewById(R.id.captureImg);
   		    iv.setImageBitmap(bm);
	   	    }
	   	});
	    
	}

}