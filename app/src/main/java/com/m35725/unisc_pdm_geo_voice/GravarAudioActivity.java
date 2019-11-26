package com.m35725.unisc_pdm_geo_voice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.m35725.unisc_pdm_geo_voice.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

public class GravarAudioActivity extends AppCompatActivity {

    public final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO= 1;
    public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE= 2;

    private boolean gravandoAudio= false;
    private boolean tocandoAudio= false;
    private boolean encerraReproduzirAudio= false;

    private MediaRecorder mediaRecorder = null;
    private String AudioSavePathInDevice = null;

    private MediaPlayer mediaPlayer= null;

    private LocationManager locManager= null;
    private long locManagerTime = 0;
    private float locManagerDist = 0;

    private EditText edtViewNomeAudio;
    private TextView txtViewDuracaoValor;
    private TextView txtViewLatitude;
    private TextView txtViewLatitudeValor;
    private TextView txtViewLongitude;
    private TextView txtViewLongitudeValor;
    private TextView txtViewReproducaoValor;

    private Button btnStartStop;
    private Button btnReproduzir;
    private Button btnGravarEFechar;

    private DatabaseHelper helper= null;

    private long startTime = 0;

    Handler gravacaoHandler = new Handler();
    Runnable timerGravacao = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            long miliSec = millis - (seconds * 1000);

            //Tempo máximo. 5 minutos (5 * 60.000= 300.000 ms)
            if( millis >= 300000 ) {
                pararGravarAudio();
            }else {
                txtViewDuracaoValor.setText(String.format("%02d:%02d.%03d",
                        minutes, seconds, miliSec));
                gravacaoHandler.postDelayed(this, 1);
            }
        }
    };

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

            if( encerraReproduzirAudio == true ){
                //Encerra
                encerrarReproduzirAudio();
                //Encerrado
                encerraReproduzirAudio= false;
            }else
                reproducaoHandler.postDelayed(this, 1);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravar_audio);

        if( mediaRecorder == null )
            mediaRecorder = new MediaRecorder();

        if( mediaPlayer == null )
            criarMediaPlayer();

        edtViewNomeAudio= (EditText) findViewById(R.id.edtViewGravarAudioNomeAudioValor);
        txtViewDuracaoValor= (TextView) findViewById(R.id.txtViewGravarAudioDuracaoValor);
        txtViewLatitude= (TextView) findViewById(R.id.txtViewGravarAudioLatitude);
        txtViewLatitudeValor= (TextView) findViewById(R.id.txtViewGravarAudioLatitudeValor);
        txtViewLongitude= (TextView) findViewById(R.id.txtViewGravarAudioLongitude);
        txtViewLongitudeValor= (TextView) findViewById(R.id.txtViewGravarAudioLongitudeValor);
        txtViewReproducaoValor= (TextView) findViewById(R.id.txtViewReproducaoAudioDuracaoValor);

        txtViewDuracaoValor.setEnabled(false);
        txtViewLatitude.setEnabled(false);
        txtViewLatitudeValor.setEnabled(false);
        txtViewLongitude.setEnabled(false);
        txtViewLongitudeValor.setEnabled(false);
        txtViewReproducaoValor.setEnabled(false);

        btnStartStop= (Button) findViewById(R.id.btnIniciarGravarAudio);
        btnReproduzir= (Button) findViewById(R.id.btnTocarAudioGravado);
        btnGravarEFechar= (Button) findViewById(R.id.btnGravarAudioFechar);

        btnReproduzir.setEnabled(false);
        btnGravarEFechar.setEnabled(false);

        if( locManager == null ) {
            locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            GPSListening locManagerListener = new GPSListening();
            locManagerListener.txtViewLatitudeValor = txtViewLatitudeValor;
            locManagerListener.txtViewLongitudeValor = txtViewLongitudeValor;
            //GPS
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    locManagerTime, locManagerDist, locManagerListener);
            //PASSIVE
            locManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                    locManagerTime, locManagerDist, locManagerListener);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Se existe mediaRecorder
        if( mediaRecorder != null ){
            // Para a gravação do audio
            if( gravandoAudio == true ) {
                //Para de gravar
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder= null;
                //Para timer
                gravacaoHandler.removeCallbacks(timerGravacao);
            }
        }

        //Se existe mediaPlayer
        if( mediaPlayer != null ){
            //Para de tocar
            if( mediaPlayer.isPlaying() )
                mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer= null;
            //Para timer
            reproducaoHandler.removeCallbacks(timerReproducao);
        }

        //Se banco de dados
        if( helper != null ){
            helper.close();
        }

    }

    protected void criarMediaPlayer(){

        mediaPlayer = new MediaPlayer();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                encerraReproduzirAudio= true;
            }
        });

    }

    public boolean iniciarGravarAudio(){

        //Fonte: https://www.tutorialspoint.com/android/android_audio_capture.htm

        Boolean retVal= false;

        if( mediaRecorder == null )
            mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        //mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        //https://stackoverflow.com/questions/44555548/android-how-to-record-audio-using-mediarecorder-and-output-as-raw-pcm
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(AudioFormat.ENCODING_PCM_16BIT);

        AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/GEO_VOICE_REC_" + "AudioRecording.3gp";
                mediaRecorder.setOutputFile(AudioSavePathInDevice);

        try {

            //Inicia gravação
            mediaRecorder.prepare();
            mediaRecorder.start();

            //Mensagem
            Toast.makeText(this, "Gravação iniciada!", Toast.LENGTH_SHORT).show();

            //Timer
            startTime = System.currentTimeMillis();
            gravacaoHandler.postDelayed(timerGravacao, 0);

            //Habilita TextView do tempo de duração
            txtViewDuracaoValor.setEnabled(true);

            //Desabilita TextView GPS
            txtViewLatitude.setEnabled(false);
            txtViewLatitudeValor.setEnabled(false);
            txtViewLongitude.setEnabled(false);
            txtViewLongitudeValor.setEnabled(false);

            //Esconde o teclado
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View view = this.getCurrentFocus();
            if (view == null)
                view = new View(this);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            //Retorna
            retVal= true;

        } catch (IllegalStateException e) {

            //Mensagem
            Toast.makeText(this, "Erro 1: " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();

        } catch (IOException e) {

            //Mensagem
            Toast.makeText(this, "Erro 2: " + e.toString(), Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

        return retVal;

    }

    protected  void pararGravarAudio(){

        //Fonte: https://www.tutorialspoint.com/android/android_audio_capture.htm

        //Para timer
        gravacaoHandler.removeCallbacks(timerGravacao);
        txtViewDuracaoValor.setText("00:00.000");

        //Para a gravação do audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder= null;

        //Habilita TextView GPS
        txtViewLatitude.setEnabled(true);
        txtViewLatitudeValor.setEnabled(true);
        txtViewLongitude.setEnabled(true);
        txtViewLongitudeValor.setEnabled(true);

        //Muda o nome do botão
        btnStartStop.setText(R.string.btnIniciarGravarAudio);

        //Permite reproduzir
        btnReproduzir.setEnabled(true);
        txtViewReproducaoValor.setEnabled(true);

        //Permite gravar e fechar
        btnGravarEFechar.setEnabled(true);

        //Mensagem
        Toast.makeText(this, "Gravação encerrada!", Toast.LENGTH_SHORT).show();

    }

    public void btnIniciarGravarAudioOnClick(View view) {

        EditText tempEditText= (EditText) findViewById(R.id.edtViewGravarAudioNomeAudioValor);

        //Não esta gravando audio
        if( gravandoAudio == false ){
            //Verifica se tem nome
            if( tempEditText.getText().toString().isEmpty() ){
                //Mensagem de erro
                Toast.makeText(this, "Informe um nome para o audio",
                        Toast.LENGTH_SHORT).show();
            }else {
                //Verifica se tem permissao RECORD_AUDIO
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    //Verifica se tem permissao WRITE_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //Inicia gravacao do audio
                        if( iniciarGravarAudio() == true ) {
                            //Esta gravando audio
                            gravandoAudio = true;
                            //Muda nome do botão
                            btnStartStop.setText(R.string.btnEncerrarGravarAudio);
                            //Não permite reproduzir
                            btnReproduzir.setEnabled(false);
                            txtViewReproducaoValor.setEnabled(false);
                            //Não permite gravar e fechar
                            btnGravarEFechar.setEnabled(false);
                        }else {
                            Toast.makeText(this,
                                    "ERRO: Não é possível iniciar a gravação",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "WRITE_EXTERNAL_STORAGE: Sem Permissão",
                                Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                } else {
                    Toast.makeText(this, "RECORD_AUDIO: Sem Permissão",
                            Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                }
            }
        }else {
            //Para a gravação de audio
            pararGravarAudio();
            //Não esta mais gravando
            gravandoAudio= false;
        }

    }

    protected void encerrarReproduzirAudio(){
        //Se esta tocando, para de tocar
        if( mediaPlayer.isPlaying() )
            mediaPlayer.stop();
        //Libera
        mediaPlayer.release();
        mediaPlayer = null;
        //Nao toca mais
        tocandoAudio= false;
        //Para timer
        reproducaoHandler.removeCallbacks(timerReproducao);
        //Muda nome do botão
        btnReproduzir.setText(R.string.btnTocarAudioGravado);
        txtViewReproducaoValor.setText("00:00.000");
        //Deixa iniciar uma gravação
        btnStartStop.setEnabled(true);
        //Deixa gravar e fechar
        btnGravarEFechar.setEnabled(true);
    }

    public void btnTocarAudioGravadoOnClick(View view) {
        //Se esta tocando
        if( tocandoAudio == true ){
            //Encerra
            encerrarReproduzirAudio();
        }else {
            //Prepara para tocar o áudio
            try {
                if( mediaPlayer == null )
                    criarMediaPlayer();
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setLooping(false);
            } catch (IOException e) {
                //Mensagem
                Toast.makeText(this, "Erro: " + e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            //Muda nome do botão
            btnReproduzir.setText(R.string.btnPararAudioGravado);
            //Não deixa iniciar uma gravação
            btnStartStop.setEnabled(false);
            //Não deixa gravar e fechar
            btnGravarEFechar.setEnabled(false);
            //Timer
            startTime = System.currentTimeMillis();
            reproducaoHandler.postDelayed(timerReproducao, 0);
            //Esta tocando
            tocandoAudio = true;
            //Mensagem
            //Toast.makeText(this, "Reproduzindo áudio gravado", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnGravarAudioFecharOnClick(View view) {

        EditText tempEditText= (EditText) findViewById(R.id.edtViewGravarAudioNomeAudioValor);

        //Verifica se tem nome
        if( tempEditText.getText().toString().isEmpty() ) {
            //Mensagem de erro
            Toast.makeText(this, "Informe um nome para o audio", Toast.LENGTH_SHORT).show();
            return;
        }

        //Grava no banco de dados
        //NomeAudio(TEXT): edtViewNomeAudio
        //Latitude(DOUBLE): txtViewLatitudeValor
        //Longitude(DOUBLE): txtViewLongitudeValor
        //Arquivo(BLOB): AudioFile
        //DataHora (TEXT): data e hora

        File file = new File(AudioSavePathInDevice);

        FileInputStream audioFile;
        try {
            audioFile = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "Arquivo não encontrado: " + e.toString(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        ByteArrayOutputStream blobAudio = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = audioFile.read(buf)) != -1;) {
                blobAudio.write(buf, 0, readNum);
            }
        } catch (IOException ex) {
            Toast.makeText(this, "Erro ao ler arquivo: " + ex.toString(),
                    Toast.LENGTH_LONG).show();
        }
        byte[] AudioBytes = blobAudio.toByteArray();

        //Data e Hora
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format( c.getTime() );

        //Banco de dados
        helper= new DatabaseHelper(this);
        SQLiteDatabase db= helper.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put("nomeAudio", edtViewNomeAudio.getText().toString());
        values.put("latitude", Double.parseDouble( txtViewLatitudeValor.getText().toString() ) );
        values.put("longitude", Double.parseDouble( txtViewLongitudeValor.getText().toString() ) );
        values.put("audioFile", AudioBytes );
        values.put("dataHora", formattedDate );
        long resultado= db.insert(DatabaseHelper.DB_TABLE_NAME_AUDIO, null, values);
        if( resultado != -1 ){
            Toast.makeText(this, "Salvo com sucesso!!", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Erro ao salvar registro!",
                    Toast.LENGTH_SHORT).show();
        }
        helper.close();
        helper= null;

        //Fechar
        GravarAudioActivity.this.finish();
    }
}
