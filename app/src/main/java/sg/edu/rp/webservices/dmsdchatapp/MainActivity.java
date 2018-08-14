package sg.edu.rp.webservices.dmsdchatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
 FirebaseAuth fbAuth;
 FirebaseUser fbUser;
 FirebaseDatabase firebaseDatabase;
    private DatabaseReference messageListRef, nameRef;
TextView tvWeather;
    EditText etMessage;
Button btnSend;
    String msguser;
    Long time;
    ArrayList<ChatMessage> alMessage = new ArrayList<ChatMessage>();
    private ChatMessage message;
    ArrayAdapter aaMessage;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fbAuth = FirebaseAuth.getInstance();

        fbUser = fbAuth.getCurrentUser();
        String uid = fbUser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        etMessage = (EditText) findViewById(R.id.etName);
        btnSend = (Button) findViewById(R.id.btnSend);
        lv = (ListView) findViewById(R.id.listView);
tvWeather = (TextView) findViewById(R.id.textViewWeather);
        alMessage = new ArrayList<ChatMessage>();
        aaMessage = new MessageAdapter(getBaseContext(), alMessage);
        lv.setAdapter(aaMessage);
        nameRef = firebaseDatabase.getReference("profiles/" + uid);
        messageListRef = firebaseDatabase.getReference("messages/");
        registerForContextMenu(lv);

        HttpRequest request = new HttpRequest
                ("https://api.data.gov.sg/v1/environment/2-hour-weather-forecast");
request.setAPIKey("USgDjj3BSjvuIANTX4LDvacxs5BG39Jx", "USgDjj3BSjvuIANTX4LDvacxs5BG39Jx");
        request.setOnHttpResponseListener(mHttpResponseListener);
        request.setMethod("GET");
        request.execute();
        nameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                msguser = (String) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = etMessage.getText().toString();

                time = new Date().getTime();

                ChatMessage messages = new ChatMessage(msg, time, msguser);
                messageListRef.push().setValue(messages);
                etMessage.setText("");


            }
        });


        messageListRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.i("CommunicateFragment", "onChildAdded");
                ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    msg.setId(dataSnapshot.getKey());
                    alMessage.add(msg);
                    aaMessage.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String selectedId = dataSnapshot.getKey();
                ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);

                if (msg != null) {
                    for (int i = 0; i < alMessage.size(); i++) {
                        if (alMessage.get(i).getId().equals(selectedId)) {
                            msg.setId(selectedId);
                            alMessage.set(i, msg);
                        }
                    }

                    aaMessage.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i("MainActivity", "onChildRemoved()");

                String selectedId = dataSnapshot.getKey();
                ChatMessage msg = dataSnapshot.getValue(ChatMessage.class);
                if (msg != null) {
                    for (int i = 0; i < alMessage.size(); i++) {
                        if (alMessage.get(i).getId().equals(selectedId)) {
                            msg.setId(selectedId);
                            alMessage.remove(i);
                        }
                    }
                    aaMessage.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });



    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deletemenu, menu);
    }



    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        String msguser1 = alMessage.get(index).getMessageUser();
        Toast.makeText(MainActivity.this, "msg sender: " + msguser + "/ncurrent user: " + msguser1, Toast.LENGTH_LONG).show();
        int id = item.getItemId();
        if (msguser1.equals(msguser)) {
            if (id == R.id.menu_delete) {


                messageListRef.child(alMessage.get(index).getId()).removeValue();
            }
            return true;


        } else if (!msguser1.equals(msguser)) {
            Toast.makeText(MainActivity.this, "You cannot delete other user's msg!", Toast.LENGTH_LONG).show();
        }




        return super.onContextItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_logout) {

            logout();

            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(i);
        }else{



            Intent i = new Intent(getBaseContext(), MainActivity.class);
            startActivity(i);

        }

        return super.onOptionsItemSelected(item);
    }

    private void logout(){
        fbAuth.signOut();
    }


    // Code for step 2 start
    private HttpRequest.OnHttpResponseListener mHttpResponseListener =
            new HttpRequest.OnHttpResponseListener() {
                @Override
                public void onResponse(String response) {

                    // process response here
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject woodlandsObj = (JSONObject) jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("forecasts").get(45);
                        String weather = (String) woodlandsObj.get("forecast");
tvWeather.setText("Weather Forecast @ Woodlands " + weather);




                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };

}