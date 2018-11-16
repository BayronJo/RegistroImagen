package com.registroimagenes.registroimagenes;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity  {

    private int READ_EXTERNAL_STORAGE_PERMISSION_CODE=1;
    private int CODIGO_IMAGEN =2;
    private ImageView imageView;
    ProgressDialog progressDialog;
    private JsonObjectRequest jsonObjectRequest;
    private RequestQueue requestQueue;
    private StringRequest stringRequest;
    EditText txtnombre;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imagen);
        requestQueue = Volley.newRequestQueue(this);
        txtnombre = findViewById(R.id.txtnombre);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirArchivo();
            }
        });
    }

    public void Click_Guardar(View v){
        CargarWebServices();
    }
    private void abrirArchivo() {
        Toast.makeText(this,"Menu Abrir",Toast.LENGTH_SHORT).show();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent,CODIGO_IMAGEN);
        }
    }

    private void CargarWebServices(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando....");
        progressDialog.show();

        String URL ="http://192.168.1.200/Api/imagenes.php?";
        stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.hide();
                if(response.trim().equalsIgnoreCase("registra")){
                    txtnombre.setText("");
                    Toast.makeText(getApplicationContext(),"Se registro con Exito",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(),"No Se registro con Exito",Toast.LENGTH_LONG).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"No Se podido conectar",Toast.LENGTH_LONG).show();

            }
        }){
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                String nombre = txtnombre.getText().toString();
                String imagen = convertirImgString(bitmap);

                Map<String ,String> parametros = new HashMap<>();
                parametros.put("nombre",nombre);
                parametros.put("imagen",imagen);
                return parametros;
            }
        };

        requestQueue.add(stringRequest);
    }

    private String convertirImgString(Bitmap bitmap) {
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,array);
        byte [] imagenbyte = array.toByteArray();
        String imagenstring = Base64.encodeToString(imagenbyte,Base64.DEFAULT);
        return imagenstring;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CODIGO_IMAGEN && resultCode== Activity.RESULT_OK){
            Uri SelectedImage = data.getData();
            bitmap= null;
            try{
                bitmap = getBitmapFromUri(SelectedImage);
                imageView.setImageBitmap(bitmap);

                try{
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),SelectedImage);
                    imageView.setImageBitmap(bitmap);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException{
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return  image;
    }
}
