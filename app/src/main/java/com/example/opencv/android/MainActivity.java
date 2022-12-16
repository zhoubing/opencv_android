package com.example.opencv.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.engine.Utils;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pic);
        OpenCVNativeLoader openCVNativeLoader = new OpenCVNativeLoader();
        openCVNativeLoader.init();
//        ByteBuffer buffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
//        bitmap.copyPixelsToBuffer(buffer);
//        Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4, buffer);
//        Log.d("OpenCV", "mat channels: " + mat.channels() + " cols: " + mat.cols() + " rows: " + mat.rows());
        initClassifier();
    }


    private void initClassifier() {
        try {
            InputStream is = getResources()
                    .openRawResource(com.example.opencv.opencvlib.R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "raw/lbpcascade_frontalface.xml");
            if (!cascadeFile.getParentFile().exists()) {
                cascadeFile.getParentFile().mkdirs();
            }
            FileOutputStream os = new FileOutputStream(cascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pic);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(byteBuffer);
            Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4, byteBuffer);

            CascadeClassifier classifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (classifier.empty()) {
                Toast.makeText(this, "load classifier failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "load classifier successful", Toast.LENGTH_SHORT).show();
            }
            MatOfRect faces = new MatOfRect();
            classifier.detectMultiScale(mat, faces, 1.1, 3, 2, new Size(bitmap.getHeight() * 0.4, bitmap.getHeight() * 0.4));
            Rect[] facesArray = faces.toArray();
            for (int i = 0; i < facesArray.length; i++) {
                Imgproc.rectangle(mat, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            }


            Bitmap bmp;
            Mat tmp = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8U, new Scalar(4));
            try {
                Imgproc.cvtColor(mat, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
                bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(tmp, bmp);
                ImageView imageView = findViewById(R.id.image);
                imageView.setImageBitmap(bmp);
            } catch (CvException e) {
                Log.d("Exception", e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
