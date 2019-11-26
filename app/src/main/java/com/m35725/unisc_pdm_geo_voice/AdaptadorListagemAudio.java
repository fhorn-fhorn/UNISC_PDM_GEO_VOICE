package com.m35725.unisc_pdm_geo_voice;

import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdaptadorListagemAudio extends SimpleAdapter {

    public AdaptadorListagemAudio(ListarAudiosActivity listarAudiosActivity,
                                  List<Map<String,Object>> listaAudios,
                                  int layoutListagemAudios, String[] de, int[] para) {
        super(listarAudiosActivity, listaAudios, layoutListagemAudios, de, para);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        View view = super.getView(position, convertView, parent);

        return view;
    }

}
