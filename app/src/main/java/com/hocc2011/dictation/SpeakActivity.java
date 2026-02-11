package com.hocc2011.dictation;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SpeakActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private List<String> sentenceList = new ArrayList<>();
    private int sentenceIndex = 0;
    private boolean isTtsReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_speak);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.UK);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                } else {
                    isTtsReady = true;
                    tts.setSpeechRate(0.8f);
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });

        // Get text and split
        String text = getIntent().getStringExtra("text");
        if (text != null && !text.isEmpty()) {
            // Split by punctuation but keep a reasonable list
            sentenceList = Arrays.asList(text.split("(?<=[.,])\\s*"));
        }

        ImageButton previous = findViewById(R.id.btn_previous);
        previous.setOnClickListener(v -> {
            if (sentenceIndex > 0) {
                sentenceIndex--;
                Toast.makeText(this, "Sentence " + (sentenceIndex + 1), Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton next = findViewById(R.id.btn_next);
        next.setOnClickListener(v -> {
            // Fix: Check against size - 1 to stay within bounds
            if (sentenceIndex < sentenceList.size() - 1) {
                sentenceIndex++;
                Toast.makeText(this, "Sentence " + (sentenceIndex + 1), Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton play = findViewById(R.id.btn_play);
        play.setOnClickListener(v -> {
            if (isTtsReady && !sentenceList.isEmpty()) {
                speakCurrentSentence();
            } else if (!isTtsReady) {
                Toast.makeText(this, "TTS Engine not ready", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void speakCurrentSentence() {
        String rawText = sentenceList.get(sentenceIndex);

        // Clean up text and verbalize punctuation
        String spokenText = rawText
                .replace(".", " full stop ")
                .replace(",", " comma ")
                .replace("?", " question mark ")
                .replace("!", " exclamation mark ")
                .replace(":", " colon ")
                .replace(";", " semicolon ")
                .replace("'", " apostrophe ")
                .replace("\"", " quotation mark ");

        Log.d("TTS", "Speaking: " + spokenText);
        // queue_flush interrupts current speech to play the new one immediately
        tts.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "dictation_id");
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}