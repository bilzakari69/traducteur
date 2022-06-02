package com.khwarizmi.translatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Spinner fromSpinner, toSpinner;
    TextInputEditText sourceEdt;
    ImageView micIV;
    MaterialButton translateBtn;
    TextView translatedTv;

    String[] fromLanguages = {"French", "Spanish", "English", "Japanese"};
    String[] toLanguages = {"French", "Spanish", "English", "Japanese"};
    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguagecode, toLanguageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEdtSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTv = findViewById(R.id.idTvTranstedTV);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(adapterView.getItemAtPosition(position).toString().equals("English")) {
                    fromLanguagecode = FirebaseTranslateLanguage.EN;
                }
                if(adapterView.getItemAtPosition(position).toString().equals("Spanish")) {
                    fromLanguagecode = FirebaseTranslateLanguage.ES;
                }
                if(adapterView.getItemAtPosition(position).toString().equals("French")) {
                    fromLanguagecode = FirebaseTranslateLanguage.FR;
                }
                if(adapterView.getItemAtPosition(position).toString().equals("Japanese")) {
                    fromLanguagecode = FirebaseTranslateLanguage.JP;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if(adapterView.getItemAtPosition(position).toString().equals("English")) {
                    toLanguageCode = FirebaseTranslateLanguage.EN;
                }
                if(adapterView.getItemAtPosition(position).toString().equals("Spanish")) {
                    toLanguageCode = FirebaseTranslateLanguage.ES;
                }
                if(adapterView.getItemAtPosition(position).toString().equals("French")) {
                    toLanguageCode = FirebaseTranslateLanguage.FR;
                }
                if(adapterView.getItemAtPosition(position).toString().equals("Japanese")) {
                    toLanguageCode = FirebaseTranslateLanguage.JP;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedTv.setText("");
                if(sourceEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Veuillez écrire pour traduire", Toast.LENGTH_SHORT).show();
                } else if(fromLanguagecode==0){
                    Toast.makeText(MainActivity.this, "Veuillez sélectionner la langue source", Toast.LENGTH_SHORT).show();
                }else if(toLanguageCode==0){
                    Toast.makeText(MainActivity.this, "Veuillez sélectionner la langue à traduire", Toast.LENGTH_SHORT).show();
                }else {
                    translateText(fromLanguagecode,toLanguageCode,sourceEdt.getText().toString());
                }
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String lang= "FR";
                Locale locale=new Locale(lang);
                Locale.setDefault(locale);

                Intent i= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().getLanguage());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Parlez pour traduire");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if (resultCode==RESULT_OK && data!=null){
                ArrayList<String>result= data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS );
                sourceEdt.setText(result.get(0));
            }
        }
    }

    private void translateText(int fromLanguagecode, int toLanguageCode, String source){

        translatedTv.setText("Téléchargement...");
        FirebaseTranslatorOptions options= new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguagecode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator= FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions= new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTv.setText("Traduction en cours");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTv.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Echec de la traduction "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Echec du téléchargement de la langue "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}