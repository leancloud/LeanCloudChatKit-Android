package cn.leanclud.imkitapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.leanclud.imkit.activity.LCIMConversationActivity;
import cn.leanclud.imkit.utils.LCIMConstants;

public class MainActivity extends AppCompatActivity {

  private TextView peerIdView;
  private Button chatButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    peerIdView = (TextView)findViewById(R.id.peer_id_view);
    chatButton = (Button)findViewById(R.id.chat_btn);

    chatButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String peerId = peerIdView.getText().toString();
        Intent intent = new Intent(MainActivity.this, LCIMConversationActivity.class);
        intent.putExtra(LCIMConstants.PEER_ID, peerId);
        startActivity(intent);
      }
    });
  }
}
