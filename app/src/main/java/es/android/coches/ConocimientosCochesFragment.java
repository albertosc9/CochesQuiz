package es.android.coches;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import es.android.coches.databinding.FragmentConocimientosCochesBinding;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ConocimientosCochesFragment extends Fragment {

    private FragmentConocimientosCochesBinding binding;


    List<PreguntaLogo> todasLasPreguntas;
    List<String> todasLasRespuestas;

    List<PreguntaLogo> preguntas;
    int respuestaCorrecta;
    int puntuacion;
    int puntuacionMaxima=0;
    JSONObject objJson;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        FileInputStream fis = null;
        try {


            if (fileExists(getContext(),"puntuaciones.json")){


                fis = getContext().openFileInput("puntuaciones.json");


                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String fileContent = br.readLine();
                while (fileContent != null) {
                    sb.append(fileContent);
                    fileContent = br.readLine();
                }

                objJson = new JSONObject(sb.toString());
                String puntuacion = objJson.getString("puntuacionMaxima");
               puntuacionMaxima = Integer.parseInt(puntuacion);


            }else {

                objJson = new JSONObject();
                objJson = objJson.put("puntuacionMaxima",puntuacionMaxima);
                objJson = objJson.put("ultima puntuacion",puntuacion);
                salvarFichero("puntuaciones.json",objJson.toString());
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        super.onCreate(savedInstanceState);
        if(todasLasPreguntas == null) {
            try {
                generarPreguntas("coches");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.shuffle(todasLasPreguntas);
        preguntas = new ArrayList<>(todasLasPreguntas);
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConocimientosCochesBinding.inflate(inflater,container,false);


        presentarPregunta();

        binding.botonRespuesta.setOnClickListener(v -> {
            int seleccionado = binding.radioGroup.getCheckedRadioButtonId();
            CharSequence mensaje = seleccionado == respuestaCorrecta ? "¡Acertaste!" : "Fallaste";
            if (mensaje.equals("¡Acertaste!")){
                puntuacion++;
            }
            Snackbar.make(v, mensaje, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Siguiente", v1 -> presentarPregunta())
                    .show();
            v.setEnabled(false);
        });

        return binding.getRoot();
    }


    private List<String> generarRespuestasPosibles(String respuestaCorrecta) {
        List<String> respuestasPosibles = new ArrayList<>();
        respuestasPosibles.add(respuestaCorrecta);

        List<String> respuestasIncorrectas = new ArrayList<>(todasLasRespuestas);
        respuestasIncorrectas.remove(respuestaCorrecta);

        for(int i=0; i<binding.radioGroup.getChildCount()-1; i++) {
            int indiceRespuesta = new Random().nextInt(respuestasIncorrectas.size());
            respuestasPosibles.add(respuestasIncorrectas.remove(indiceRespuesta));

        }
        Collections.shuffle(respuestasPosibles);
        return respuestasPosibles;
    }

    private void presentarPregunta() {
        if(preguntas.size() > 0) {
            binding.botonRespuesta.setEnabled(true);

            int pregunta = new Random().nextInt(preguntas.size());

            PreguntaLogo preguntaActual = preguntas.remove(pregunta);
            preguntaActual.setRespuetas(generarRespuestasPosibles(preguntaActual.respuestaCorrecta));

            InputStream bandera = null;
            try {
                int elemento = getResources().getIdentifier(preguntaActual.logo,"raw",getContext().getPackageName());
                bandera = getContext().getResources().openRawResource(elemento);
                binding.bandera.setImageBitmap(BitmapFactory.decodeStream(bandera));
            } catch (Exception e) {

                e.printStackTrace();
            }
            binding.radioGroup.clearCheck();

            for (int i = 0; i < binding.radioGroup.getChildCount(); i++) {
                RadioButton radio = (RadioButton) binding.radioGroup.getChildAt(i);

                CharSequence respuesta = preguntaActual.getRespuetas().get(i);
                if (respuesta.equals(preguntaActual.respuestaCorrecta)){
                    respuestaCorrecta = radio.getId();


                }

                radio.setText(respuesta);
            }
        } else {

            objJson = new JSONObject();
            if (puntuacionMaxima<puntuacion){


                try {
                    objJson = objJson.put("puntuacionMaxima",puntuacion);
                    objJson = objJson.put("ultima puntuacion",puntuacion);
                    salvarFichero("puntuaciones.json",objJson.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                binding.bandera.setVisibility(View.GONE);
                binding.radioGroup.setVisibility(View.GONE);
                binding.botonRespuesta.setVisibility(View.GONE);
                binding.textView.setText("Has batido tu record \n"+puntuacion);
            }else{
                try {
                    objJson = objJson.put("puntuacionMaxima",puntuacionMaxima);
                    objJson = objJson.put("ultima puntuacion",puntuacion);
                    salvarFichero("puntuaciones.json",objJson.toString());


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                binding.bandera.setVisibility(View.GONE);
                binding.radioGroup.setVisibility(View.GONE);
                binding.botonRespuesta.setVisibility(View.GONE);
                binding.textView.setText("No Has batido tu record \n"+puntuacion);
            }


        }



    }
    private void salvarFichero( String fichero,String texto) {
        FileOutputStream fos;
        try {
            fos = getContext().openFileOutput(fichero, Context.MODE_PRIVATE);
            fos.write(texto.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class PreguntaLogo {
        private String nombre;
        private String logo;
        private String respuestaCorrecta;
        private List<String> respuetas;

        public PreguntaLogo(String nombre, String logo, String respuestaCorrecta) {
            this.nombre = nombre;
            this.logo = logo;
            this.respuestaCorrecta = respuestaCorrecta;
        }

        public List<String> getRespuetas() {
            return respuetas;
        }

        public void setRespuetas(List<String> respuetas) {
            this.respuetas = respuetas;
        }
    }

    private void crearJson(){

    }
    private Document leerXML(String fichero) throws Exception {
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder constructor = factory.newDocumentBuilder();
        int elemento = getResources().getIdentifier(fichero,"raw",getContext().getPackageName());
        Document doc = constructor.parse(getContext().getResources().openRawResource(elemento));
        doc.getDocumentElement().normalize();
        return doc;
    }

    private void generarPreguntas(String fichero) throws Exception {
        todasLasPreguntas = new LinkedList<>();
        todasLasRespuestas = new LinkedList<>();
        Document doc = leerXML(fichero);
        Element documentElement = doc.getDocumentElement();
        NodeList paises = documentElement.getChildNodes();
        for(int i=0; i<paises.getLength(); i++) {
            if(paises.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element pais = (Element) paises.item(i);
                String nombre = pais.getAttribute("nombre");
                String nombreMostrar = pais.getElementsByTagName("nombre_mostrar").item(0).getTextContent();
                String ficheroLogo = pais.getElementsByTagName("logo").item(0).getTextContent();
                todasLasPreguntas.add(new PreguntaLogo(nombre, ficheroLogo, nombreMostrar));
                todasLasRespuestas.add(nombreMostrar);
            }
        }
    }
}