package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String urlEndPoint = "https://api.openai.com/v1/chat/completions";
    private String myApiKey = BuildConfig.API_KEY;
    private String output ="";
    public static final MediaType JSON = MediaType.get("application/json");
    AppCompatButton button;
    OkHttpClient client = new OkHttpClient();
    Spinner spinnerLanguage , spinnerLength;
    SeekBar seekBar;
    TextView textViewSeekBar;
    ChipGroup chipGroup;
    String language="English",size="150",age="5", gender="male",genres="Mystery",Prompt="";
    private RadioButton radioButton;
    private RadioGroup radioGroup;
    private ALoadingDialog aLodingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button=findViewById(R.id.button);
        seekBar=findViewById(R.id.seekbar);
        textViewSeekBar=findViewById(R.id.textView5);
        spinnerLanguage=findViewById(R.id.spinner1);
        spinnerLength=findViewById(R.id.spinner2);
        radioGroup = findViewById(R.id.gender);
        chipGroup=findViewById(R.id.chipGroup);
        aLodingDialog = new ALoadingDialog(this);
        ArrayList<String>arrayList = new ArrayList<>();
        arrayList.add("Action");
        arrayList.add("Comedy");
        arrayList.add("Drama");
        arrayList.add("Science Fiction");
        arrayList.add("Horror");
        arrayList.add("Thriller");
        arrayList.add("Mystery");
        arrayList.add("Fantasy");
        arrayList.add("Romance");
        arrayList.add("Adventure");
        arrayList.add("Literary Fiction");
        arrayList.add("Satire");
        arrayList.add("Dystopian");
        arrayList.add("Fairy tale");
        Random random = new Random();
        for(String s: arrayList)
        {
            Chip chip =(Chip) LayoutInflater.from(MainActivity.this).inflate(R.layout.chip_layout,null);
            chip.setText(s);
            chip.setId(random.nextInt());
            chipGroup.addView(chip);
        }
        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if(!checkedIds.isEmpty())
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i: checkedIds)
                    {
                        Chip chip = findViewById(i);
                        stringBuilder.append(", ").append(chip.getText());
                    }
                    genres=stringBuilder.toString().replaceFirst(",","");
                }
            }
        });
        ArrayAdapter<CharSequence> adapterLanguage = ArrayAdapter.createFromResource(this,R.array.languages, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapterLength = ArrayAdapter.createFromResource(this,R.array.size, android.R.layout.simple_spinner_item);
        adapterLanguage.setDropDownViewResource(android.R.layout.simple_spinner_item);
        adapterLength.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLanguage.setAdapter(adapterLanguage);
        spinnerLength.setAdapter(adapterLength);
        spinnerLanguage.setOnItemSelectedListener(this);
        spinnerLength.setOnItemSelectedListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                age= String.valueOf(progress);
                textViewSeekBar.setText(age);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aLodingDialog.show();
                int selectedGenderId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedGenderId);
                gender=radioButton.getText().toString();
                Log.e("language",language);
                Log.e("length",size);
                Log.e("age",age);
                Log.e("gender",gender);
                Log.e("language",language);
                Log.e("genres",genres);
                Prompt = "Please generate a bed time story for a child of age "+age+" in language "+language+" the gender of the child is "+ gender +" the length of story should be in between "+size+" the genre of the story should be "+genres;
                chatGpt();
            }
        });
    }
    public void chatGpt()
    {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArrayMessage = new JSONArray();
        try
        {
            // Create the system message
            JSONObject jsonObjectSystemMessage = new JSONObject();
            jsonObjectSystemMessage.put("role", "system");
            jsonObjectSystemMessage.put("content", "You are a helpful assistant.");
            jsonArrayMessage.put(jsonObjectSystemMessage);

            // Create the user message
            JSONObject jsonObjectUserMessage = new JSONObject();
            jsonObjectUserMessage.put("role", "user");
            jsonObjectUserMessage.put("content", Prompt);
            jsonArrayMessage.put(jsonObjectUserMessage);

            // Add the messages array to the main JSON object
            jsonObject.put("model", "gpt-3.5-turbo");
            jsonObject.put("messages", jsonArrayMessage);
        }
        catch (JSONException e)
        {
            throw new RuntimeException(e);
        }
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder().url(urlEndPoint).header("Authorization", "Bearer "+myApiKey).post(body).build();
        try{
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful())
                    {
                        try
                        {
                            String responseBodyString = response.body().string(); // Read the response body string
                            JSONObject jsonObject1 = new JSONObject(responseBodyString);
                            JSONArray jsonArray = jsonObject1.getJSONArray("choices");
                            String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        aLodingDialog.cancel();
                                        Intent intent = new Intent(MainActivity.this, storyPage.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.putExtra("KEY_TEXT_TO_PASS", result);
                                        startActivity(intent);
                                    }
                                });
                            }catch (Exception e)
                            {
                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, response.toString() + "->"+ response.body().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Something went wrong try again", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinner1) {
            language = parent.getItemAtPosition(position).toString();
        } else if (parent.getId() == R.id.spinner2) {
            size = parent.getItemAtPosition(position).toString();
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinnerLanguage.setSelection(getIndex(spinnerLanguage, "English"));
        spinnerLength.setSelection(getIndex(spinnerLength, "150-200"));
    }
    private int getIndex(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        return adapter.getPosition(value);
    }
}