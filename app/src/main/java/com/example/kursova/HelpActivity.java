package com.example.kursova;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    int[] path;
    String[] descriptions;
    ImageView imageView;
    Button buttonNext;
    TextView textView;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        imageView = findViewById(R.id.imageView);
        buttonNext = findViewById(R.id.buttonNext);
        textView = findViewById(R.id.textView);

        descriptions = new String[3];
        descriptions[0] = "Для початку роботи слід натиснути кнопку SEARCH";
        descriptions[1] = "Для прокладення маршруту, необхідно ввести точку відправлення у поле \"From\", точку прибуття у поле\"To\" та натиснути на карту. Щоб переглянути опис та інформацію вибраної локації, слід натиснути на відповідну їй кнопку DETAILS.";
        descriptions[2] = "При натисненні на кнопку навігації, що знаходиться в нижньому лівому кутку екрану, на карті з'явиться поточне місцезнаходження пристрою. Заповнивши поле \"To\" та натиснушви на карту, буде прокладено маршрут до обраної локації, який кожні декілька секунд оновлюватиметься.";

        path = new int[3];
        path[0] = R.drawable.help1;
        path[1] = R.drawable.help2;
        path[2] = R.drawable.help3;

        imageView.setImageResource(path[0]);
        textView.setText(descriptions[0]);
        index = 1;

        buttonNext.setOnClickListener(v -> {
            imageView.setImageResource(path[(index % 3)]);
            textView.setText(descriptions[(index % 3)]);
            index++;
        });

    };
}
