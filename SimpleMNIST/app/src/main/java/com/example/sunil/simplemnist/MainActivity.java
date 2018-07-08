package com.example.sunil.simplemnist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;

    static {
        System.loadLibrary("tensorflow_inference");
    }

    private static final String MODEL_FILE = "file:///android_asset/optimized_frozen_mnist_model.pb";
    private static final String INPUT_NODE = "x_input";
    private static final String OUTPUT_NODE = "y_actual";
    private static final int[] INPUT_SHAPE = {1, 784};
    private TensorFlowInferenceInterface inferenceInterface;


    private int imageListIndex = 9;
    private final int[] imageIDList = {
            R.drawable.digit0,
            R.drawable.digit1,
            R.drawable.digit2,
            R.drawable.digit3,
            R.drawable.digit4,
            R.drawable.digit5,
            R.drawable.digit6,
            R.drawable.digit7,
            R.drawable.digit8,
            R.drawable.digit9
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_view);

        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(getAssets(), MODEL_FILE);
    }

    public void predictDigitClick(View view)
    {
        float[] pixelBuffer = convertImage();
        float[] results = formPrediction(pixelBuffer);
        //for(float result : results)
        //{
        //    Log.d("result",String.valueOf(result));
        //}

        printResults(results);
    }

    private void printResults(float[] results)
    {
        float max=0;
        float secondMax = 0;
        int secondMaxIndex = 0;
        int maxIndex = 0;

        for(int i=0; i<10; i++)
        {
            if(results[i] > max)
            {
                secondMax = max;
                secondMaxIndex = maxIndex;

                max = results[i];
                maxIndex = i;
            }
            else if(results[i] < max && results[i] > secondMax)
            {
                secondMax = results[i];
                secondMaxIndex = i;
            }
        }

        String output = "Model predicts:" + String.valueOf(maxIndex) + ", second choice: " + String.valueOf(secondMaxIndex);
        textView.setText(output);
    }

    private float[] formPrediction(float[] pixelBuffer)
    {
        inferenceInterface.fillNodeFloat(INPUT_NODE, INPUT_SHAPE, pixelBuffer);
        inferenceInterface.runInference(new String[] {OUTPUT_NODE});
        float[] results = {0,0,0,0,0,0,0,0,0,0};
        inferenceInterface.readNodeFloat(OUTPUT_NODE, results);
        return results;
    }

    private float[] convertImage() {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),
                imageIDList[imageListIndex]);
        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 28, 28, true);
        imageView.setImageBitmap(imageBitmap);
        int[] imageAsIntArray = new int[784];
        float[] imageAsFloatArray = new float[784];
        imageBitmap.getPixels(imageAsIntArray, 0, 28, 0, 0, 28,28);
        for(int i=0; i<784; i++)
        {
            imageAsFloatArray[i] = imageAsIntArray[i] / (-16777216);
        }
        return imageAsFloatArray;
    }

    public void loadNextImageClick(View view)
    {
        imageListIndex = (imageListIndex >= 9)? 0 : imageListIndex + 1;
        imageView.setImageDrawable(getDrawable(imageIDList[imageListIndex]));
    }
}
