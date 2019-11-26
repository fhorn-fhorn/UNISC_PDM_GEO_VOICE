package com.m35725.unisc_pdm_geo_voice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListarAudiosActivity extends AppCompatActivity {

    private ListView lstViewAudios;

    List<Map<String,Object>>    audios;

    private String[] de = {"nomeAudio", "dataHora", "latitude", "longitude"};
    private int[] para = {R.id.txtViewListagemAudiosNomeAudio_Valor,
                            R.id.txtViewListagemAudiosDataHora_Valor,
                            R.id.txtViewListagemAudiosLatitude_Valor,
                            R.id.txtViewListagemAudiosLongitude_Valor};

    private DatabaseHelper  helper= null;

    private String idAcao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_audios);

        //id da acao a ser executada
        Bundle extras= getIntent().getExtras();
        idAcao= extras.getString("acao");

        //ListView
        lstViewAudios= findViewById(R.id.lstViewAudios);

        //Carrega do BD para a  ListView
        carregaBDParaListView();

        //ListView
        lstViewAudios.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String,Object> item= audios.get(i);
                if ( item != null ) {
                    //Se acao Player
                    if( idAcao.equals("player") ) {
                        Intent intent = new Intent(ListarAudiosActivity.this,
                                AudioPlayerActivity.class);
                        if (item.containsKey("id"))
                            intent.putExtra("id", item.get("id").toString());
                        startActivity(intent);
                    }
                }
                //Se acao Mapa
                if( idAcao.equals("mapa") ) {
                    Intent intent = new Intent(ListarAudiosActivity.this,
                            ListarAudiosMapaActivity.class);
                    if (item.containsKey("id"))
                        intent.putExtra("id", item.get("id").toString());
                    startActivity(intent);
                }
            }
        });

        lstViewAudios.setLongClickable(true);
        lstViewAudios.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                //Elemento
                Map<String,Object> item= audios.get(pos);
                //Verifica se chave existe
                if (item.containsKey("id")) {
                    //Apaga registros
                    helper = new DatabaseHelper(getApplicationContext());
                    SQLiteDatabase db = helper.getWritableDatabase();
                    String where="id=?";
                    String ID= item.get("id").toString();
                    if( db.delete(DatabaseHelper.DB_TABLE_NAME_AUDIO, where,
                            new String[]{ID}) == 1 ){
                        //FHORN
                        Toast.makeText(getApplicationContext(), "Apagado com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        //Carrega listview novamente
                        carregaBDParaListView();
                    }else{
                        //FHORN
                        Toast.makeText(getApplicationContext(), "Erro ao apagar registro id=" +
                                        item.get("id").toString(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //FHORN
                    Toast.makeText(getApplicationContext(), "Chave [id] não existe",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

    }

    private void carregaBDParaListView(){

        TextView txtViewListaVazia= findViewById(R.id.txtViewListaAudiosVazia);

        //Banco de Dados e ArrayList de audios
        helper= new DatabaseHelper(this);
        String query= "SELECT * FROM audio";
        SQLiteDatabase db= helper.getReadableDatabase();
        Cursor cursor= db.rawQuery(query, null);
        if( cursor.moveToFirst() ){
            audios= new ArrayList<Map<String,Object>>();
            DecimalFormat df = new DecimalFormat("#.###");
            for( int i = 0; i < cursor.getCount(); i++ ) {
                Map<String, Object> item = new HashMap<String, Object>();
                String id = cursor.getString(DatabaseHelper.DB_COLUMN_ID);
                String nomeAudio = cursor.getString(DatabaseHelper.DB_COLUMN_NOME_AUDIO);
                Double latitude = cursor.getDouble(DatabaseHelper.DB_COLUMN_LATITUDE);
                Double longitude = cursor.getDouble(DatabaseHelper.DB_COLUMN_LONGITUDE);
                String dtHr = cursor.getString(DatabaseHelper.DB_COLUMN_DATA_HORA);
                item.put("id", id);
                item.put("nomeAudio", nomeAudio);
                item.put("latitude", "Lat: "+ df.format(latitude) );
                item.put("longitude", "Lon: "+ df.format(longitude) );
                item.put("dataHora", dtHr);
                audios.add(item);
                cursor.moveToNext();
            }
            //Carrega na ListView
            AdaptadorListagemAudio adapter= new AdaptadorListagemAudio(this,
                    audios, R.layout.listagem_audios, de, para );
            lstViewAudios.setAdapter(adapter);
            txtViewListaVazia.setVisibility(View.GONE);
        }else{
            txtViewListaVazia.setVisibility(View.VISIBLE);
            lstViewAudios.setAdapter(null);
        }
        cursor.close();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (helper != null){
            helper.close();
            helper = null;
        }
    }

}
