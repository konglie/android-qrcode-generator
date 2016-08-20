package com.kurungkurawal.qrcodegenerator;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String tag = "QRCGEN";

    private MainActivity self;

    private EditText txtQRText;
    private Button btnGenerate, btnReset;
    private ImageView imgResult;
    private ProgressBar loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;

        txtQRText   = (EditText)findViewById(R.id.txtQR);
        btnGenerate = (Button)findViewById(R.id.btnGenerate);
        btnReset    = (Button)findViewById(R.id.btnReset);
        imgResult   = (ImageView)findViewById(R.id.imgResult);
        loader      = (ProgressBar)findViewById(R.id.loader);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.generateImage();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.reset();
            }
        });
    }

    private void alert(String message){
        AlertDialog dlg = new AlertDialog.Builder(self)
                .setTitle("QRCode Generator")
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        dlg.show();
    }

    private void endEditing(){
        txtQRText.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }


    private void generateImage(){
        final String text = txtQRText.getText().toString();
        if(text.trim().isEmpty()){
            alert("Ketik dulu data yang ingin dibuat QR Code");
            return;
        }

        endEditing();
        showLoadingVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = imgResult.getMeasuredWidth();
                if( size > 1){
                    Log.e(tag, "size is set manually");
                    size = 260;
                }

                Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
                hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
                hintMap.put(EncodeHintType.MARGIN, 1);
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                try {
                    BitMatrix byteMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size,
                            size, hintMap);
                    int height = byteMatrix.getHeight();
                    int width = byteMatrix.getWidth();
                    final Bitmap qrImage = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    for (int x = 0; x < width; x++){
                        for (int y = 0; y < height; y++){
                            qrImage.setPixel(x, y, byteMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                        }
                    }

                    self.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgResult.setImageBitmap(qrImage);
                            self.showLoadingVisible(false);
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                    alert(e.getMessage());
                }
            }
        }).start();
    }

    private void showLoadingVisible(boolean visible){
        if(visible){
            imgResult.setImageResource(android.R.color.transparent);
        }
        loader.setVisibility(
                (visible) ? View.VISIBLE : View.GONE
        );
    }

    private void reset(){
        txtQRText.setText("");
        imgResult.setImageResource(android.R.color.transparent);
        endEditing();
    }
}
