package mx.edu.tesoem.isc.tlgr.celdadecarga;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import mx.edu.tesoem.isc.tlgr.celdadecarga.model.Celda;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private List<Celda> listCelda = new ArrayList<Celda>();
    ArrayAdapter<Celda> arrayAdapterCelda;

    EditText nomres, produ, peso;
    TextView btconected;
    ListView listapeso;
    ImageView btnabout;
    Button btnguardar, btnactualizar, btneliminar, btnpesar;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Celda celdaSelected;

    String address = null , name=null;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try { setw();} catch (Exception e) {}

        nomres = findViewById(R.id.nomre);
        produ = findViewById(R.id.prod);
        peso = findViewById(R.id.pes);

        btnabout = findViewById(R.id.about);
        btnpesar = findViewById(R.id.pesar);
        btnguardar = findViewById(R.id.gu);
        btnactualizar = findViewById(R.id.ac);
        btneliminar = findViewById(R.id.el);

        listapeso = findViewById(R.id.listpe);

        inicioFireBase();
        listaDatos();

        listapeso.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                celdaSelected = (Celda) parent.getItemAtPosition(position);
                nomres.setText(celdaSelected.getNomres());
                produ.setText(celdaSelected.getProdu());
                peso.setText(celdaSelected.getPeso());
            }
        });

        btnguardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = nomres.getText().toString();
                String producto = produ.getText().toString();
                String pesoo = peso.getText().toString();

                if (nombre.equals("")||producto.equals("")||pesoo.equals("")){
                    validacion();
                }
                else {
                    Celda c = new Celda();
                    c.setIdc(UUID.randomUUID().toString());
                    c.setNomres(nombre);
                    c.setProdu(producto);
                    c.setPeso(pesoo);
                    databaseReference.child("Celda").child(c.getIdc()).setValue(c);
                    Toast.makeText(MainActivity.this,"Agregado", Toast.LENGTH_LONG).show();
                    limpiarcajas();
                }
            }
        });

        btnactualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                alerta.setMessage("¿Deseas actualizar los datos?")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Celda c = new Celda();
                                c.setIdc(celdaSelected.getIdc());
                                c.setNomres(nomres.getText().toString().trim());
                                c.setProdu(produ.getText().toString().trim());
                                c.setPeso(peso.getText().toString().trim());
                                databaseReference.child("Celda").child(c.getIdc()).setValue(c);
                                limpiarcajas();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Actualizar");
                titulo.show();
            }
        });

        btneliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                alerta.setMessage("¿Desea eliminar los datos?")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Celda c = new Celda();
                                c.setIdc(celdaSelected.getIdc());
                                databaseReference.child("Celda").child(c.getIdc()).removeValue();
                                limpiarcajas();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Eliminar");
                titulo.show();
            }
        });

        btnabout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private void setw() throws IOException
    {
        btconected=(TextView) findViewById(R.id.btco);
        bluetooth_connect_device();
        btnpesar=(Button)findViewById(R.id.pesar);
        
        btnpesar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){led_on_off("f");}
                if (event.getAction() == MotionEvent.ACTION_UP) {led_on_off("b");}
            return true;}
        });
    }

    private void bluetooth_connect_device() throws IOException {
        try {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size()>0){
                for (BluetoothDevice bt : pairedDevices){
                    address = bt.getAddress().toString();name = bt.getName().toString();
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception we){}
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
        btSocket.connect();
        try { btconected.setText("BT Name: " +name+"\n BT Adrress: "+address);}
        catch (Exception e) {}
    }

    private void led_on_off (String i){
        try {
            if (btSocket!=null){
                btSocket.getOutputStream().write(i.toString().getBytes());
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void listaDatos() {
        databaseReference.child("Celda").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                listCelda.clear();
                for (DataSnapshot objSnaptshot : datasnapshot.getChildren()){
                    Celda c = objSnaptshot.getValue(Celda.class);
                    listCelda.add(c);

                    arrayAdapterCelda = new ArrayAdapter<Celda>(MainActivity.this, android.R.layout.simple_expandable_list_item_1, listCelda);
                    listapeso.setAdapter(arrayAdapterCelda);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void inicioFireBase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void limpiarcajas(){
        nomres.setText("");
        produ.setText("");
        peso.setText("");
    }

    private void validacion() {
        String nombre = nomres.getText().toString();
        String producto = produ.getText().toString();
        String pesoo = peso.getText().toString();

        if (nombre.equals("")){
            nomres.setError("Dato Requerido");
        } else if (producto.equals("")){
            produ.setError("Dato Requerido");
        } else if (pesoo.equals("")){
            peso.setError("Dato Requerido");
        }
    }
}