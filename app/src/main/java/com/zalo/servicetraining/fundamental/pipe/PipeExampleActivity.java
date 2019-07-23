package com.zalo.servicetraining.fundamental.pipe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.zalo.servicetraining.R;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class PipeExampleActivity extends AppCompatActivity {
    private static final String TAG = "PipeExampleActivity";
    private EditText mEditText;
    private TextView mTextView;
    private static final int LOG = 1;
    private static final String LOG_CONTENT ="log_content";

    PipedReader r;
    PipedWriter w;
    private Thread workerThread;
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what==LOG) {
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    String log = bundle.getString(LOG_CONTENT);
                    if(log!=null)
                        mTextView.setText(log+"\n"+mTextView.getText());
                }
            }
        }
    };

    TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start,
                                      int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start,
                                  int before, int count) {
            try {

                if(count > before) {
                    w.write(charSequence.subSequence(before, count).
                            toString());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r = new PipedReader();
        w = new PipedWriter();
        try {
            w.connect(r);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_pipe);
        mEditText = findViewById(R.id.edit_text);
        mTextView = findViewById(R.id.log_text_view);
        mEditText.addTextChangedListener(mTextWatcher);

        workerThread = new Thread(new TextHandlerTask(r).setUIHandler(mUIHandler));

        workerThread.start();
    }

    @Override
    protected void onDestroy() {

        mEditText.removeTextChangedListener(mTextWatcher);
        mTextWatcher = null;
        workerThread.interrupt();
        workerThread = null;
        try {
            r.close();
            w.close();
        } catch (IOException ignored) {}
        super.onDestroy();
    }

    private static class TextHandlerTask implements Runnable {
        private final PipedReader reader;
        private Handler mUIHandler;

        public TextHandlerTask setUIHandler(Handler handler) {
            mUIHandler = handler;
            return this;
        }

        private void log(String log){
            if(mUIHandler!=null) {
                Message message = new Message();
                message.what = LOG;
                Bundle bundle = new Bundle();
                bundle.putString(LOG_CONTENT,log);
                message.setData(bundle);
                mUIHandler.sendMessage(message);
            }
        }

        TextHandlerTask(PipedReader reader){
            this.reader = reader;
        }
        @Override
        public void run() {
            log("run: pipe-thread started");
          /*  while(!Thread.currentThread().isInterrupted()){
                Log.d(TAG, "loop");
                try {
                    int i;
                    while((i = reader.read()) != -1){
                        char c = (char) i;
                        Log.d(TAG, "char = " + c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
            int i;
            while (!Thread.currentThread().isInterrupted()&&(i = tryRead())!=-1) {
                log("run: loop");
                char c = (char)i;
                log( "run: char = "+ c);
            }
            Log.d(TAG, "run: finish");
        }

        int tryRead() {
            try {
                return reader.read();
            } catch (IOException e) {
                return -1;
            }
        }
    }
}