package com.saram.app.activitys;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.Result;
import com.saram.app.R;
import com.saram.app.models.Images;
import com.saram.app.models.Userbd;
import com.saram.app.models.rutas;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    // BASE DE DATOS
    Userbd userbd = new Userbd(this);
    Images imagenes = new Images();

    // SE DECLARAN LOS OBJETOS UTILIZADOS
    ImageView ivlogo, ivChatbot;
    LinearLayout llQR;
    TextView tvOlvido, tvRegistro;
    Button btnIngreso;
    EditText etNombre, etPass;
    TextInputLayout tilNombre, tilPass;
    // SE DECLARAN LOS OBJETOS NECESARIOS PARA LA LIBRERÍA DE VOLLEY
    RequestQueue requestQueue;
    ProgressDialog progressDialog;

    // PARA EL SCANNER DEL CÓDIGOS QR
    private ZXingScannerView myScannerview;

    // CREAMOS UNA CADENA LA CUAL CONTENDRÁ LA CADENA DE NUESTRO WEB SERVICE
    String HttpUri = rutas.login;

    // ESTE MÉTODO EVITA QUE SE REGRESE CON LA FLECHA DE RETORNO QUE TODOS TENEMOS
    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ELIMINA EL TOOL BAR DE LA ACTIVITY
        getSupportActionBar().hide();

        // SE ENLANZAN LOS CONTROLADORES CON LA VISTA
        ivlogo = (ImageView) findViewById(R.id.ivLogo);
        ivChatbot = (ImageView) findViewById(R.id.ivChatBot);
        tvOlvido = (TextView) findViewById(R.id.tvOlvido);
        tvRegistro = (TextView) findViewById(R.id.tvRegistro);
        btnIngreso = (Button) findViewById(R.id.btnIngreso);
        etNombre = (EditText) findViewById(R.id.etNombre);
        etPass = (EditText) findViewById(R.id.etPass);
        tilNombre = (TextInputLayout) findViewById(R.id.tilNombre);
        tilPass = (TextInputLayout) findViewById(R.id.tilPass);
        llQR = (LinearLayout) findViewById(R.id.llQR);

        Animation animacionChat = AnimationUtils.loadAnimation(this, R.anim.chat_bot_escala);
        animacionChat.setFillAfter(false);
        ivChatbot.setAnimation(animacionChat);

        // VERIFICA QUE TENGA LA APP LOS PERMISOS NECESARIOS PARA LA UTILIZACIÓN DE LA CAMARA
        final int permissionCheckCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        // SE VERIFICA CON LA CONDICIONAL QUE SE TENGAN LOS PERMISOS INSTALADOS
        if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // AQUI SE COLOCA MENSAJES EXTRAS QUE SE QUIERAN COLOCAR SOBRE LA EXPLICACIÓN DE LOS PERMISOS
            } else {
                // PEDIRÁ LA ACTIVACIÓN DEL SERVICIO
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }

        // SE ACTIVAN LOS OBJETOS DE REQUEST QUEUE Y PROGRESS DIALOG
        requestQueue = Volley.newRequestQueue(MainActivity.this);
        progressDialog = new ProgressDialog(MainActivity.this);

        // SE CREAN LAS VALIDACIONES EN TIEMPO REAL
        // VALIDACIÓN DENTRO DEL CAMPO NOMBRE
        etNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // PARA LIMPIAR LOS ERRORES DENTRO DEL PROCESO DE LLENADO DEL CAMPO
                tilNombre.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // VALIDACION DENTRO DEL CAMPO CONTRASEÑA
        etPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // PARA LIMPIAR LOS ERRORES DENTRO DEL PROCESO DE LLENADO DEL CAMPO
                tilNombre.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // OYENTE DEL TEXTO DE RECUPERAR LA CONTRASEÑA
        tvOlvido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevaContrasena();
            }
        });

        // OYENTE PARA UN NUEVO REGISTRO
        tvRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevoRegistro();
            }
        });

        // SE CREA EL OYENTE PARA EL BOTÓN DE INGRESO
        btnIngreso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ingresar();
            }
        });

        // SE CREAL EL OYECTE PARA EL LINEAR LAYOUT DE LECTOR DE QR
        llQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionCheckCamera == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(getApplicationContext(), "DEBES DE ACTIVAR LOS PERMISOS DE ACCESO A LA CÁMARA PARA PODER UTILIZAR ESTA OPCIÓN", Toast.LENGTH_LONG).show();
                }
                else{
                    activarLector();
                }
            }
        });

        // OYENTE DEL CHATBOT
        ivChatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent irChat = new Intent(getApplicationContext(), ChatBotActivity.class);
                startActivity(irChat);
            }
        });
    }

    // MÉTODO DE INGRESO AL INICIO DE LA APP
    private void ingresar(){
        // SE OBTIENEN LOS VALORES DE LOS CAMPOS
        final String email = tilNombre.getEditText().getText().toString();
        final String pass = tilPass.getEditText().getText().toString();

        // SE VALIDAN QUE LOS CAMPOS NO ESTEN VACÍOS
        if(email.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "LLENA TODOS LOS CAMPOS", Toast.LENGTH_LONG).show();
        }
        else{
            // SE OBTIENEN LOS RESULTADOS DE LA VALIDACIÓN DE AMBOS CAMPOS
            Boolean vemail = validaemail(email);
            Boolean vpass = validapassword(pass);

            // SE VALIDA QUE LOS CAMPOS HAYAN PASADO SUS RESPECTIVAS VALIDACIONES
            if(vemail && vpass) {
                // MOSTRAMOS EL PROGRESS DIALOG ---- AQUÍ SE COMIENZA EL ARMADO Y LA EJECUCIÓN DEL WEB SERVICE
                progressDialog.setMessage("CARGANDO...");
                progressDialog.show();
                // CREACIÓN DE LA CADENA A EJECUTAR EN EL WEB SERVICE MEDIANTE VOLLEY
                // Objeto de volley
                StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUri,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String serverresponse) {
                                // UNA VEZ QUE SE MANDAN TODOS LOS VALORES AL WEB SERVICE
                                // QUITAMOS EL PROGRESS DIALOG PARA QUE UNA VEZ QUE SE MANDEN LOS DATOS
                                // YA SE PUEDA TRABAJAR
                                progressDialog.dismiss();
                                // MANEJO DE ERRORES CON RESPECTO A LA RESPUESTA
                                try {
                                    // CREAR UN OBJETO DE TIPO JSON PARA OBTENER EL ARCHIVO QUE MANDARÁ EL WEB SERVICE
                                    JSONObject obj = new JSONObject(serverresponse);
                                    // INTERPRETAR EL VALOR DEL JSON OBTENIDO DEL WEB SERVICE
                                    String status = obj.getString("status");
                                    // OBTENER EL MENSAJE
                                    String mensaje = obj.getString("mensaje");

                                    // INTERPRETAR VALORES
                                    if(status.contentEquals("false")){
                                        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        // SE OBTIENE EL TOKEN
                                        String token = obj.getString("Token");
                                        // SE OBTIENE EL NOMBRE
                                        String nombre = obj.getString("Nombre");
                                        // SE OBTIENE EL NOMBRE
                                        String apellidos = obj.getString("Apellidos");

                                        Toast.makeText(getApplicationContext(), mensaje+" "+nombre, Toast.LENGTH_LONG).show();
                                        // ACTIVAMOS EL EDITOR DEL SHARED
                                        SharedPreferences sp1 = getSharedPreferences("MisDatos", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sp1.edit();
                                        editor.putString("nombre", nombre);
                                        editor.putString("token", token);
                                        editor.apply();
                                        // VERIFICAMOS QUE NO EXISTA REGISTRO EN LA BD
                                        String[] datos = userbd.getData("1");
                                        if(datos[0] == null){
                                            userbd.setData(1, nombre, imagenes.transformarImagenAByte(ivlogo));
                                        }

                                        // REDIRIGE AL INICIO DE LA APP
                                        Intent intent_inicio = new Intent(getApplicationContext(), inicioActivity.class);
                                        startActivity(intent_inicio);
                                    }
                                }catch(JSONException e){
                                    e.printStackTrace();
                                }

                            }
                            // ESTE SE EJECUTA SI HAY UN ERROR EN LA RESPUESTA
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // SE OCULTA EL PROGRESS DIALOG
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }){
                    // MAPEO DE LOS VALORES QUE MANDAMOS AL WEB SERVICE
                    protected Map<String, String> getParams(){
                        // RETORNAR LOS VALORES
                        Map<String, String> parametros = new HashMap<>();
                        parametros.put("Correo", email);
                        parametros.put("Contrasena", pass);
                        return parametros;
                    }
                };

                // SE MANDA A EJECUTAR EL STRING PARA LA LIBRERÍA DE VOLLEY
                requestQueue.add(stringRequest);
            }
        }
    }

    // MÉTODO DE LA ACCIÓN DEL OYENTE PARA LA RECUPERACIÓN DE LA CONTRASEÑA
    private void nuevaContrasena(){
        Intent intentcontra = new Intent(this, recuperarContra.class);
        startActivity(intentcontra);
    }

    // MÉTODO DE LA ACCIÓN DEL OYENTE PARA UN NUEVO REGISTRO
    private void nuevoRegistro(){
        // SE INTANCIA EL INTENT
        Intent intentregistro = new Intent(this, registroActivity.class);
        // SE INICIA LA NUEVA ACTIVITY
        startActivity(intentregistro);
    }
    private boolean validaemail(String noe){
        // O SE ASEGURA QUE TENGA DATOS ESPECÍFICOS DE UN EMAIL
        Pattern patronEmail = Patterns.EMAIL_ADDRESS;
        // Y NO PASE DEL LÍMTE PERMITIDO
        if(!patronEmail.matcher(noe).matches() || noe.length() > 50 ) {
            tilNombre.setError("Correo no válido, Intentalo de nuevo");
            return false;
        }else{
            tilNombre.setError(null);
        }
        return true;
    }
    private boolean validapassword(String password){
        // VALIDA QUE NO PASE DEL LÍMTE PERMITIDO
        if (password.length() > 16) {
            tilPass.setError("Contraseña no válida, Intentalo de nuevo");
            return false;
        }else{
            tilPass.setError(null);
        }
        return true;
    }
    private void activarLector(){
        myScannerview = new ZXingScannerView(this);
        setContentView(myScannerview);
        myScannerview.setResultHandler(this);
        myScannerview.startCamera();
    }

    // ESTE MÉTODO SE ENCARGA DE LEER EL CÓDIFO Y TRADUCIRLO
    @Override
    public void handleResult(final Result result) {
        // PARA QUE CONTINUE ESCANEANDO SI ES QUE YA SE ESCANEÓ UNA VEZ
        myScannerview.resumeCameraPreview(this);
        // MOSTRAMOS EL PROGRESS DIALOG ---- AQUÍ SE COMIENZA EL ARMADO Y LA EJECUCIÓN DEL WEB SERVICE
        progressDialog.setMessage("CARGANDO...");
        progressDialog.show();
        // CREACIÓN DE LA CADENA A EJECUTAR EN EL WEB SERVICE MEDIANTE VOLLEY
        // Objeto de volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpUri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String serverresponse) {
                        // UNA VEZ QUE SE MANDAN TODOS LOS VALORES AL WEB SERVICE
                        // QUITAMOS EL PROGRESS DIALOG PARA QUE UNA VEZ QUE SE MANDEN LOS DATOS
                        // YA SE PUEDA TRABAJAR
                        progressDialog.dismiss();
                        // MANEJO DE ERRORES CON RESPECTO A LA RESPUESTA
                        try {
                            // CREAR UN OBJETO DE TIPO JSON PARA OBTENER EL ARCHIVO QUE MANDARÁ EL WEB SERVICE
                            JSONObject obj = new JSONObject(serverresponse);
                            // INTERPRETAR EL VALOR DEL JSON OBTENIDO DEL WEB SERVICE
                            String status = obj.getString("status");
                            // OBTENER EL MENSAJE
                            String mensaje = obj.getString("mensaje");

                            // INTERPRETAR VALORES
                            if(status.contentEquals("false")){
                                Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                            }
                            else{
                                String nombre = obj.getString("Nombre");

                                Toast.makeText(getApplicationContext(), mensaje+" "+nombre, Toast.LENGTH_LONG).show();

                                // REDIRIGE AL INICIO DE LA APP
                                Intent intent_inicio = new Intent(getApplicationContext(), inicioActivity.class);
                                startActivity(intent_inicio);
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                    // ESTE SE EJECUTA SI HAY UN ERROR EN LA RESPUESTA
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // SE OCULTA EL PROGRESS DIALOG
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            // MAPEO DE LOS VALORES QUE MANDAMOS AL WEB SERVICE
            protected Map<String, String> getParams(){
                // RETORNAR LOS VALORES
                Map<String, String> parametros = new HashMap<>();
                parametros.put("Token", result.getText());
                return parametros;
            }
        };

        // SE MANDA A EJECUTAR EL STRING PARA LA LIBRERÍA DE VOLLEY
        requestQueue.add(stringRequest);
    }
}
