package com.m35725.unisc_pdm_geo_voice;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.AbstractChartView;
import lecho.lib.hellocharts.view.BubbleChartView;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;
import lecho.lib.hellocharts.view.PreviewColumnChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

public class AudioPlayerActivity extends AppCompatActivity {

    private DatabaseHelper  helper= null;

    private LineChartView lineChartView;
    private byte[] audioFileBytes;
    private int janelaXGrafico= 0;
    private int baseXGrafico= 0;
    private int incBaseXGrafico= 0;
    private int incTimerReproducao= 0;
    private boolean axisYCarregado= false;

    private MediaPlayer mediaPlayer= null;

    private Boolean encerraReproduzirAudio= false;
    private String AudioSavePathInDevice = null;

    private long startTime = 0;

    private TextView txtViewReproducaoValor;

    Handler reproducaoHandler = new Handler();
    Runnable timerReproducao = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            long miliSec = millis - (seconds * 1000);

            if( mediaPlayer.isPlaying() ) {
                millis = mediaPlayer.getCurrentPosition();
                seconds = (int) (millis / 1000);
                minutes = seconds / 60;
                seconds = seconds % 60;
                miliSec = millis - (seconds * 1000);

                txtViewReproducaoValor.setText(String.format("%02d:%02d.%03d",
                        minutes, seconds, miliSec));
            }

            //Conta mais uma passado do timer
            incTimerReproducao= incTimerReproducao + 1;
            if( incTimerReproducao >= 10 ){
                incTimerReproducao= 0;
                baseXGrafico= baseXGrafico + incBaseXGrafico;
                if( baseXGrafico > (audioFileBytes.length - janelaXGrafico) )
                    baseXGrafico= (audioFileBytes.length - janelaXGrafico);
                desenhaGrafico();
            }

            if( encerraReproduzirAudio == true ){
                //Encerra
                encerrarReproduzirAudio();
                //Encerrado
                encerraReproduzirAudio= false;
                //Fecha Activity
                AudioPlayerActivity.this.finish();
            }else
                reproducaoHandler.postDelayed(this, 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        txtViewReproducaoValor= (TextView) findViewById(R.id.txtViewAudioPlayerDuracao);

        Bundle extras= getIntent().getExtras();
        String idAudio= extras.getString("id");

        //Banco de Dados
        helper= new DatabaseHelper(this);
        String query= "SELECT * FROM audio WHERE id=" + idAudio;
        SQLiteDatabase db= helper.getReadableDatabase();
        Cursor cursor= db.rawQuery(query, null);
        if( cursor.moveToFirst() ) {
            String id = cursor.getString(DatabaseHelper.DB_COLUMN_ID);
            String nomeAudio = cursor.getString(DatabaseHelper.DB_COLUMN_NOME_AUDIO);
            Double latitude = cursor.getDouble(DatabaseHelper.DB_COLUMN_LATITUDE);
            Double longitude = cursor.getDouble(DatabaseHelper.DB_COLUMN_LONGITUDE);
            audioFileBytes = cursor.getBlob(DatabaseHelper.DB_COLUMN_ARQUIVO_AUDIO);
            String dtHr = cursor.getString(DatabaseHelper.DB_COLUMN_DATA_HORA);

            //Nome do audio
            TextView txtViewNomeAudio= findViewById(R.id.txtViewAudioPlayerNomeAudio);
            txtViewNomeAudio.setText(nomeAudio);
            //DataHora
            TextView txtViewDataHora= findViewById(R.id.txtViewAudioPlayerDataHora);
            txtViewDataHora.setText(dtHr);
            //Latitude
            DecimalFormat df = new DecimalFormat("#.###");
            TextView txtViewLatitude= findViewById(R.id.txtViewAudioPlayerLatitude);
            txtViewLatitude.setText("Lat: " + df.format(latitude));
            //Longitude
            TextView txtViewLongitude= findViewById(R.id.txtViewAudioPlayerLongitude);
            txtViewLongitude.setText("Lon: " + df.format(longitude));

            //BLOB para arquivo
            AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/GEO_VOICE_REC_" + "AudioRecording.3gp";
            File audioFile = new File(AudioSavePathInDevice);
            FileOutputStream fileOutput;
            try {
                fileOutput = new FileOutputStream(audioFile);
                fileOutput.write(audioFileBytes);
                fileOutput.flush();
                fileOutput.close();
            } catch (FileNotFoundException e) {
                Toast.makeText(this, "Arquivo não encontrado: " + e.toString(),
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } catch (IOException ex) {
                Toast.makeText(this, "Erro ao ler arquivo: " + ex.toString(),
                    Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }

            //Tocar áudio
            try {
                if( mediaPlayer == null ) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            encerraReproduzirAudio= true;
                        }
                    });
                }
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setLooping(false);
                //Timer
                startTime = System.currentTimeMillis();
                reproducaoHandler.postDelayed(timerReproducao, 0);
                //Grafico
                incBaseXGrafico= audioFileBytes.length / (mediaPlayer.getDuration()/10);
                baseXGrafico= 0;
                janelaXGrafico= 300;
                incTimerReproducao= 0;
                desenhaGrafico();
            } catch (IOException e) {
                //Mensagem
                Toast.makeText(this, "Erro: " + e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Audio não encontrado: " + idAudio, Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        encerrarReproduzirAudio();
    }

    protected void encerrarReproduzirAudio(){
        //Se esta tocando, para de tocar
        if( mediaPlayer.isPlaying() )
            mediaPlayer.stop();
        //Libera
        mediaPlayer.release();
        mediaPlayer = null;
        //Para timer
        reproducaoHandler.removeCallbacks(timerReproducao);
    }

    public void btnAudioPlayerCancelarOnClick(View view) {
        //Encerra reproduzir audio
        encerrarReproduzirAudio();
        //Fecha Activity
        AudioPlayerActivity.this.finish();
    }

    public void desenhaGrafico() {

        //http://nipunswritings.blogspot.com/2016/06/hellocharts-for-android-example.html

        String decimalPattern = "#.##";
        DecimalFormat decimalFormat = new DecimalFormat(decimalPattern);

        lineChartView = (LineChartView) findViewById(R.id.chart);

        List<PointValue> values = new ArrayList<PointValue>();
        PointValue tempPointValue;
        for(int i = baseXGrafico; i < (baseXGrafico+janelaXGrafico); i++ ) {
            tempPointValue = new PointValue(i, audioFileBytes[i]);
            values.add(tempPointValue);
        }

        Line line = new Line(values)
                .setColor(Color.BLUE)
                .setCubic(false)
                .setHasPoints(false).setHasLabels(false);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        List<AxisValue> axisValuesForX = new ArrayList<>();
        AxisValue tempAxisValue;
        for(int i = baseXGrafico; i < janelaXGrafico; i++){
            tempAxisValue = new AxisValue(i);
            axisValuesForX.add(tempAxisValue);
        }
        Axis xAxis = new Axis(axisValuesForX);
        data.setAxisXBottom(xAxis);

        if( axisYCarregado == false ) {
            //Valor mais alto para Y
            int maxAudioValue= 0;
            for( int i = 0; i < audioFileBytes.length; i++ ){
                if(audioFileBytes[i] > maxAudioValue)
                    maxAudioValue= audioFileBytes[i];
            }
            List<AxisValue> axisValuesForY = new ArrayList<>();
            for (int i = 0; i <= maxAudioValue; i++) {
                tempAxisValue = new AxisValue(i);
                axisValuesForY.add(tempAxisValue);
            }
            Axis yAxis = new Axis(axisValuesForY);
            data.setAxisYLeft(yAxis);
            axisYCarregado= true;
        }

        lineChartView.setLineChartData(data);

    }

}
