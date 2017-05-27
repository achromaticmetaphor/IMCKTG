package us.achromaticmetaphor.imcktg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuItem;

public class About extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_listview);
    final String[] cmds = new String[] {"About app", "About icons"};
    final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, cmds);
    ListView listView = (ListView) findViewById(R.id.cmdlist);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
        Intent intent = new Intent(About.this, AboutText.class);
        intent.putExtra("about_text", pos == 0 ? R.string.about_code : R.string.about_icons);
        startActivity(intent);
      }
    });
  }
}
